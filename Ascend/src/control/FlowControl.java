package control;

import java.util.List;

import cmd.Command;
import interpreter.Parser;
import util.AscendException;
import util.ErrorCode;

public abstract class FlowControl {
	protected Parser parser;
	protected List<Command> commands;
	protected int[] blockLevels;
	protected int headerBlockLevel;
	protected int headerIndex;
	protected int endIndex;
	protected String controlName;
	
	public FlowControl(String controlName) {
		this.parser = Parser.getParser();
		this.commands = parser.getCommands();
		this.blockLevels = parser.getBlockLevels();
		this.headerBlockLevel = parser.getBlockLevel();
		this.headerIndex = parser.getCommandIndex();
		this.endIndex = findMatchingEndPosition();
		this.controlName = controlName;
	}
	
	public abstract boolean atEnd();
	
	public String getName() {
		return controlName;
	}
	
	private int findMatchingEndPosition() {
		int subControls = 1;
		for (int i = headerIndex + 3; i < commands.size(); i++) {
			switch (commands.get(i).getName()) {
			case "OPEN_STRUCT":
				subControls++;
				break;
			case "CLOSE_STRUCT":
				if (--subControls == 0) {
					return i;
				}
				break;
			}
		}
		throw new AscendException(ErrorCode.SYNTAX, "Missing end statement");
	}
}
