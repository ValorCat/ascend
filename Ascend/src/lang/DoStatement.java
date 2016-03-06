package lang;
import cmd.*;
import cmd.control.OpenDoStructCommand;
import cmd.control.OpenStructCommand;
import util.CommandBuilder;
import util.TokenArray;

public class DoStatement implements Statement {
	
	public boolean isValid(TokenArray statement) {
		return statement.size() == 1 && statement.get(0).equals("do");
	}
	
	public Command[] apply(TokenArray statement) {
		CommandBuilder commands = new CommandBuilder();
		commands.add(new OpenDoStructCommand());
		commands.add(new OpenStructCommand());
		commands.add(new OpenBlockCommand());
		return commands.finish();
	}
	
}
