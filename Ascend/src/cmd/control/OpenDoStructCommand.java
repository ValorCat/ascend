package cmd.control;

import cmd.Command;
import interpreter.Parser;
import interpreter.Parser.DoControl;

public class OpenDoStructCommand extends Command {

	@Override
	public String getName() {
		return "OPEN_DO_STRUCT";
	}

	@Override
	public void execute(Parser parser) {
		DoControl control = parser.new DoControl();
		parser.addFlowControl(control);
	}

}
