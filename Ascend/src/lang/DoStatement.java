package lang;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import cmd.*;
import cmd.control.SetupDoControlCommand;
import cmd.control.OpenStructCommand;
import util.TokenArray;

public class DoStatement implements Statement {

	@Override
	public Matcher getMatcher(TokenArray statement) {
		//return statement.size() == 1 && statement.get(0).equals("do");
		return statement.match("do");
	}

	@Override
	public void parseStatement(MatchResult match) {}

	@Override
	public void buildCommands(List<Command> commands) {
		commands.add(new SetupDoControlCommand());
		commands.add(new OpenStructCommand());
		commands.add(new OpenBlockCommand());
	}
	
}
