package cmd.var;

import cmd.Command;
import interpreter.Parser;
import util.Value;

public class SetCommand extends Command {

	private String varName;
	private Value varValue;
	
	public SetCommand(String varName, Value varValue) {
		super("SET_CONST");
		this.varName = varName;
		this.varValue = varValue;
	}

	@Override
	public void onExecute() {
		Parser.getParser().getEnv().mapNameToValue(varName, varValue);
		devOutput("%s = %s", varName, varValue);
	}

}
