package cmd;

import interpreter.Parser;

public class CloseBlockCommand extends Command {
	
	@Override
	public String getName() {
		return "CLOSE_BLOCK";
	}

	@Override
	public void execute(Parser parser) {
		parser.getEnv().unmapBlockLevel(parser.getBlockLevel());
	}

}
