package cmd.control;

import cmd.Command;
import control.ForControl;
import interpreter.Evaluator;
import interpreter.Parser;
import util.AscendException;
import util.ErrorCode;
import util.TokenArray;
import util.Value;

public class SetupForLoopCommand extends Command {

	private String varName;
	private TokenArray finalExpr;
	private TokenArray updateExpr;
	
	public SetupForLoopCommand(TokenArray finalExpr, TokenArray updateExpr, String varName) {
		super("SETUP_FOR_LOOP");
		this.finalExpr = finalExpr;
		this.updateExpr = updateExpr;
		this.varName = varName;
	}

	@Override
	public void onExecute() {
		devOutput("%s to %s by %s -> ", varName, finalExpr, updateExpr);
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
			throw new AscendException(ErrorCode.TYPE, "Update expression evaluated to type %s, expected number", updateValue.type());
		}
		ForControl control = new ForControl(finalExpr, updateValue, varName, reverse);
		boolean result = control.evaluate();
		devOutput(result);
		Parser.getParser().addFlowControl(control);
	}

}
