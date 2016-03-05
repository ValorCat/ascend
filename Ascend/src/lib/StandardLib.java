package lib;

import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Scanner;

import interpreter.Ascend;
import interpreter.Evaluator;
import interpreter.Parser;
import interpreter.Tokenizer;
import util.AscendException;
import util.ErrorCode;
import util.Value;

public class StandardLib {
	
	public final static String[] FUNCTIONS = {"all", "any", "callable", "env", "eval", "format", "id", "input", "max", "min", "sum", "typeof"};
	public final static String[] PROCEDURES = {"assert", "bind", "exec", "exit", "print", "printf", "sleep", "write"};
	
	public static Value[] params;
	public static Value returnValue;
	
	private static String outPrefix = interpreter.Ascend.debugMode ? "\n >> " : "";
	private static String inPrefix = interpreter.Ascend.debugMode ? "\n << " : "";
	
	static void paramCheck(String name, Value[] params, boolean inRange, String... paramTypes) {
		String paramTypesStr = "";
		for (String param : paramTypes) {
			paramTypesStr += ", " + param;
		}
		if (paramTypesStr.length() > 3) {
			paramTypesStr = paramTypesStr.substring(2);
		}
		String message = getErrorMessage(name, paramTypesStr, params);
		if (!inRange) {
			throw new AscendException(ErrorCode.TYPE, message);
		}
		for (int i = 0; i < paramTypes.length; i++) {
			String thisType = paramTypes[i];
			if (thisType.equals("object")) {
				continue;
			}
			if (thisType.endsWith("...")) {
				String type = thisType.substring(0, paramTypes[i].length() - 3);
				if (type.equals("object")) {
					break;
				}
				for (int j = i; j < params.length; j++) {
					if (!params[j].type().equals(type)) {
						throw new AscendException(ErrorCode.TYPE, message);
					}
				}
				return;
			}
			if (!params[i].type().equals(thisType)) {
				throw new AscendException(ErrorCode.TYPE, message);
			}
		}
	}
	
	static String getErrorMessage(String name, String paramTypes, Value[] params) {
		String argTypes = "";
		for (Value param : params) {
			argTypes += ", " + param.type();
		}
		if (argTypes.length() > 3) {
			argTypes = argTypes.substring(2);
		}
		return String.format("Expected call %1$s(%2$s), got %1$s(%3$s)", name, paramTypes, argTypes);
	}
	
	public static void all() {
		paramCheck("all", params, params.length > 0, "bool...");
		for (Value param : params) {
			if (!(boolean) param.value()) {
				returnValue = new Value("bool", false);
				return;
			}
		}
		returnValue = new Value("bool", true);
	}
	
	public static void any() {
		paramCheck("any", params, params.length > 0, "bool...");
		for (Value param : params) {
			if ((boolean) param.value()) {
				returnValue = new Value("bool", true);
				return;
			}
		}
		returnValue = new Value("bool", false);
	}
	
	public static void _assert() {
		paramCheck("assert", params, params.length == 1, "bool");
		if (!(boolean) (params[0].value())) {
			throw new AscendException(ErrorCode.ASSERT, "Assertion failed");
		}
	}
	
	public static void bind() {
		paramCheck("bind", params, params.length == 2, "str", "object");
		interpreter.Parser.getParser().getEnv().mapNameToValue((String) params[0].value(), params[1]);
	}
	
	public static void callable() {
		paramCheck("callable", params, params.length == 1, "object");
		String type = params[0].type();
		if (type.equals("proc") || type.equals("func")) {
			returnValue = new Value("bool", true);
		} else {
			returnValue = new Value("bool", false);
		}
	}
	
	public static void env() {
		paramCheck("env", params, params.length == 0);
		// an array
	}
	
	public static void eval() {
		paramCheck("eval", params, params.length == 1, "str");
		String input = (String) params[0].value();
		Parser.loadNewParser();
		returnValue = Evaluator.evaluate(Tokenizer.tokenize(input));
		Parser.unloadParser();
	}
	
	public static void exec() {
		paramCheck("exec", params, params.length == 1, "str");
		String input = (String) params[0].value();
		if (Ascend.debugMode) {
			System.out.println();
		}
		interpreter.Ascend.execute(input);
	}
	
	public static void exit() {
		paramCheck("exit", params, params.length == 0);
		// handled in Parser.callExecutable
	}
	
