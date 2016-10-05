package cmd;

import java.util.List;

import interpreter.Evaluator;
import util.AscendException;
import util.ErrorCode;
import util.TokenArray;
import util.Value;

public class IfThenCommand extends Command {

	private TokenArray expr;
	private List<Command> commands;

	public IfThenCommand(TokenArray expr, List<Command> commands) {
		super("IF_THEN");
		this.expr = expr;
		this.commands = commands;
	}

	@Override
	public void onExecute() {
		Value result = Evaluator.evaluate(expr);
		if (!result.isA("bool")) {
			throw new AscendException(ErrorCode.TYPE, "Conditional expression evaluated to '" + result.type() + "', expected 'bool'");
		}
		boolean resultAsBool = (boolean) result.value();
		if (resultAsBool) {
			for (Command command : commands) {
				command.onExecute();
			}
		}
	}
	
}
