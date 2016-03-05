package lang;

import cmd.*;
import util.CommandBuilder;
import util.TokenArray;

public class BlockEnd implements Statement {
	
	public boolean isValid(TokenArray statement) {
		return statement.size() == 1 && statement.get(0).equals("end");
	}
	
	public Command[] apply(TokenArray statement) {
		CommandBuilder commands = new CommandBuilder();
		commands.add(new CloseBlockCommand());
		commands.add(new CloseStructCommand());
		return commands.finish();
	}

}
