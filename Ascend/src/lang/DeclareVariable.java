package lang;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import cmd.*;
import cmd.var.RegisterCommand;
import util.TokenArray;

public class DeclareVariable implements Statement {

	private String varType;
	private String varName;

	@Override
	public Matcher getMatcher(TokenArray statement) {
		/*return statement.size() == 2 &&
				TokenType.isIdentifier(statement.get(0)) &&
				TokenType.isIdentifier(statement.get(1));*/
		return statement.match("({Type}) ({Ident})");
	}

	@Override
	public void parseStatement(MatchResult match) {
		varType = match.group(1);
		varName = match.group(2);
	}

	@Override
	public void buildCommands(List<Command> commands) {
		commands.add(new RegisterCommand(varName, varType));
	}

}
