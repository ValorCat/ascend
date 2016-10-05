package lang;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import cmd.*;
import cmd.var.*;
import interpreter.Evaluator;
import interpreter.Tokenizer;
import util.TokenArray;
import util.Value;

public class AssignVariable implements Statement {
	
	private String varName;
	private String operator;
	private TokenArray expr;
	private Value value;

	@Override
	public Matcher getMatcher(TokenArray statement) {
		/*return statement.size() >= 3 &&
				TokenType.isIdentifier(statement.get(0)) &&
				TokenType.isAssignmentOperator(statement.get(1));*/
		return statement.match("({Ident}) ({Equals}) (.*)");
	}

	@Override
	public void parseStatement(MatchResult match) {
		varName = match.group(1);
		operator = match.group(2);
		expr = Tokenizer.retokenize(match.group(3));
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
			commands.add(new SetCommand(varName, value));
		} else {
			commands.add(new CompoundSetCommand(varName, value, operator));
		}
	}
	
	private void buildWithVariableValue(List<Command> commands) {
		if (operator.equals("=")) {
			commands.add(new SetVarCommand(varName, expr));
		} else {
			commands.add(new CompoundSetVarCommand(varName, expr, operator));
		}
	}
	
}
