package cmd.control;

import cmd.Command;
import interpreter.Parser;
import interpreter.Parser.FlowControl;
import interpreter.Parser.ForControl;

public class CloseStructCommand extends Command {

	@Override
	public String getName() {
		return "CLOSE_STRUCT";
	}

	@Override
	public void execute(Parser parser) {
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
