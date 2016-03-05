package lib;

import java.util.Random;

import util.Value;

public class RandomLib {
	
	public final static String[] FUNCTIONS = {"randbool", "randfloat", "randint", "random"};
	public final static String[] PROCEDURES = {"seed"};
	
	public static Value[] params;
	public static Value returnValue;
	
	private static Random randgen = new Random();
	
	public static void randbool() {
		StandardLib.paramCheck("randbool", params, params.length == 0);
		returnValue = new Value("bool", randgen.nextBoolean());
	}
	
	public static void randfloat() {
		StandardLib.paramCheck("randfloat", params, params.length == 2, "float", "float");
		double min = (double) params[0].value();
		double max = (double) params[1].value();
		returnValue = new Value("float", min + randgen.nextDouble() * (max - min));
	}
	
	public static void randint() {
		StandardLib.paramCheck("randint", params, params.length == 2, "int", "int");
		int min = (int) params[0].value();
		int max = (int) params[1].value();
		returnValue = new Value("int", min + randgen.nextInt(max - min + 1));
	}
	
	public static void random() {
		StandardLib.paramCheck("random", params, params.length == 0);
		returnValue = new Value("float", randgen.nextDouble());
	}
	
	public static void seed() {
		StandardLib.paramCheck("seed", params, params.length == 1, "int");
		randgen.setSeed((int) params[0].value()); 
	}

}
