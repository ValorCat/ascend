package interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import cmd.Command;
import lang.*;
import util.AscendException;
import util.ErrorCode;
import util.TokenArray;

public class Compiler {
	
	private static final Statement[] STATEMENT_CLASSES = {
			new BlockEnd(),
			new DoStatement(),
			new DeleteVariable(),
			
			new ForLoop(),
			new IfThenBranch(),
			new IfBranch(),
			new WhileLoop(),
			
			new AssignIndex(),
			new AssignVariable(),
			new InitializeVariable(),
			new DeclareVariable(),
			new IncrementVariable(),
			
			new CallProcedure(),
	};
	
	public static class CommandData {
		
		private List<Command> commands;
		private int[] levels;
		
		public CommandData(List<Command> commands, int[] levels) {
			this.commands = commands;
			this.levels = levels;
		}
		
		public List<Command> getCommands() {
			return commands;
		}
		
		public int[] getBlockLevels() {
			return levels;
		}
		
	}
	
	public static CommandData compile(TokenArray tokenStream) {
		List<TokenArray> statements = getStatements(tokenStream);
		if (Ascend.debugMode) {
			System.out.print("Statements: ");
			for (TokenArray statement : statements) {
				System.out.print(statement + "  ");
			}
			System.out.println();
		}
		
		List<Command> commands = getCommands(statements);
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
	
	public static List<TokenArray> getStatements(TokenArray tokenStream) {
		List<TokenArray> statements = new ArrayList<>();
		List<String> currentStatement = new ArrayList<>();
		for (String token : tokenStream) {
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
		return statements;
	}
	
	public static List<Command> getCommands(List<TokenArray> statements) {
		List<Command> commands = new ArrayList<>();
		for (TokenArray statement : statements) {
			for (Command command : getCommands(statement)) {
				commands.add(command);
			}
		}
		return commands;
	}
	
	public static List<Command> getCommands(TokenArray statement) {
		Statement statementType = getStatementClass(statement);
		List<Command> commands = new ArrayList<Command>();
		statementType.buildCommands(commands);
		return commands;
	}
	
	private static Statement getStatementClass(TokenArray statement) {
		if (statement.size() > 0) {
			for (Statement type : STATEMENT_CLASSES) {
				Matcher matcher = type.getMatcher(statement);
				if (matcher != null && matcher.matches()) {
					type.parseStatement(matcher.toMatchResult());;
					return type;
				}
			}
		}
		throw new AscendException(ErrorCode.SYNTAX, "Invalid statement construction: %s", statement.toCleanString());
	}
	
	private static int[] getBlockLevels(List<Command> commands) {
		int length = commands.size();
		int[] levels = new int[length];
		boolean bumpNextLeft = false;
		for (int i = 0; i < length; i++) {
			int lastLevel = i > 0 ? levels[i - 1] : 0;
			if (bumpNextLeft) {
				lastLevel--;
				bumpNextLeft = false;
			}
			switch (commands.get(i).getName()) {
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
