package cmd.control;

import cmd.Command;
import control.FlowControl;
import control.IfControl;
import interpreter.Parser;
import util.AscendException;
import util.ErrorCode;
import util.TokenArray;

public class SetupIfBranchCommand extends Command {

	private TokenArray branchExpr;
	
	public SetupIfBranchCommand(TokenArray branchExpression) {
		super("SETUP_IF_BRANCH");
		this.branchExpr = branchExpression;
	}
	
	@Override
	public void onExecute() {
		devOutput("%s -> ", branchExpr);
		FlowControl topControl = Parser.getParser().getTopControl();
		if (!(topControl instanceof IfControl)) {
			throw new AscendException(ErrorCode.SYNTAX, "Missing end statement before else block");
		}
		IfControl control = (IfControl) topControl;
		boolean result = control.evaluate(branchExpr);
		devOutput(result);
	}

}
