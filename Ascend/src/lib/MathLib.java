package lib;

import util.AscendException;
import util.ErrorCode;
import util.Value;

public class MathLib {
	
	public final static String[] FUNCTIONS = {"abs", "acos", "asin", "atan", "ceil", "cos", "deg", "floor", "hypot", "ln", "log", "rad", "round", "signum", "sin", "tan"};
	public final static String[] PROCEDURES = {};
	
	public static Value[] params;
	public static Value returnValue;
	
	private static interface Expression {
		double execute(double arg);
	}
	
	private static Value operate(String name, Expression exp, double arg, boolean domain, String mathDomain) {
		if (domain) {
			throw new AscendException(ErrorCode.MATH, "Domain of " + name + " is " + mathDomain + ", got " + arg);
		}
		return new Value("float", exp.execute(arg));
	}
	
	private static Value operate(Expression exp) {
		return new Value("float", exp.execute((double) params[0].value()));
	}
	
	public static void abs() {
		StandardLib.paramCheck("abs", params, params.length == 1, "object");
		Value param = params[0];
		if (param.isA("int")) {
			returnValue = new Value("int", Math.abs((int) param.value()));
		} else if (param.isA("float")) {
			returnValue = new Value("float", Math.abs((double) param.value()));
		} else {
			throw new AscendException(ErrorCode.TYPE, StandardLib.getErrorMessage("abs", "int|float", params));
		}
	}
	
	public static void acos() {
		StandardLib.paramCheck("acos", params, params.length == 1, "float");
		double arg = (double) params[0].value();
		returnValue = operate("arccosine", x -> Math.acos(x), arg, Math.abs(arg) > 1, "-1 <= n <= 1");
	}
	
	public static void asin() {
		StandardLib.paramCheck("asin", params, params.length == 1, "float");
		double arg = (double) params[0].value();
		returnValue = operate("arcsine", x -> Math.asin(x), arg, Math.abs(arg) > 1, "-1 <= n <= 1");
	}
	
	public static void atan() {
		StandardLib.paramCheck("atan", params, params.length == 1, "float");
		returnValue = operate(x -> Math.atan(x));
	}
	
	public static void ceil() {
		StandardLib.paramCheck("ceil", params, params.length == 1, "float");
		returnValue = operate(x -> Math.ceil(x));
	}
	
	public static void cos() {
		StandardLib.paramCheck("cos", params, params.length == 1, "float");
		returnValue = operate(x -> Math.cos(x));
	}
	
	public static void deg() {
		StandardLib.paramCheck("deg", params, params.length == 1, "float");
		returnValue = operate(x -> Math.toDegrees(x));
	}
	
	public static void floor() {
		StandardLib.paramCheck("floor", params, params.length == 1, "float");
		returnValue = operate(x -> Math.floor(x));
	}
	
	public static void hypot() {
		StandardLib.paramCheck("hypot", params, params.length == 2, "float", "float");
		double arg1 = (double) params[0].value();
		double arg2 = (double) params[1].value();
		returnValue = new Value("float", Math.hypot(arg1, arg2));
	}
	
	public static void ln() {
		StandardLib.paramCheck("ln", params, params.length == 1, "float");
		double arg = (double) params[0].value();
		returnValue = operate("natural log function", x -> Math.log(x), arg, arg <= 0, "n > 0");
	}
	
	public static void log() {
		StandardLib.paramCheck("log", params, params.length == 2, "float", "float");
		double base = (double) params[0].value();
		double arg = (double) params[1].value();
		if (arg < 0) {
			throw new AscendException(ErrorCode.MATH, "Domain of log function is n >= 0, got " + arg);
		}
		returnValue = new Value("float", Math.log(arg) / Math.log(base));
	}
	
	public static void rad() {
		StandardLib.paramCheck("rad", params, params.length == 1, "float");
		returnValue = operate(x -> Math.toRadians(x));
	}
	
	public static void round() {
		StandardLib.paramCheck("round", params, params.length == 1, "float");
		returnValue = operate(x -> Math.round(x));
	}
	
	public static void signum() {
		StandardLib.paramCheck("signum", params, params.length == 1, "float");
		returnValue = operate(x -> Math.signum(x));
	}
	
	public static void sin() {
		StandardLib.paramCheck("sin", params, params.length == 1, "float");
		returnValue = operate(x -> Math.sin(x));
	}
	
	public static void tan() {
		StandardLib.paramCheck("tan", params, params.length == 1, "float");
		returnValue = operate(x -> Math.tan(x));
	}

}
