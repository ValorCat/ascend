package lang;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import cmd.*;
import cmd.var.UnregisterCommand;
import util.TokenArray;

public class DeleteVariable implements Statement {

	private String varName;
	
	@Override
	public Matcher getMatcher(TokenArray statement) {
		/*return statement.size() == 2 &&
				statement.get(0).equals("del") &&
				TokenType.isIdentifier(statement.get(1));*/
		return statement.match("del ({Ident})");
	}

	@Override
	public void parseStatement(MatchResult match) {
		varName = match.group(1);
	}

	@Override
	public void buildCommands(List<Command> commands) {
		commands.add(new UnregisterCommand(varName));
	}

}
