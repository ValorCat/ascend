package cmd;

import interpreter.Parser;

public class CloseBlockCommand extends Command {
	
	public CloseBlockCommand() {
		super("CLOSE_BLOCK");
	}

	@Override
	public void onExecute() {
		Parser parser = Parser.getParser();
		parser.getEnv().unmapBlockLevel(parser.getBlockLevel());
	}

}
