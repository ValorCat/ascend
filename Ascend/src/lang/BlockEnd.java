package lang;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import cmd.*;
import cmd.control.CloseStructCommand;
import util.TokenArray;

public class BlockEnd implements Statement {
	
	@Override
	public Matcher getMatcher(TokenArray statement) {
		//return statement.size() == 1 && statement.get(0).equals("end");
		return statement.match("end");
	}

	@Override
	public void parseStatement(MatchResult match) {}

	@Override
	public void buildCommands(List<Command> commands) {
		commands.add(new CloseBlockCommand());
		commands.add(new CloseStructCommand());
	}

}
