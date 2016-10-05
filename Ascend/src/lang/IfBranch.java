package lang;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import cmd.*;
import cmd.control.SetupIfBranchCommand;
import cmd.control.SetupIfControlCommand;
import interpreter.Tokenizer;
import cmd.control.OpenStructCommand;
import util.TokenArray;

public class IfBranch implements Statement {

	static enum BranchType { IF, ELSE_IF, ELSE }
	
	private BranchType branchType;
	private TokenArray expr;

	@Override
	public Matcher getMatcher(TokenArray statement) {
		int length = statement.size();
		boolean isIfBranch = statement.startsWith("if");
		if (length == 1) {
			branchType = BranchType.ELSE;
			return statement.match("else");
		} else if (length > 1 && (isIfBranch || statement.startsWith("else"))) {
			branchType = isIfBranch ? BranchType.IF : BranchType.ELSE_IF;
			return statement.range(1).match("\\( (.*) \\)");
		} else {
			return null;
		}
	}

	@Override
	public void parseStatement(MatchResult match) {
		if (branchType == BranchType.ELSE) {
			expr = new TokenArray("true");
		} else {
			expr = Tokenizer.retokenize(match.group(1));
		}
	}

	@Override
	public void buildCommands(List<Command> commands) {
		switch (branchType) {
		case IF:
			commands.add(new SetupIfControlCommand(expr));
			commands.add(new OpenStructCommand());
			break;
		case ELSE_IF: case ELSE:
			commands.add(new CloseBlockCommand());
			commands.add(new SetupIfBranchCommand(expr));
			break;
		}
		commands.add(new OpenBlockCommand());
	}

}
