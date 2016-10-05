package cmd.control;

import cmd.Command;
import control.FlowControl;
import control.ForControl;
import interpreter.Parser;

public class CloseStructCommand extends Command {

	public CloseStructCommand() {
		super("CLOSE_STRUCT");
	}
	
	@Override
	public void onExecute() {
		Parser parser = Parser.getParser();
		FlowControl control = parser.getTopControl();
		boolean active = control.atEnd();
		if (!active) {
			if (control instanceof ForControl) {
				String varName = ((ForControl) control).getIterName();
				parser.getEnv().unmapName(varName);
				devOutput("(unloaded %s) ", varName);
			}
			parser.removeFlowControl();
		}
		devOutput("(%s) %s", control.getName(), active ? "looped" : "done");
	}

}
