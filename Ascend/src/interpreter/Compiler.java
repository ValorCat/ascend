package interpreter;

import java.util.ArrayList;

import cmd.Command;
import lang.*;
import util.AscendException;
import util.ErrorCode;
import util.TokenArray;

public class Compiler {
	
	private static final Statement[] statementTypes = new Statement[] {
			new Assignment(),
			new BlockEnd(),
			new Declaration(),
			new Deletion(),
			new DoStatement(),
			new ForLoop(),
			new IfStatement(),
			new Increment(),
			new IndexAssignment(),
			new Initialization(),
			new ProcedureCall(),
			new WhileLoop(),
	};
	
	public static class CommandData {
		
		private Command[] commands;
		private int[] levels;
		
		public CommandData(Command[] commands, int[] levels) {
			this.commands = commands;
			this.levels = levels;
		}
		
		public Command[] getCommands() {
			return commands;
		}
		
		public int[] getBlockLevels() {
			return levels;
		}
		
	}
	
	public CommandData compile(TokenArray tokenStream) {
		TokenArray[] statements = getStatements(tokenStream);
		
		if (Ascend.debugMode) {
			System.out.print("Statements: ");
			for (TokenArray statement : statements) {
				System.out.print(statement + "  ");
			}
			System.out.println();
		}
		
		Command[] commands = getCommands(statements);
		if (Ascend.debugMode) {
			System.out.print("Commands: ");
			for (Command command : commands) {
				System.out.print(command.getName().toUpperCase() + " ");
			}
			System.out.println();
		}
		int[] levels = getBlockLevels(commands);
		/*if (Ascend.debugMode) {
			System.out.print("Levels:");
			for (int level : levels) {
				System.out.print(" " + level);
			}
			System.out.println();
		}*/
		return new CommandData(commands, levels);
	}
	
	private TokenArray[] getStatements(TokenArray tokenStream) {
		ArrayList<TokenArray> statements = new ArrayList<TokenArray>();
		ArrayList<String> currentStatement = new ArrayList<String>();
		while (tokenStream.hasNext()) {
			String token = tokenStream.next();
			if (!token.equals("\n") && !token.equals(";")) {
				currentStatement.add(token);
			} else if (!currentStatement.isEmpty()) {
				statements.add(new TokenArray(currentStatement));
				currentStatement.clear();
			}
		}
		if (!currentStatement.isEmpty()) {
			statements.add(new TokenArray(currentStatement));
		}
		return statements.toArray(new TokenArray[0]);
	}
	
	private Command[] getCommands(TokenArray[] statements) {
		ArrayList<Command> commands = new ArrayList<Command>();
		for (TokenArray statement : statements) {
			Statement statementType = getStatementType(statement);
			if (statementType == null) {
				throw new AscendException(ErrorCode.SYNTAX, "Invalid statement construction");
			}
			for (Command command : statementType.apply(statement)) {
				commands.add(command);
			}
		}
		return commands.toArray(new Command[0]);
	}
	
	private static Statement getStatementType(TokenArray statement) {
		for (Statement type : statementTypes) {
			if (type.isValid(statement)) {
				return type;
			}
		}
		return null;
	}
	
	private int[] getBlockLevels(Command[] commands) {
		int[] levels = new int[commands.length];
		boolean bumpNextLeft = false;
		for (int i = 0; i < commands.length; i++) {
			int lastLevel = i > 0 ? levels[i - 1] : 0;
			if (bumpNextLeft) {
				lastLevel--;
				bumpNextLeft = false;
			}
			switch (commands[i].getName()) {
			case "OPEN_BLOCK":
				levels[i] = lastLevel + 1;
				break;
			case "CLOSE_BLOCK":
				if (lastLevel == 0) {
					throw new AscendException(ErrorCode.SYNTAX, "Misplaced 'end' or 'else' statement");
				}
				levels[i] = lastLevel;
				bumpNextLeft = true;
				break;
			default:
				levels[i] = lastLevel;
			}
		}
		return levels;
	}
	
}
