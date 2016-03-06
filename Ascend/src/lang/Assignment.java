package lang;
import cmd.*;
import cmd.var.CompoundSetCommand;
import cmd.var.CompoundSetVarCommand;
import cmd.var.SetCommand;
import cmd.var.SetVarCommand;
import interpreter.Evaluator;
import util.CommandBuilder;
import util.TokenArray;
import util.TokenType;
import util.Value;

public class Assignment implements Statement {
	
	public boolean isValid(TokenArray statement) {
		return statement.size() >= 3 &&
				TokenType.isIdentifier(statement.get(0)) &&
				TokenType.isAssignmentOperator(statement.get(1));
	}
	
	public Command[] apply(TokenArray statement) {
		String varName = statement.get(0);
		String operator = statement.get(1).substring(0, 1);
		TokenArray expr = statement.range(2);
		Value value = Evaluator.getConstValue(expr);
		
		CommandBuilder commands = new CommandBuilder();
		if (value != null) {
			constValue(commands, varName, value, operator);
		} else {
			varValue(commands, varName, expr, operator);
		}
		return commands.finish();
	}
	
	private void constValue(CommandBuilder commands, String varName, Value value, String operator) {
		if (operator.equals("=")) {
			commands.add(new SetCommand(varName, value));
		} else {
			commands.add(new CompoundSetCommand(varName, value, operator));
		}
	}
	
	private void varValue(CommandBuilder commands, String varName, TokenArray expr, String operator) {
		if (operator.equals("=")) {
			commands.add(new SetVarCommand(varName, expr));
		} else {
			commands.add(new CompoundSetVarCommand(varName, expr, operator));
		}
	}
	
}
