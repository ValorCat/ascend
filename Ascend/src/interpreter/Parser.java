package interpreter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Stack;

import cmd.Command;
import interpreter.Compiler.CommandData;
import util.AscendException;
import util.ErrorCode;
import util.Operation;
import util.TokenArray;
import util.Value;

public class Parser {
	
	// Old Parser: pastebin.com/gS95e7nB
	
	private static Stack<Parser> parsers = new Stack<Parser>();
	
	private Command[] commands;
	private int[] blockLevels;
	private Environment environment;
	private Stack<FlowControl> flowControls;
	
	private boolean exit;
	private int currentBlockLevel;
	private int commandIndex;
	
	public static Parser getParser() {
		return parsers.peek();
	}
	
	public static Parser loadNewParser() {
		parsers.push(new Parser());
		return parsers.peek();
	}
	
	public static void unloadParser() {
		parsers.pop();
	}
	
	public Environment getEnv() {
		return environment;
	}
	
	public int getBlockLevel() {
		return currentBlockLevel;
	}
	
	public int getCommandIndex() {
		return commandIndex;
	}
	
	public void addFlowControl(FlowControl control) {
		flowControls.push(control);
	}
	
	public FlowControl getTopControl() {
		return flowControls.peek();
	}
	
	public void removeFlowControl() {
		flowControls.pop();
	}
	
	public Parser() {
		this.environment = new Environment();
		this.flowControls = new Stack<FlowControl>();
		this.exit = false;
		this.currentBlockLevel = 0;
		this.commandIndex = 0;
	}
	
	public void parse(CommandData commandData) {
		commands = commandData.getCommands();
		blockLevels = commandData.getBlockLevels();
		if (Ascend.debugMode) {System.out.println("======================================================================");}
		while (commandIndex < commands.length) {
			if (exit) {break;}
			Command command = commands[commandIndex];
			currentBlockLevel = blockLevels[commandIndex];
			if (Ascend.debugMode) {System.out.printf("(%d|%d) %s ", commandIndex, currentBlockLevel, command.getName());}
			command.execute(getParser());
			if (Ascend.debugMode) {System.out.println();}
			commandIndex++;
		}
	}
	
	public abstract class FlowControl {
		protected int headerBlockLevel;
		protected int headerIndex;
		protected int endIndex;
		
		public FlowControl() {
			this.headerBlockLevel = currentBlockLevel;
			this.headerIndex = commandIndex;
			this.endIndex = findMatchingEndPosition();
		}
		
		public abstract String getName();
		
		public abstract boolean atEnd();
		
		private int findMatchingEndPosition() {
			int subControls = 1;
			for (int i = headerIndex + 3; i < commands.length; i++) {
				switch (commands[i].getName()) {
				case "OPEN_STRUCT":
					subControls++;
					break;
				case "CLOSE_STRUCT":
					if (--subControls == 0) {
						return i;
					}
					break;
				}
			}
			throw new AscendException(ErrorCode.SYNTAX, "Missing end statement");
		}
	}
	
	public class IfControl extends FlowControl {
		private ArrayList<Integer> elseBlockIndices;
		private int terminateIndex = endIndex;
		
		public IfControl() {
			super();
			elseBlockIndices = new ArrayList<Integer>();
			for (int i = headerIndex + 3; i < endIndex; i++) {
				if (blockLevels[i] == headerBlockLevel) {
					String commandName = commands[i].getName();
					if (commandName.equals("OPEN_IF_BRANCH")) {
						elseBlockIndices.add(i);
					} else if (!commandName.equals("CLOSE_STRUCT")) {
						throw new AscendException(ErrorCode.SYNTAX, "Else clause expected");
					}
				}
			}
		}
		
		@Override
		public String getName() {return "IF";}
		
		public boolean evaluate(TokenArray expr) {
			if (commandIndex == terminateIndex) {
				commandIndex = endIndex - 1;
				return false;
			}
			Value result = Evaluator.evaluate(expr);
			if (!result.isA("bool")) {
				throw new AscendException(ErrorCode.TYPE, "Conditional expression evaluated to '" + result.type() + "', expected 'bool'");
			}
			if (elseBlockIndices.size() > 0 && commandIndex == elseBlockIndices.get(0)) {
				elseBlockIndices.remove(0);
			}
			boolean resultAsBool = (boolean) result.value();
			boolean elsesRemaining = elseBlockIndices.size() > 0;
			if (resultAsBool) {
				if (elsesRemaining) {
					terminateIndex = elseBlockIndices.get(0);
				}
			} else {
				if (elsesRemaining) {
					commandIndex = elseBlockIndices.get(0) - 1;
				} else {
					commandIndex = endIndex - 1;
				}
			}
			return resultAsBool;
		}
		
