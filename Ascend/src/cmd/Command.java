package cmd;

import interpreter.Ascend;

public abstract class Command {
	
	protected String name;

	public Command(String name) {
		this.name = name;
	}
	
	public abstract void onExecute();
	
	public String getName() {
		return name;
	}
	
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
