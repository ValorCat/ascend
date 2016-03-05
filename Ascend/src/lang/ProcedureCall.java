package lang;

import cmd.*;
import interpreter.Evaluator;
import util.CommandBuilder;
import util.TokenArray;
import util.TokenType;

public class ProcedureCall implements Statement {

	public boolean isValid(TokenArray statement) {
		return statement.size() >= 3 &&
				TokenType.isIdentifier(statement.get(0)) &&
				statement.get(1).equals("(") &&
				statement.get(-1).equals(")");
	}
	
	
	public Command[] apply(TokenArray statement) {
		TokenArray expr = statement.range(2, -1);
		CommandBuilder commands = new CommandBuilder();
		if (expr.size() == 0) {
			commands.add(new InvokeCommand(statement.get(0), null));
		} else {
			TokenArray[] paramList = Evaluator.divideExprList(expr, ",");
			commands.add(new InvokeCommand(statement.get(0), paramList));
		}
		return commands.finish();
	}
	
}
