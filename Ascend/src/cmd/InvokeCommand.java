package cmd;

import java.util.ArrayList;
import java.util.Arrays;

import interpreter.Evaluator;
import interpreter.Parser;
import util.AscendException;
import util.ErrorCode;
import util.TokenArray;
import util.Value;

public class InvokeCommand extends Command {

	private String varName;
	private TokenArray[] arguments;
	
	public InvokeCommand(String varName, TokenArray[] arguments) {
		this.varName = varName;
		this.arguments = arguments;
	}

	@Override
	public String getName() {
		return "INVOKE";
	}

	@Override
	public void execute(Parser parser) {
		devOutput("%s %s -> [", varName, Arrays.toString(arguments));
		Value varValue = parser.getEnv().getValueFromName(varName);
		if (varValue.isA("proc")) {
			ArrayList<Value> argValues = new ArrayList<Value>();
			if (arguments != null) {
				for (int i = 0; i < arguments.length; i++) {
					Value arg = Evaluator.evaluate(arguments[i]);
					argValues.add(arg);
					devOutput("%s,", arg);
				}
			}
			devOutput("]");
			if (varValue.value() instanceof String) {
				// built-in procedure
				Value[] argValuesArray = argValues.toArray(new Value[0]);
				Parser.callExecutable(varName, varValue, argValuesArray);
			}
		} else if (varValue.isA("func")) {
			throw new AscendException(ErrorCode.TYPE, "Cannot call function '" + varName + "' without assigning return value");
		} else {
			throw new AscendException(ErrorCode.TYPE, "Variable '" + varName + "' of type '" + varValue.type() + "' is not callable");
		}
	}

}