		@Override
		public boolean atEnd() {return false;}
	}
	
	public class WhileControl extends FlowControl {
		private TokenArray controlExpr;
		
		public WhileControl(TokenArray controlExpr) {
			super();
			this.controlExpr = controlExpr;
		}

		@Override
		public String getName() {return "WHILE";}
		
		public boolean evaluate() {
			Value result = Evaluator.evaluate(controlExpr);
			if (!result.isA("bool")) {
				throw new AscendException(ErrorCode.TYPE, "Conditional expression evaluated to '" + result.type() + "', expected 'bool'");
			}
			boolean resultAsBool = (boolean) result.value();
			if (!resultAsBool) {
				commandIndex = endIndex;
			}
			return resultAsBool;
		}
		
		@Override
		public boolean atEnd() {
			boolean result = evaluate();
			if (result) {
				commandIndex = headerIndex + 1;
			}
			return result;
		}
	}
	
	public class ForControl extends FlowControl {
		private TokenArray finalExpr;
		private Value finalValue;
		private Value updateValue;
		private String varName;
		private String operator;

		public ForControl(TokenArray finalExpr, Value updateValue, String varName, boolean reverse) {
			super();
			this.finalExpr = finalExpr;
			this.finalValue = Evaluator.getConstValue(finalExpr);
			this.updateValue = updateValue;
			this.varName = varName;
			this.operator = reverse ? ">=" : "<=";
		}

		@Override
		public String getName() {return "FOR";}
		
		public boolean evaluate() {
			Value endValue = finalValue != null ? finalValue : Evaluator.evaluate(finalExpr);
			TokenArray continueExpr = new TokenArray(varName, operator, String.valueOf(endValue.value()));
			Value result = Evaluator.evaluate(continueExpr);
			if (!result.isA("bool")) {
				throw new AscendException(ErrorCode.TYPE, String.format("End expression evaluated to '%s', expected 'bool'", result.type()));
			}
			boolean resultAsBool = (boolean) result.value();
			if (!resultAsBool) {
				commandIndex = endIndex;
			}
			return resultAsBool;
		}
		
		@Override
		public boolean atEnd() {
			Environment env = getParser().getEnv();
			Value oldValue = env.getValueFromName(varName);
			Value newValue = Operation.operate("+", oldValue, updateValue);
			env.mapNameToValue(varName, newValue);
			boolean result = evaluate();
			if (result) {
				commandIndex = headerIndex + 1;
			}
			return result;
		}
		
		public String getIterName() {return varName;}
	}
	
	public class DoControl extends FlowControl {
		@Override
		public String getName() {return "DO";}
		
		@Override
		public boolean atEnd() {return false;}
	}
	
	public static Value callExecutable(String varName, Value exec, Value[] params) {
		try {
			String[] value = ((String) exec.value()).split("/");
			String libName = value[0];
			String name = value[1];
			if (name.equals("assert")) {
				name = "_" + name;
			}
			Method method;
			switch (libName) {
			case "core":
				method = lib.StandardLib.class.getMethod(name);
				lib.StandardLib.params = params;
				method.invoke(null);
				return lib.StandardLib.returnValue;
			case "math":
				method = lib.MathLib.class.getMethod(name);
				lib.MathLib.params = params;
				method.invoke(null);
				return lib.MathLib.returnValue;
			case "string":
				method = lib.StringLib.class.getMethod(name);
				lib.StringLib.params = params;
				method.invoke(null);
				return lib.StringLib.returnValue;
			case "random":
				method = lib.RandomLib.class.getMethod(name);
				lib.RandomLib.params = params;
				method.invoke(null);
				return lib.RandomLib.returnValue;
			default:
				throw new AscendException(ErrorCode.TYPE, "Invalid callable namespace '" + value + "'");
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof AscendException) {
				throw (AscendException) e.getCause();
			} else {
				e.printStackTrace();
			}
		}
		return null;
	}
	
}