	public static void format() {
		paramCheck("format", params, params.length > 1, "str", "object...");
		String formatString = (String) params[0].value();
		Value[] range = Arrays.copyOfRange(params, 1, params.length);
		Object[] args = new Object[range.length];
		for (int i = 0; i < range.length; i++) {
			args[i] = (Object) range[i].value();
		}
		try {
			returnValue = new Value("str", String.format(formatString, args));
		} catch (IllegalFormatException e) {
			throw new AscendException(ErrorCode.TYPE, e.getMessage());
		}
	}
	
	public static void id() {
		paramCheck("id", params, params.length == 1, "int");
		returnValue = interpreter.Parser.getParser().getEnv().getValueFromID((int) params[0].value());
	}
	
	public static void input() {
		paramCheck("input", params, params.length == 0);
		Scanner scanner = new Scanner(System.in);
		System.out.print(inPrefix);
		returnValue = new Value("str", scanner.nextLine());
		scanner.close();
	}
	
	public static void max() {
		paramCheck("max", params, params.length > 1, "object");
		double max = Double.NEGATIVE_INFINITY;
		boolean aFloat = false;
		for (Value value : params) {
			if (value.isA("int")) {
				int v = (int) value.value();
				if (v > max) {
					max = v;
				}
			} else if (value.isA("float")) {
				aFloat = true;
				double v = (double) value.value();
				if (v > max) {
					max = v;
				}
			} else {
				throw new AscendException(ErrorCode.TYPE, "The 'max' executable expects a series of integers or floats");
			}
		}
		if (aFloat) {
			returnValue = new Value("float", max);
		} else {
			returnValue = new Value("int", (int) max);
		}
	}
	
	public static void min() {
		paramCheck("min", params, params.length > 1, "object");
		double min = Double.POSITIVE_INFINITY;
		boolean aFloat = false;
		for (Value value : params) {
			if (value.isA("int")) {
				int v = (int) value.value();
				if (v < min) {
					min = v;
				}
			} else if (value.isA("float")) {
				aFloat = true;
				double v = (double) value.value();
				if (v < min) {
					min = v;
				}
			} else {
				throw new AscendException(ErrorCode.TYPE, "The 'min' executable expects a series of integers or floats");
			}
		}
		if (aFloat) {
			returnValue = new Value("float", min);
		} else {
			returnValue = new Value("int", (int) min);
		}
	}
	
	public static void print() {
		paramCheck("print", params, params.length <= 1, "object");
		String arg = params.length == 0 ? "" : params[0].toCleanString();
		System.out.print(outPrefix + arg + (interpreter.Ascend.debugMode ? "" : "\n"));
	}
	
	public static void printf() {
		paramCheck("printf", params, params.length > 1, "str", "object...");
		String formatString = (String) params[0].value();
		Value[] range = Arrays.copyOfRange(params, 1, params.length);
		Object[] args = new Object[range.length];
		for (int i = 0; i < range.length; i++) {
			args[i] = (Object) range[i].value();
		}
		try {
			System.out.print(outPrefix + String.format(formatString, args) + (interpreter.Ascend.debugMode ? "" : "\n"));
		} catch (IllegalFormatException e) {
			throw new AscendException(ErrorCode.TYPE, e.getMessage());
		}
	}
	
	public static void sleep() throws InterruptedException {
		paramCheck("sleep", params, params.length == 1, "int");
		Thread.sleep((int) params[0].value());
	}
	
	public static void sum() {
		paramCheck("sum", params, params.length > 0, "object");
		double sum = 0;
		boolean aFloat = false;
		for (Value value : params) {
			if (value.isA("int")) {
				sum += (int) value.value();
			} else if (value.isA("float")) {
				aFloat = true;
				sum += (double) value.value();
			} else {
				throw new AscendException(ErrorCode.TYPE, "The 'sum' executable expects a series of integers or floats");
			}
		}
		if (aFloat) {
			returnValue = new Value("float", sum);
		} else {
			returnValue = new Value("int", (int) sum);
		}
	}
	
	public static void typeof() {
		paramCheck("type", params, params.length == 1, "object");
		returnValue = new Value("type", params[0].type());
	}
	
	public static void write() {
		paramCheck("write", params, params.length == 1, "object");
		String arg = params[0].toCleanString();
		System.out.print(outPrefix + arg + (interpreter.Ascend.debugMode ? "\n" : ""));
	}
	
}
