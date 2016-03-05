package cmd;

import interpreter.Parser;
import interpreter.Parser.IfControl;
import util.TokenArray;

public class OpenIfStructCommand extends Command {

	private TokenArray branchExpr;
	
	public OpenIfStructCommand(TokenArray branchExpression) {
		this.branchExpr = branchExpression;
	}
	
	@Override
	public String getName() {
		return "OPEN_IF_STRUCT";
	}

	@Override
	public void execute(Parser parser) {
		devOutput("%s -> ", branchExpr);
		IfControl control = parser.new IfControl();
		boolean result = control.evaluate(branchExpr);
		devOutput(result);
		parser.addFlowControl(control);
	}

}
