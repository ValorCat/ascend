package cmd.var;

import cmd.Command;
import interpreter.Parser;
import util.Value;

public class SetCommand extends Command {

	private String varName;
	private Value varValue;
	
	public SetCommand(String varName, Value varValue) {
		this.varName = varName;
		this.varValue = varValue;
	}

	@Override
	public String getName() {
		return "SET_CONST";
	}

	@Override
	public void execute(Parser parser) {
		parser.getEnv().mapNameToValue(varName, varValue);
		devOutput("%s = %s", varName, varValue);
	}

}
