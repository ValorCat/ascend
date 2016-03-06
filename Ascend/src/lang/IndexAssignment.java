package lang;
import cmd.*;
import cmd.index.IndexCompoundSetCommand;
import cmd.index.IndexCompoundSetVarCommand;
import cmd.index.IndexSetCommand;
import cmd.index.IndexSetVarCommand;
import interpreter.Evaluator;
import util.AscendException;
import util.CommandBuilder;
import util.ErrorCode;
import util.TokenArray;
import util.TokenType;
import util.Value;

public class IndexAssignment implements Statement {
	
	public boolean isValid(TokenArray statement) {
		return statement.size() >= 6 &&
				TokenType.isIdentifier(statement.get(0)) &&
				statement.get(1).equals("[");
	}
	
	public Command[] apply(TokenArray statement) {
		int endBracket = Evaluator.findMatchingWrapper(statement, 1);
		String operToken = statement.get(endBracket + 1);
		if (!TokenType.isAssignmentOperator(operToken)) {
			throw new AscendException(ErrorCode.SYNTAX, "Expected assignment operator, got '%s'", operToken);
		}
		
		String varName = statement.get(0);
		TokenArray index = statement.range(2, endBracket);
		String operator = operToken.substring(0, 1);
		TokenArray expr = statement.range(endBracket + 2);
		Value value = Evaluator.getConstValue(expr);
		
		CommandBuilder commands = new CommandBuilder();
		if (value != null) {
			constValue(commands, varName, index, value, operator);
		} else {
			varValue(commands, varName, index, expr, operator);
		}
		return commands.finish();
	}
	
	private void constValue(CommandBuilder commands, String varName, TokenArray index, Value value, String operator) {
		if (operator.equals("=")) {
			commands.add(new IndexSetCommand(varName, index, value));
		} else {
			commands.add(new IndexCompoundSetCommand(varName, index, value, operator));
		}
	}
	
	private void varValue(CommandBuilder commands, String varName, TokenArray index, TokenArray value, String operator) {
		if (operator.equals("=")) {
			commands.add(new IndexSetVarCommand(varName, index, value));
		} else {
			commands.add(new IndexCompoundSetVarCommand(varName, index, value, operator));
		}
	}
	
}
