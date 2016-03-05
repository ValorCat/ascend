package lang;

import cmd.*;
import interpreter.Evaluator;
import util.CommandBuilder;
import util.AscendException;
import util.ErrorCode;
import util.TokenArray;
import util.TokenType;
import util.Value;

public class Initialization implements Statement {

	public boolean isValid(TokenArray statement) {
		return statement.size() >= 4 &&
				TokenType.isIdentifier(statement.get(0)) &&
				TokenType.isIdentifier(statement.get(1)) &&
				TokenType.isAssignmentOperator(statement.get(2));
	}

	public Command[] apply(TokenArray statement) {
		if (!statement.get(2).equals("=")) {
			throw new AscendException(ErrorCode.REFERENCE, "Cannot update uninitialized variable '" + statement.get(1) + "'");
		}
		CommandBuilder commands = new CommandBuilder();
		commands.add(new RegisterCommand(statement.get(1), statement.get(0)));
		TokenArray exp = statement.range(3);
		Value value = Evaluator.getConstValue(exp);
		if (value != null) {
			// value is constant
			if (!value.isA(statement.get(0))) {
				throw new AscendException(ErrorCode.TYPE, "Cannot assign type '" + value.type() + "' to " + statement.get(0) + " variable");
			}
			commands.add(new SetCommand(statement.get(1), value));
		} else {
			// value is variable
			commands.add(new SetVarCommand(statement.get(1), exp));
		}
		return commands.finish();
	}
	
}
