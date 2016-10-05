package lang;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import cmd.*;
import cmd.control.SetupForLoopCommand;
import cmd.control.OpenStructCommand;
import cmd.var.RegisterCommand;
import cmd.var.SetCommand;
import cmd.var.SetVarCommand;
import interpreter.Evaluator;
import interpreter.Tokenizer;
import util.AscendException;
import util.ErrorCode;
import util.TokenArray;
import util.Value;

public class ForLoop implements Statement {
	
	private String iterType;
	private String iterName;
	private TokenArray initExpr;
	private Value initValue;
	private TokenArray termExpr;
	private TokenArray updateExpr;
	
	@Override
	public Matcher getMatcher(TokenArray statement) {
		/*return statement.size() >= 9 &&
				statement.get(0).equals("for") &&
				statement.get(1).equals("(") &&
				statement.get(4).equals("=") &&
				statement.get(-1).equals(")");*/
		return statement.match("for \\( ({Type}) ({Ident}) ({Equals}) (.*) \\)");
	}

	@Override
	public void parseStatement(MatchResult match) {
		if (!match.group(3).equals("="))
			throw new AscendException(ErrorCode.REFERENCE, "Expected initializer, got %s", match.group(3));
		try {
			iterType = match.group(1);
			iterName = match.group(2);
			TokenArray expr = Tokenizer.retokenize(match.group(4));
			TokenArray[] bounds = Evaluator.divideExprList(expr, ",");
			initExpr = bounds[0];
			initValue = Evaluator.getConstValue(initExpr);
			termExpr = bounds[1];
			int byClauseStart = getByClauseIndex(expr);
			updateExpr = byClauseStart > -1 ? expr.range(byClauseStart + 1, -1) : new TokenArray("1");
		} catch (IndexOutOfBoundsException e) {
			throw new AscendException(ErrorCode.SYNTAX, "Malformed 'for' loop construction");
		}
	}

	@Override
	public void buildCommands(List<Command> commands) {
		commands.add(new RegisterCommand(iterName, iterType));
		if (initValue != null)
			commands.add(new SetCommand(iterName, initValue));
		else
			commands.add(new SetVarCommand(iterName, initExpr));
		commands.add(new SetupForLoopCommand(termExpr, updateExpr, iterName));
		commands.add(new OpenStructCommand());
		commands.add(new OpenBlockCommand());
	}
	
	private static int getByClauseIndex(TokenArray expr) {
		for (int i = 0; i < expr.size(); i++) {
			if (expr.get(i).equals("by")) {
				return i;
			}
		}
		return -1;
	}

}
