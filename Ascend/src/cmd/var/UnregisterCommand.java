package cmd.var;

import cmd.Command;
import interpreter.Parser;

public class UnregisterCommand extends Command {

	String varName;
	
	public UnregisterCommand(String varName) {
		super("UNREGISTER");
		this.varName = varName;
	}
	
	@Override
	public void onExecute() {
		Parser.getParser().getEnv().unmapName(varName);
		devOutput(varName);
	}

}
