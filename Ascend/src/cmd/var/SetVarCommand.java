package cmd.var;

import cmd.Command;
import interpreter.Evaluator;
import interpreter.Parser;
import util.TokenArray;
import util.Value;

public class SetVarCommand extends Command {

	private String varName;
	private TokenArray varValue;
	
	public SetVarCommand(String varName, TokenArray varValue) {
		super("SET_VAR");
		this.varName = varName;
		this.varValue = varValue;
	}
	
	@Override
	public void onExecute() {
		devOutput("%s = %s", varName, varValue);
		Value result = Evaluator.evaluate(varValue);
		devOutput(" -> %s", result);
		Parser.getParser().getEnv().mapNameToValue(varName, result);
	}

}
