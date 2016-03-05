package cmd;

import interpreter.Parser;

public class UnregisterCommand extends Command {

	String varName;
	
	public UnregisterCommand(String varName) {
		this.varName = varName;
	}
	
	@Override
	public String getName() {
		return "UNREGISTER";
	}

	@Override
	public void execute(Parser parser) {
		parser.getEnv().unmapName(varName);
		devOutput(varName);
	}

}
