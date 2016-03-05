package util;

import java.util.ArrayList;

import cmd.Command;

public class CommandBuilder {
	
	private ArrayList<Command> list;
	
	public CommandBuilder() {
		this.list = new ArrayList<Command>();
	}
	
	public void add(Command command) {
		list.add(command);
	}
	
	public Command[] finish() {
		return list.toArray(new Command[0]);
	}

}
