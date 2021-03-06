package cmd.var;

import cmd.Command;
import interpreter.Parser;
import util.AscendException;
import util.ErrorCode;
import util.Operation;
import util.Value;

public class CompoundSetCommand extends Command {

	private String varName;
	private Value modValue;
	private String operator;
	
	public CompoundSetCommand(String varName, Value modValue, String operator) {
		super("SET_COMP_CONST");
		this.varName = varName;
		this.modValue = modValue;
		this.operator = operator;
	}

	@Override
	public void onExecute() {
		Parser parser = Parser.getParser();
		Value initValue = parser.getEnv().getValueFromName(varName);
		devOutput("%s %s %s %s", varName, initValue, operator, modValue);
		if (!initValue.hasValue()) {
			throw new AscendException(ErrorCode.REFERENCE, "Cannot update uninitialized variable '" + varName + "'");
		}
		Value newValue = Operation.operate(operator, initValue, modValue);
		parser.getEnv().mapNameToValue(varName, newValue);
		devOutput(" -> %s", newValue);
	}

}
