package lang;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import cmd.*;
import cmd.index.IndexCompoundSetCommand;
import cmd.index.IndexCompoundSetVarCommand;
import cmd.index.IndexSetCommand;
import cmd.index.IndexSetVarCommand;
import interpreter.Evaluator;
import interpreter.Tokenizer;
import util.TokenArray;
import util.Value;

public class AssignIndex implements Statement {
	
	private String varName;
	private TokenArray index;
	private String operator;
	private TokenArray expr;
	private Value value;

	@Override
	public Matcher getMatcher(TokenArray statement) {
		/*return statement.size() >= 6 &&
				TokenType.isIdentifier(statement.get(0)) &&
				statement.get(1).equals("[");*/
		return statement.match("({Ident}) \\[ (.*) \\] ({Equals}) (.*)");
	}

	@Override
	public void parseStatement(MatchResult match) {
		varName = match.group(1);
		index = Tokenizer.retokenize(match.group(2));
		operator = match.group(3);
		expr = Tokenizer.retokenize(match.group(4));
		value = Evaluator.getConstValue(expr);
	}

	@Override
	public void buildCommands(List<Command> commands) {
		if (value != null) {
			buildWithConstantValue(commands);
		} else {
			buildWithVariableValue(commands);
		}
	}
	
	private void buildWithConstantValue(List<Command> commands) {
		if (operator.equals("=")) {
			commands.add(new IndexSetCommand(varName, index, value));
		} else {
			commands.add(new IndexCompoundSetCommand(varName, index, value, operator));
		}
	}
	
	private void buildWithVariableValue(List<Command> commands) {
		if (operator.equals("=")) {
			commands.add(new IndexSetVarCommand(varName, index, expr));
		} else {
			commands.add(new IndexCompoundSetVarCommand(varName, index, expr, operator));
		}
	}

}
