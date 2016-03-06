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
		this.varName = varName;
		this.varValue = varValue;
	}
	
	@Override
	public String getName() {
		return "SET_VAR";
	}

	@Override
	public void execute(Parser parser) {
		devOutput("%s = %s", varName, varValue);
		Value result = Evaluator.evaluate(varValue);
		devOutput(" -> %s", result);
		parser.getEnv().mapNameToValue(varName, result);
	}

}
