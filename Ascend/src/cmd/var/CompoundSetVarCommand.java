package cmd.var;

import cmd.Command;
import interpreter.Evaluator;
import interpreter.Parser;
import util.AscendException;
import util.ErrorCode;
import util.Operation;
import util.TokenArray;
import util.Value;

public class CompoundSetVarCommand extends Command {

	private String varName;
	private TokenArray modExpr;
	private String operator;
	
	public CompoundSetVarCommand(String varName, TokenArray modExpr, String operator) {
		super("SET_COMP_VAR");
		this.varName = varName;
		this.modExpr = modExpr;
		this.operator = operator;
	}
	
	@Override
	public void onExecute() {
		Parser parser = Parser.getParser();
		Value initValue = parser.getEnv().getValueFromName(varName);
		devOutput("%s (%s) %s %s", varName, initValue, operator, modExpr);
		if (!initValue.hasValue()) {
			throw new AscendException(ErrorCode.REFERENCE, "Cannot update uninitialized variable '" + varName + "'");
		}
		Value updateValue = Evaluator.evaluate(modExpr);
		Value newValue = Operation.operate(operator, initValue, updateValue);
		parser.getEnv().mapNameToValue(varName, newValue);
		devOutput(" -> %s", newValue);
	}

}
