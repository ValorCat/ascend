package cmd.index;

import cmd.Command;
import interpreter.Evaluator;
import interpreter.Parser;
import util.AscendException;
import util.ErrorCode;
import util.TokenArray;
import util.Value;

public class IndexSetVarCommand extends Command {

	private String varName;
	private TokenArray index;
	private TokenArray varValue;
	
	public IndexSetVarCommand(String varName, TokenArray index, TokenArray varValue) {
		super("SEQ_SET_VAR");
		this.varName = varName;
		this.index = index;
		this.varValue = varValue;
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
		Value value = Evaluator.evaluate(varValue);
		devOutput("%s[%d] = %s", varName, (int) indexVal.value(), varValue);
		parser.getEnv().mapArrayIndexToValue(array, (int) indexVal.value(), value);
		devOutput(" -> %s", value);
	}

}
