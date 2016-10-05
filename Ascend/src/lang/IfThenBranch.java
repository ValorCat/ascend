package lang;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import cmd.Command;
import cmd.IfThenCommand;
import interpreter.Compiler;
import interpreter.Tokenizer;
import util.TokenArray;

public class IfThenBranch implements Statement {
	
	private TokenArray expr;
	private TokenArray block;
	
	@Override
	public Matcher getMatcher(TokenArray statement) {
		return statement.match("if \\( (.*) \\) then (.*)");
	}

	@Override
	public void parseStatement(MatchResult match) {
		expr = Tokenizer.retokenize(match.group(1));
		block = Tokenizer.retokenize(match.group(2));
	}

	@Override
	public void buildCommands(List<Command> commands) {
		commands.add(new IfThenCommand(expr, Compiler.getCommands(block)));
	}

}
