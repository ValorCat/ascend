package lang;

import cmd.*;
import cmd.var.CompoundSetCommand;
import util.CommandBuilder;
import util.TokenArray;
import util.TokenType;
import util.Value;

public class Increment implements Statement {
	
	public boolean isValid(TokenArray statement) {
		return statement.size() == 2 &&
				TokenType.isIdentifier(statement.get(0)) &&
				(statement.get(1).equals("++") || statement.get(1).equals("--"));
	}
	
	public Command[] apply(TokenArray statement) {
		CommandBuilder commands = new CommandBuilder();
		String operator = statement.get(1).substring(0, 1);
		commands.add(new CompoundSetCommand(statement.get(0), new Value("int", 1), operator));
		return commands.finish();
	}
	
}
