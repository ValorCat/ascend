package lang;

import cmd.*;
import cmd.control.OpenStructCommand;
import cmd.control.OpenWhileLoopCommand;
import util.CommandBuilder;
import util.TokenArray;

public class WhileLoop implements Statement {
	
	public boolean isValid(TokenArray statement) {
		return statement.size() >= 4 &&
				statement.get(0).equals("while") &&
				statement.get(1).equals("(") &&
				statement.get(-1).equals(")");
	}
	
	public Command[] apply(TokenArray statement) {
		TokenArray exp = statement.range(2, -1);
		CommandBuilder commands = new CommandBuilder();
		commands.add(new OpenWhileLoopCommand(exp));
		commands.add(new OpenStructCommand());
		commands.add(new OpenBlockCommand());
		return commands.finish();
	}

}
