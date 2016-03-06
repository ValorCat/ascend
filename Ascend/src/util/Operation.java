package util;

import interpreter.Environment;
import interpreter.Parser;

public class Operation {

	private static String type = null;
	private static Object value = null;
	
	private static interface Arithmetic {
		double operate(double a, double b);
	}
	
	private static interface Comparison {
		boolean operate(double a, double b);
	}
	
	public static Value operate(String operator, Value left, Value right) {
		switch (operator) {
		case "+":   add(left, right); break;
		case "-":   subtract(left, right); break;
		case "*":   multiply(left, right); break;
		case "/":   divide(left, right); break;
		case "%":   mod(left, right); break;
		case "^":   raise(left, right); break;
		case "==":  equal(left, right); break;
		case "<>":  inequal(left, right); break;
		case "<":   lessThan(left, right); break;
		case ">":   greaterThan(left, right); break;
		case "<=":  lessEqual(left, right); break;
		case ">=":  greaterEqual(left, right); break;
		case "and": and(left, right); break;
		case "or":  or(left, right); break;
		case "is":  isA(left, right); break;
		case "to":  typeCast(left, right); break;
		default:
			throw new AscendException(ErrorCode.SYNTAX, "The '%s' operator is undefined", operator);
		}
		Value result = poll();
		if (result == null) {
			throw new AscendException(ErrorCode.TYPE, "The '%s' operator is undefined for the operand types '%s' and '%s'", operator, left.type(), right.type());
		}
		return result;
	}
	
	public static Value operate(String operator, Value operand) {
		switch (operator) {
		case "not": not(operand); break;
		case "#":   length(operand); break;
		case "?":   toBoolean(operand); break;
		default:
			throw new AscendException(ErrorCode.SYNTAX, "The '%s' operator is undefined", operator);
		}
		Value result = poll();
		if (result == null) {
			throw new AscendException(ErrorCode.TYPE, "The '%s' operator is undefined for the operand type '%s'", operator, operand.type());		}
		return result;
	}
	
	public static void binaryOp(Value a, Value b, Arithmetic op) {
		if (a.isA("int")) {
			int l = (int) a.value();
			if (b.isA("int")) {
				type = "int";
				value = (int) op.operate(l, (int) b.value());
			} else if (b.isA("float")) {
				type = "float";
				value = op.operate(l, (double) b.value());
			}
		} else if (a.isA("float")) {
			double l = (double) a.value();
			if (b.isA("int")) {
				type = "float";
				value = op.operate(l, (double) (int) b.value());
			} else if (b.isA("float")) {
				type = "float";
				value = op.operate(l, (double) b.value());
			}
		}
	}
	
	public static void binaryComp(Value a, Value b, Comparison com) {
		type = "bool";
		if (a.isA("int")) {
			int l = (int) a.value();
			if (b.isA("int")) {
				value = com.operate(l, (int) b.value());
			} else if (b.isA("float")) {
				value = com.operate(l, (double) b.value());
			}
		} else if (a.isA("float")) {
			double l = (double) a.value();
			if (b.isA("int")) {
				value = com.operate(l, (double) (int) b.value());
			} else if (b.isA("float")) {
				value = com.operate(l, (double) b.value());
			}
		}
	}
	
	public static Value poll() {
		if (type == null || value == null) {
			return null;
		}
		Value result = new Value(type, value);
		type = null;
		value = null;
		return result;
	}
	
	public static void add(Value left, Value right) {
		if (left.isA("str") || right.isA("str")) {
			type = "str";
			if (left.isA("str")) {
				value = (String) left.value() + right.value();
			} else {
				value = left.value() + (String) right.value();
			}
			return;
		}
		binaryOp(left, right, (a, b) -> a + b);
	}
	
	public static void and(Value left, Value right) {
		type = "bool";
		if (left.isA("bool") && right.isA("bool")) {
			value = (boolean) left.value() && (boolean) right.value();
		}
	}
	
