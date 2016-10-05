package lang;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import cmd.*;
import cmd.var.RegisterCommand;
import cmd.var.SetCommand;
import cmd.var.SetVarCommand;
import interpreter.Evaluator;
import interpreter.Tokenizer;
import util.AscendException;
import util.ErrorCode;
import util.Operation;
import util.TokenArray;
import util.Value;

public class InitializeVariable implements Statement {

	private String varType;
	private String varName;
	private TokenArray expr;
	private Value value;

	@Override
	public Matcher getMatcher(TokenArray statement) {
		/*return statement.size() >= 4 &&
				TokenType.isIdentifier(statement.get(0)) &&
				TokenType.isIdentifier(statement.get(1)) &&
				TokenType.isAssignmentOperator(statement.get(2));*/
		return statement.match("({Type}) ({Ident}) ({Equals}) (.*)");
	}

	@Override
	public void parseStatement(MatchResult match) {
		String operator = match.group(3);
		if (!operator.equals("="))
			throw new AscendException(ErrorCode.REFERENCE, "Expected initializer, got %s", operator);
		varType = match.group(1);
		varName = match.group(2);
		expr = Tokenizer.retokenize(match.group(4));
		value = Evaluator.getConstValue(expr);
	}

	@Override
	public void buildCommands(List<Command> commands) {
		commands.add(new RegisterCommand(varName, varType));
		if (value != null) {
			if (!value.isA(varType)) {
				Operation.typeCast(value, new Value("type", varType));
				value = Operation.poll();
			}
			commands.add(new SetCommand(varName, value));
		} else {
			commands.add(new SetVarCommand(varName, expr));
		}
	}
	
}
