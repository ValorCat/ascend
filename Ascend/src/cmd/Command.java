package cmd;

import interpreter.Ascend;
import interpreter.Parser;

public abstract class Command {
	
	public abstract String getName();
	
	public abstract void execute(Parser parser);
	
	protected void devOutput(Object output) {
		if (Ascend.debugMode) {
			System.out.print(output);
		}
	}
	
	protected void devOutput(String format, Object... inputs) {
		if (Ascend.debugMode) {
			System.out.printf(format, inputs);
		}
	}

}