	public static void divide(Value left, Value right) {
		if ((right.isA("int") && ((int) right.value() == 0)) ||
				(right.isA("float") && ((double) right.value() == 0.0))) {
			throw new AscendException(ErrorCode.MATH, "Cannot divide " + left.value() + " by " + right.value());
		}
		binaryOp(left, right, (a, b) -> a / b);
	}
	
	public static void equal(Value left, Value right) {
		type = "bool";
		if (left.isA("str") && right.isA("str")) {
			value = left.value().equals(right.value());
			return;
		} else if (left.isA("type") && right.isA("type")) {
			value = left.value().equals(right.value());
			return;
		}
		binaryComp(left, right, (a, b) -> a == b);
	}
	
	public static void greaterThan(Value left, Value right) {
		binaryComp(left, right, (a, b) -> a > b);
	}
	
	public static void greaterEqual(Value left, Value right) {
		binaryComp(left, right, (a, b) -> a >= b);
	}
	
	public static void index(Value sequence, Value index1, Value index2) {
		if (!index1.isA("int")) {
			throw new AscendException(ErrorCode.TYPE, "Expected int index, got %s", index1.type());
		} else if (index2 != null && !index2.isA("int")) {
			throw new AscendException(ErrorCode.TYPE, "Expected int index, got %s", index2.type());
		}
		int intIndex1 = (int) index1.value();
		if (sequence.isA("str")) {
			type = "str";
			String base = (String) sequence.value();
			if (index2 == null) {
				// get char
				try {
					value = base.substring(intIndex1, intIndex1 + 1);
				} catch (IndexOutOfBoundsException e) {
					throw new AscendException(ErrorCode.INDEX, "Cannot get index %d from length %d string", intIndex1, base.length());
				}
			} else {
				// get slice
				int intIndex2 = (int) index2.value();
				try {
					value = base.substring(intIndex1, intIndex2);
				} catch (IndexOutOfBoundsException e) {
					throw new AscendException(ErrorCode.INDEX, "Cannot get slice %d:%d from length %d string", intIndex1, intIndex2, base.length());
				}
			}
		} else if (sequence.isArray()) {
			Environment env = Parser.getParser().getEnv();
			type = env.getArrayType(sequence);
			if (index2 == null) {
				// get item
				value = env.getValueFromArrayIndex(sequence, intIndex1).value();
			} else {
				// get slice
				int intIndex2 = (int) index2.value();
				type += "[]";
				Value[] slice = new Value[intIndex2 - intIndex1];
				for (int i = intIndex1; i < intIndex2; i++) {
					slice[i - intIndex1] = env.getValueFromArrayIndex(sequence, i);
				}
				value = env.defineArray(env.getArrayType(sequence), slice).value();
			}
		} else if (sequence.isA("type")) {
			if (index2 != null) {
				throw new AscendException(ErrorCode.SYNTAX, "Expected array length, got multiple values");
			}
			Environment env = Parser.getParser().getEnv();
			String arrayType = (String) sequence.value();
			Value newArray = env.defineArray(arrayType, new Value[intIndex1]);
			type = newArray.type();
			value = newArray.value();
		}
	}
	
	public static void inequal(Value left, Value right) {
		type = "bool";
		if (left.isA("str") && right.isA("str")) {
			value = !left.value().equals(right.value());
			return;
		} else if (left.isA("type") && right.isA("type")) {
			value = !left.value().equals(right.value());
			return;
		}
		binaryComp(left, right, (a, b) -> a != b);
	}
	
	public static void isA(Value left, Value right) {
		type = "bool";
		if (right.isA("type")) {
			value = left.type().equals(right.value());
		}
	}
	
	public static void length(Value operand) {
		type = "int";
		if (operand.isA("str")) {
			value = ((String) operand.value()).length();
		} else if (operand.isArray()) {
			value = Parser.getParser().getEnv().getArrayLength(operand);
		}
	}
	
