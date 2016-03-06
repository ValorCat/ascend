package cmd.var;

import cmd.Command;
import interpreter.Parser;
import util.Value;

public class RegisterCommand extends Command {

	private String varName;
	private String varType;
	
	public RegisterCommand(String varName, String varType) {
		this.varName = varName;
		this.varType = varType;
	}

	@Override
	public String getName() {
		return "REGISTER";
	}

	@Override
	public void execute(Parser parser) {
		Value value = new Value(varType);
		parser.getEnv().mapNameToValue(varName, value);
		devOutput("%s %s", value.type(), varName);
	}

}
