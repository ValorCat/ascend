package cmd.var;

import cmd.Command;
import interpreter.Parser;
import util.Value;

public class RegisterCommand extends Command {

	private String varName;
	private String varType;
	
	public RegisterCommand(String varName, String varType) {
		super("REGISTER");
		this.varName = varName;
		this.varType = varType;
	}

	@Override
	public void onExecute() {
		Value value = new Value(varType);
		Parser.getParser().getEnv().mapNameToValue(varName, value);
		devOutput("%s %s", value.type(), varName);
	}

}
