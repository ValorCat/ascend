package lib;

import java.util.Random;

import interpreter.Environment;
import interpreter.Parser;
import util.AscendException;
import util.ErrorCode;
import util.Value;

public class RandomLib {
	
	public final static String[] FUNCTIONS = {"choose", "getBool", "getFloat", "getInt", "getStr", "rand"};
	public final static String[] PROCEDURES = {"seed"};
	
	public static Value[] params;
	public static Value returnValue;
	
	private static Random randgen = new Random();
	
	public static void choose() {
		StandardLib.paramCheck("choose", params, params.length == 1, "object");
		if (!params[0].isArray()) {
			throw new AscendException(ErrorCode.TYPE, "The 'choose' executable expects an array");
		}
		Environment env = Parser.getParser().getEnv();
		int length = env.getArrayLength(params[0]);
		int randNum = randgen.nextInt(length);
		returnValue = env.getValueFromArrayIndex(params[0], randNum);
	}
	
	public static void getBool() {
		StandardLib.paramCheck("getBool", params, params.length == 0);
		returnValue = new Value("bool", randgen.nextBoolean());
	}
	
	public static void getFloat() {
		StandardLib.paramCheck("getFloat", params, params.length == 2, "float", "float");
		double min = (double) params[0].value();
		double max = (double) params[1].value();
		returnValue = new Value("float", min + randgen.nextDouble() * (max - min));
	}
	
	public static void getInt() {
		StandardLib.paramCheck("getInt", params, params.length == 2, "int", "int");
		int min = (int) params[0].value();
		int max = (int) params[1].value();
		returnValue = new Value("int", min + randgen.nextInt(max - min + 1));
	}
	
	public static void getStr() {
		StandardLib.paramCheck("getStr", params, params.length == 2, "str", "int");
		String options = (String) params[1].value();
		int optionsLength = options.length();
		int length = (int) params[1].value();
		StringBuilder string = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int rand = randgen.nextInt(optionsLength);
			string.append(options.charAt(rand));
		}
		returnValue = new Value("str", string.toString());
	}
	
	public static void rand() {
		StandardLib.paramCheck("rand", params, params.length == 0);
		returnValue = new Value("float", randgen.nextDouble());
	}
	
	public static void seed() {
		StandardLib.paramCheck("seed", params, params.length == 1, "int");
		randgen.setSeed((int) params[0].value()); 
	}

}
