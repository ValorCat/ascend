package cmd.control;

import cmd.Command;
import interpreter.Evaluator;
import interpreter.Parser;
import interpreter.Parser.ForControl;
import util.AscendException;
import util.ErrorCode;
import util.TokenArray;
import util.Value;

public class OpenForLoopCommand extends Command {

	private String varName;
	private TokenArray finalExpr;
	private TokenArray updateExpr;
	
	public OpenForLoopCommand(TokenArray finalExpr, TokenArray updateExpr, String varName) {
		this.finalExpr = finalExpr;
		this.updateExpr = updateExpr;
		this.varName = varName;
	}

	@Override
	public String getName() {
		return "OPEN_FOR_STRUCT";
	}

	@Override
	public void execute(Parser parser) {
		devOutput("%s -> %s by %s -> ", varName, finalExpr, updateExpr);
		Value updateValue = Evaluator.evaluate(updateExpr);
		boolean reverse;
		switch (updateValue.type()) {
		case "int":
			reverse = (int) updateValue.value() < 0;
			break;
		case "float":
			reverse = (double) updateValue.value() < 0;
			break;
		default:
			throw new AscendException(ErrorCode.TYPE, "Update expression evaluated to type " + updateValue.type() + ", expected number");
		}
		ForControl control = parser.new ForControl(finalExpr, updateValue, varName, reverse);
		boolean result = control.evaluate();
		devOutput(result);
		parser.addFlowControl(control);
	}

}
