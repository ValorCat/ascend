package lang;

import cmd.*;
import util.CommandBuilder;
import util.TokenArray;

public class IfStatement implements Statement {
	
	public boolean isValid(TokenArray statement) {
		if (statement.size() == 1) {
			return statement.get(0).equals("else");
		}
		return statement.size() >= 4 &&
				(statement.get(0).equals("if") || statement.get(0).equals("else")) &&
				statement.get(1).equals("(") &&
				statement.get(-1).equals(")");
	}
	
	public Command[] apply(TokenArray statement) {
		CommandBuilder commands = new CommandBuilder();
		if (statement.size() == 1) {
			elseClause(commands);
		} else {
			TokenArray expr = statement.range(2, -1);
			if (statement.get(0).equals("else")) {
				elseIfClause(commands, expr);
			} else {
				ifClause(commands, expr);
			}
		}
		commands.add(new OpenBlockCommand());
		return commands.finish();
	}
	
	private void ifClause(CommandBuilder commands, TokenArray expr) {
		commands.add(new OpenIfStructCommand(expr));
		commands.add(new OpenStructCommand());
	}
	
	private void elseIfClause(CommandBuilder commands, TokenArray expr) {
		commands.add(new CloseBlockCommand());
		commands.add(new OpenIfBranchCommand(expr));
	}
	
	private void elseClause(CommandBuilder commands) {
		commands.add(new CloseBlockCommand());
		commands.add(new OpenIfBranchCommand(new TokenArray("true")));
	}

}
