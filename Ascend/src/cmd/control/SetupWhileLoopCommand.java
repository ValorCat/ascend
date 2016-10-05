package cmd.control;

import cmd.Command;
import control.WhileControl;
import interpreter.Parser;
import util.TokenArray;

public class SetupWhileLoopCommand extends Command {

	private TokenArray loopExpr;
	
	public SetupWhileLoopCommand(TokenArray loopExpr) {
		super("SETUP_WHILE_LOOP");
		this.loopExpr = loopExpr;
	}

	@Override
	public void onExecute() {
		devOutput("%s -> ", loopExpr);
		WhileControl control = new WhileControl(loopExpr);
		boolean result = control.evaluate();
		devOutput(result);
		Parser.getParser().addFlowControl(control);
	}

}
