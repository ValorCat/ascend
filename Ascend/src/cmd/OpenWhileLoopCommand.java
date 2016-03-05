package cmd;

import interpreter.Parser;
import interpreter.Parser.WhileControl;
import util.TokenArray;

public class OpenWhileLoopCommand extends Command {

	private TokenArray loopExpr;
	
	public OpenWhileLoopCommand(TokenArray loopExpr) {
		this.loopExpr = loopExpr;
	}

	@Override
	public String getName() {
		return "OPEN_WHILE_STRUCT";
	}

	@Override
	public void execute(Parser parser) {
		devOutput("%s -> ", loopExpr);
		WhileControl control = parser.new WhileControl(loopExpr);
		boolean result = control.evaluate();
		devOutput(result);
		parser.addFlowControl(control);
	}

}
