package cmd;

import interpreter.Parser;

public class OpenBlockCommand extends Command {

	@Override
	public String getName() {
		return "OPEN_BLOCK";
	}

	@Override
	public void execute(Parser parser) {
		// used by parser to open a block
	}

}
