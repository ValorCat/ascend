package lang;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import cmd.Command;
import util.TokenArray;

public interface Statement {
	
	Matcher getMatcher(TokenArray statement);
	
	void parseStatement(MatchResult match);
	
	void buildCommands(List<Command> commands);
	
}
