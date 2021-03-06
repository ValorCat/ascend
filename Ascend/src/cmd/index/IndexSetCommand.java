package cmd.index;

import cmd.Command;
import interpreter.Evaluator;
import interpreter.Parser;
import util.AscendException;
import util.ErrorCode;
import util.TokenArray;
import util.Value;

public class IndexSetCommand extends Command {

	private String varName;
	private TokenArray index;
	private Value value;

	public IndexSetCommand(String varName, TokenArray index, Value value) {
		super("SEQ_SET_CONST");
		this.varName = varName;
		this.index = index;
		this.value = value;
	}

	@Override
	public void onExecute() {
		Parser parser = Parser.getParser();
		Value array = parser.getEnv().getValueFromName(varName);
		if (!array.isArray()) {
			throw new AscendException(ErrorCode.INDEX, "Cannot index type '%s'", array.type());
		}
		Value indexVal = Evaluator.evaluate(index);
		if (!indexVal.isA("int")) {
			throw new AscendException(ErrorCode.TYPE, "Index value must be of type 'int', got '%s'", indexVal.type());
		}
		parser.getEnv().mapArrayIndexToValue(array, (int) indexVal.value(), value);
		devOutput("%s[%d] = %s", varName, (int) indexVal.value(), value);
	}

}
