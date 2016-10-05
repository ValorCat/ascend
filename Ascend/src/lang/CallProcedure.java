package lang;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import cmd.*;
import interpreter.Evaluator;
import interpreter.Tokenizer;
import util.TokenArray;

public class CallProcedure implements Statement {

	private String procName;
	private TokenArray expr;

	@Override
	public Matcher getMatcher(TokenArray statement) {
		/*return statement.size() >= 3 &&
				TokenType.isIdentifier(statement.get(0)) &&
				statement.get(1).equals("(") &&
				statement.get(-1).equals(")");*/
		return statement.match("({Ident}) \\( (.*?)\\s?\\)");
	}


	@Override
	public void parseStatement(MatchResult match) {
		procName = match.group(1);
		expr = Tokenizer.retokenize(match.group(2));
	}


	@Override
	public void buildCommands(List<Command> commands) {
		if (expr.size() == 0 || expr.get(0).equals("")) {
			commands.add(new InvokeCommand(procName, null));
		} else {
			TokenArray[] args = Evaluator.divideExprList(expr, ",");
			commands.add(new InvokeCommand(procName, args));
		}
	}
	
}
