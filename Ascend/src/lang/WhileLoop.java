package lang;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import cmd.*;
import cmd.control.OpenStructCommand;
import cmd.control.SetupWhileLoopCommand;
import interpreter.Tokenizer;
import util.TokenArray;

public class WhileLoop implements Statement {
	
	private TokenArray expr;

	@Override
	public Matcher getMatcher(TokenArray statement) {
		/*return statement.size() >= 4 &&
				statement.get(0).equals("while") &&
				statement.get(1).equals("(") &&
				statement.get(-1).equals(")");*/
		return statement.match("while \\( (.*) \\)");
	}

	@Override
	public void parseStatement(MatchResult match) {
		expr = Tokenizer.retokenize(match.group(1));
	}

	@Override
	public void buildCommands(List<Command> commands) {
		commands.add(new SetupWhileLoopCommand(expr));
		commands.add(new OpenStructCommand());
		commands.add(new OpenBlockCommand());
	}

}