	public static void lessThan(Value left, Value right) {
		binaryComp(left, right, (a, b) -> a < b);
	}
	
	public static void lessEqual(Value left, Value right) {
		binaryComp(left, right, (a, b) -> a <= b);
	}
	
	public static void mod(Value left, Value right) {
		binaryOp(left, right, (a, b) -> a % b);
	}
	
	public static void multiply(Value left, Value right) {
		binaryOp(left, right, (a, b) -> a * b);
	}
	
	public static void not(Value operand) {
		type = "bool";
		if (operand.isA("bool")) {
			value = !((boolean) operand.value());
		}
	}
	
	public static void or(Value left, Value right) {
		type = "bool";
		if (left.isA("bool") && right.isA("bool")) {
			value = (boolean) left.value() || (boolean) right.value();
		}
	}
	
	public static void raise(Value left, Value right) {
		binaryOp(left, right, (a, b) -> Math.pow(a, b));
	}
	
	public static void subtract(Value left, Value right) {
		binaryOp(left, right, (a, b) -> a - b);
	}
	
	public static void toBoolean(Value operand) {
		type = "bool";
		if (operand.isA("bool")) {
			value = operand.value();
		} else {
			value = castToBool(operand);
		}
	}
	
	public static void typeCast(Value left, Value right) {
		if (right.isA("type")) {
			String toType = (String) right.value();
			String fromType = (String) left.type();
			if (left.isA(toType)) {
				type = left.type();
				value = left.value();
			}
			type = toType;
			switch (toType) {
			case "bool":  value = castToBool(left); break;
			case "float": value = castToFloat(left); break;
			case "int":   value = castToInt(left); break;
			case "str":   value = castToStr(left); break;
			case "type":  value = castToType(left); break;
			default: throw new AscendException(ErrorCode.TYPE, "Cannot cast %s to %s", fromType, toType);
			}
		}
	}
	
	private static boolean castToBool(Value value) {
		boolean result;
		switch (value.type()) {
		case "float":
			result = (double) value.value() != 0.0;
			break;
		case "int":
			result = (int) value.value() != 0;
			break;
		case "str":
			result = !((String) value.value()).isEmpty();
			break;
		default:
			throw new AscendException(ErrorCode.TYPE, "Cannot cast %s to bool", value.type());
		}
		return result;
	}
	
	private static double castToFloat(Value value) {
		double result;
		switch (value.type()) {
		case "bool":
			result = (boolean) value.value() ? 1.0 : 0.0;
			break;
		case "int":
			result = (double) (int) value.value();
			break;
		case "str":
			result = Double.valueOf((String) value.value());
			break;
		default:
			throw new AscendException(ErrorCode.TYPE, "Cannot cast %s to float", value.type());
		}
		return result;
	}
	
	private static int castToInt(Value value) {
		int result;
		switch (value.type()) {
		case "bool":
			result = (boolean) value.value() ? 1 : 0;
			break;
		case "float":
			result = (int) (double) value.value();
			break;
		case "str":
			result = Integer.valueOf((String) value.value());
			break;
		default:
			throw new AscendException(ErrorCode.TYPE, "Cannot cast %s to int", value.type());
		}
		return result;
	}
	
	private static String castToStr(Value value) {
		String result;
		switch (value.type()) {
		case "bool":
			result = String.valueOf((boolean) value.value());
			break;
		case "float":
			result = String.valueOf((double) value.value());
			break;
		case "int":
			result = String.valueOf((int) value.value());
			break;
		case "type":
			result = (String) value.value();
			break;
		default:
			throw new AscendException(ErrorCode.TYPE, "Cannot cast %s to str", value.type());
		}
		return result;
	}
	
	private static String castToType(Value value) {
		String result;
		switch (value.type()) {
		case "str":
			result = (String) value.value();
			break;
		default:
			throw new AscendException(ErrorCode.TYPE, "Cannot cast %s to type", value.type());
		}
		return result;
	}

}
