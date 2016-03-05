package lang;

import cmd.*;
import interpreter.Evaluator;
import util.AscendException;
import util.CommandBuilder;
import util.ErrorCode;
import util.TokenArray;
import util.Value;

public class ForLoop implements Statement {

	public boolean isValid(TokenArray statement) {
		return statement.size() >= 9 &&
				statement.get(0).equals("for") &&
				statement.get(1).equals("(") &&
				statement.get(4).equals("=") &&
				statement.get(-1).equals(")");
	}
	
	public Command[] apply(TokenArray statement) {
		int byClauseStart = getByClauseIndex(statement);
		TokenArray iterStartToEnd = statement.range(5, byClauseStart > -1 ? byClauseStart : -1);
		TokenArray[] expressions = Evaluator.divideExprList(iterStartToEnd, ",");
		if (expressions.length != 2) {
			throw new AscendException(ErrorCode.SYNTAX, "Invalid for loop construction");
		}
		String varType = statement.get(2);
		String varName = statement.get(3);
		TokenArray initExpr = expressions[0];
		TokenArray finalExpr = expressions[1];
		TokenArray updateExpr = byClauseStart > -1 ? statement.range(byClauseStart + 1, -1) : new TokenArray("1");
		CommandBuilder commands = new CommandBuilder();
		commands.add(new RegisterCommand(varName, varType));
		Value initValue = Evaluator.getConstValue(initExpr);
		if (initValue != null) {
			commands.add(new SetCommand(varName, initValue));
		} else {
			commands.add(new SetVarCommand(varName, initExpr));
		}
		commands.add(new OpenForLoopCommand(finalExpr, updateExpr, varName));
		commands.add(new OpenStructCommand());
		commands.add(new OpenBlockCommand());
		return commands.finish();
	}
	
	private int getByClauseIndex(TokenArray statement) {
		for (int i = 8; i < statement.size(); i++) {
			if (statement.get(i).equals("by")) {
				return i;
			}
		}
		return -1;
	}
	
}
