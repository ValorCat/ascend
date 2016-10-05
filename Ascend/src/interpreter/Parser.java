package interpreter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Stack;

import cmd.Command;
import control.FlowControl;
import interpreter.Compiler.CommandData;
import util.AscendException;
import util.ErrorCode;
import util.Value;

public class Parser {
	
	// Old Parser: pastebin.com/gS95e7nB
	
	private static Stack<Parser> parsers = new Stack<Parser>();
	
	private List<Command> commands;
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
	
	public List<Command> getCommands() {
		return commands;
	}
	
	public int[] getBlockLevels() {
		return blockLevels;
	}
	
	public int getBlockLevel() {
		return currentBlockLevel;
	}
	
	public int getCommandIndex() {
		return commandIndex;
	}
	
	public void setCommandIndex(int newIndex) {
		commandIndex = newIndex;
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
		while (commandIndex < commands.size()) {
			if (exit) {break;}
			Command command = commands.get(commandIndex);
			currentBlockLevel = blockLevels[commandIndex];
			if (Ascend.debugMode) {System.out.printf("(%d|%d) %s ", commandIndex, currentBlockLevel, command.getName());}
			command.onExecute();
			if (Ascend.debugMode) {System.out.println();}
			commandIndex++;
		}
	}
	
	public static Value callExecutable(String varName, Value exec, Value[] params) {
		try {
			String[] value = ((String) exec.value()).split("/");
			String libName = value[0];
			String name = value[1];
			if (name.equals("assert") || name.equals("clone")) {
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
