package lang;

import cmd.*;
import cmd.var.UnregisterCommand;
import util.CommandBuilder;
import util.TokenArray;
import util.TokenType;

public class Deletion implements Statement {

	public boolean isValid(TokenArray statement) {
		return statement.size() == 2 &&
				statement.get(0).equals("del") &&
				TokenType.isIdentifier(statement.get(1));
	}

	public Command[] apply(TokenArray statement) {
		CommandBuilder commands = new CommandBuilder();
		commands.add(new UnregisterCommand(statement.get(1)));
		return commands.finish();
	}

}
