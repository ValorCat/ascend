package cmd.control;

import cmd.Command;
import control.IfControl;
import interpreter.Parser;
import util.TokenArray;

public class SetupIfControlCommand extends Command {

	private TokenArray branchExpr;
	
	public SetupIfControlCommand(TokenArray branchExpression) {
		super("SETUP_IF_CNTRL");
		this.branchExpr = branchExpression;
	}
	
	@Override
	public void onExecute() {
		devOutput("%s -> ", branchExpr);
		IfControl control = new IfControl();
		boolean result = control.evaluate(branchExpr);
		devOutput(result);
		Parser.getParser().addFlowControl(control);
	}

}
