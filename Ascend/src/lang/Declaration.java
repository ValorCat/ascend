package lang;

import cmd.*;
import util.CommandBuilder;
import util.TokenArray;
import util.TokenType;

public class Declaration implements Statement {

	public boolean isValid(TokenArray statement) {
		return statement.size() == 2 &&
				TokenType.isIdentifier(statement.get(0)) &&
				TokenType.isIdentifier(statement.get(1));
	}

	public Command[] apply(TokenArray statement) {
		CommandBuilder commands = new CommandBuilder();
		commands.add(new RegisterCommand(statement.get(1), statement.get(0)));
		return commands.finish();
	}

}
