package lib;

import interpreter.Parser;
import util.Value;

public class StringLib {
	
	public final static String[] FUNCTIONS = {"compare", "contains", "copy", "count", "index", "lower", "prefix", "replace", "reverse", "split", "suffix", "pad", "trim", "upper"};
	public final static String[] PROCEDURES = {};
	
	public static Value[] params;
	public static Value returnValue;
	
	public static void compare() {
		StandardLib.paramCheck("compare", params, params.length == 2, "str", "str");
		String str1 = (String) params[0].value();
		String str2 = (String) params[1].value();
		returnValue = new Value("int", str1.compareTo(str2));
	}
	
	public static void contains() {
		StandardLib.paramCheck("contains", params, params.length == 2, "str", "str");
		String str1 = (String) params[0].value();
		String str2 = (String) params[1].value();
		returnValue = new Value("bool", str1.indexOf(str2) > -1);
	}
	
	public static void copy() {
		StandardLib.paramCheck("copy", params, params.length == 2, "str", "int");
		String str = (String) params[0].value();
		int count = (int) params[1].value();
		String result = "";
		for (int i = 0; i < count; i++) {
			result += str;
		}
		returnValue = new Value("str", result);
	}
	
	public static void count() {
		StandardLib.paramCheck("count", params, params.length == 2, "str", "str");
		String str = (String) params[0].value();
		String query = (String) params[1].value();
		int lastIndex = 0;
		int count = 0;
		while (lastIndex != -1) {
			lastIndex = str.indexOf(query, lastIndex);
			if (lastIndex != -1) {
				count++;
				lastIndex += query.length();
			}
		}
		returnValue = new Value("int", count);
	}
	
	public static void index() {
		StandardLib.paramCheck("index", params, params.length == 2, "str", "str");
		String str1 = (String) params[0].value();
		String str2 = (String) params[1].value();
		returnValue = new Value("int", str1.indexOf(str2));
	}
	
	public static void lower() {
		StandardLib.paramCheck("lower", params, params.length == 1, "str");
		String str = (String) params[0].value();
		returnValue = new Value("str", str.toLowerCase());
	}
	
	public static void pad() {
		StandardLib.paramCheck("pad", params, params.length == 2, "str", "int");
		String str = (String) params[0].value();
		int len = (int) params[1].value();
		returnValue = new Value("str", String.format("%1$"+len+"s", str));
	}
	
	public static void prefix() {
		StandardLib.paramCheck("prefix", params, params.length == 2, "str", "str");
		String str = (String) params[0].value();
		String prefix = (String) params[1].value();
		returnValue = new Value("bool", str.startsWith(prefix));
	}
	
	public static void replace() {
		StandardLib.paramCheck("replace", params, params.length == 3, "str", "str", "str");
		String str = (String) params[0].value();
		String query = (String) params[1].value();
		String repl = (String) params[2].value();
		returnValue = new Value("str", str.replaceAll(query, repl));
	}
	
	public static void reverse() {
		StandardLib.paramCheck("reverse", params, params.length == 1, "str");
		String str = (String) params[0].value();
		returnValue = new Value("str", new StringBuilder(str).reverse().toString());
	}
	
	public static void split() {
		StandardLib.paramCheck("split", params, params.length == 2, "str", "str");
		String str = (String) params[0].value();
		String regex = (String) params[1].value();
		String[] splits = str.split(regex);
		Value[] values = new Value[splits.length];
		for (int i = 0; i < splits.length; i++) {
			values[i] = new Value("str", splits[i]);
		}
		returnValue = Parser.getParser().getEnv().defineArray("str", values);
	}
	
	public static void suffix() {
		StandardLib.paramCheck("suffix", params, params.length == 2, "str", "str");
		String str = (String) params[0].value();
		String suffix = (String) params[1].value();
		returnValue = new Value("str", str.endsWith(suffix));
	}
	
	public static void trim() {
		StandardLib.paramCheck("trim", params, params.length == 1, "str");
		String str = (String) params[0].value();
		returnValue = new Value("str", str.trim());
	}
	
	public static void upper() {
		StandardLib.paramCheck("replace", params, params.length == 1, "str");
		String str = (String) params[0].value();
		returnValue = new Value("str", str.toUpperCase());
	}

}
