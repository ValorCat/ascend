package lang;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import cmd.*;
import cmd.var.CompoundSetCommand;
import util.TokenArray;
import util.Value;

public class IncrementVariable implements Statement {
	
	private String varName;
	private String operator;

	@Override
	public Matcher getMatcher(TokenArray statement) {
		/*return statement.size() == 2 &&
				TokenType.isIdentifier(statement.get(0)) &&
				(statement.get(1).equals("++") || statement.get(1).equals("--"));*/
		if (statement.size() == 2) {
			String operator = statement.get(1);
			if (operator.equals("++") || operator.equals("--"))
				return statement.match("({Ident}) (.+)");
		}
		return null;
	}

	@Override
	public void parseStatement(MatchResult match) {
		varName = match.group(1);
		operator = match.group(2);
	}

	@Override
	public void buildCommands(List<Command> commands) {
		commands.add(new CompoundSetCommand(varName, new Value("int", 1), operator));
	}
	
}
