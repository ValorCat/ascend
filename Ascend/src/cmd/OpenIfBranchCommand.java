package cmd;

import interpreter.Parser;
import interpreter.Parser.FlowControl;
import interpreter.Parser.IfControl;
import util.AscendException;
import util.ErrorCode;
import util.TokenArray;

public class OpenIfBranchCommand extends Command {

	private TokenArray branchExpr;
	
	public OpenIfBranchCommand(TokenArray branchExpression) {
		this.branchExpr = branchExpression;
	}
	
	@Override
	public String getName() {
		return "OPEN_IF_BRANCH";
	}

	@Override
	public void execute(Parser parser) {
		devOutput("%s -> ", branchExpr);
		FlowControl topControl = parser.getTopControl();
		if (!(topControl instanceof IfControl)) {
			throw new AscendException(ErrorCode.SYNTAX, "Missing end statement before else block");
		}
		IfControl control = (IfControl) topControl;
		boolean result = control.evaluate(branchExpr);
		devOutput(result);
	}

}
