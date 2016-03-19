package cmd.index;

import cmd.Command;
import interpreter.Evaluator;
import interpreter.Parser;
import util.AscendException;
import util.ErrorCode;
import util.Operation;
import util.TokenArray;
import util.Value;

public class IndexCompoundSetCommand extends Command {

	private String varName;
	private TokenArray index;
	private Value modValue;
	private String operator;

	public IndexCompoundSetCommand(String varName, TokenArray index, Value modValue, String operator) {
		this.varName = varName;
		this.index = index;
		this.modValue = modValue;
		this.operator = operator;
	}
	
	@Override
	public String getName() {
		return "SEQ_SET_COMP_CONST";
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
		devOutput("%s[%d] %s %s %s", varName, indexPos, initValue, operator, modValue);
		Value newValue = Operation.operate(operator, initValue, modValue);
		parser.getEnv().mapArrayIndexToValue(array, indexPos, newValue);
		devOutput(" -> %s", newValue);
	}

}
