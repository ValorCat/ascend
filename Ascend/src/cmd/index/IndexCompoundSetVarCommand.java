package cmd.index;

import cmd.Command;
import interpreter.Evaluator;
import interpreter.Parser;
import util.AscendException;
import util.ErrorCode;
import util.Operation;
import util.TokenArray;
import util.Value;

public class IndexCompoundSetVarCommand extends Command {

	private String varName;
	private TokenArray index;
	private TokenArray modExpr;
	private String operator;
	
	public IndexCompoundSetVarCommand(String varName, TokenArray index, TokenArray modExpr, String operator) {
		this.varName = varName;
		this.index = index;
		this.modExpr = modExpr;
		this.operator = operator;
	}

	@Override
	public String getName() {
		return "SEQ_COMP_SET_VAR";
	}

	@Override
	public void execute(Parser parser) {
		Value array = parser.getEnv().getValueFromName(varName);
		if (!array.isArray()) {
			throw new AscendException(ErrorCode.INDEX, "Cannot index type '%s'", array.type());
		}
		Value indexVal = Evaluator.evaluate(index);
		if (!indexVal.isA("int")) {
			throw new AscendException(ErrorCode.TYPE, "Index value must be of type 'int', got '%s'", indexVal.type());
		}
		int indexPos = (int) indexVal.value();
		Value initValue = parser.getEnv().getValueFromArrayIndex(array, indexPos);
		Value modValue = Evaluator.evaluate(modExpr);
		devOutput("%s[%d] %s %s %s", varName, indexPos, initValue, operator, modValue);
		Value newValue = Operation.operate(operator, initValue, modValue);
		parser.getEnv().mapArrayIndexToValue(array, indexPos, newValue);
		devOutput(" -> %s", newValue);
	}

}
