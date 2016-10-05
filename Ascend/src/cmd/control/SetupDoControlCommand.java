package cmd.control;

import cmd.Command;
import control.DoControl;
import interpreter.Parser;

public class SetupDoControlCommand extends Command {

	public SetupDoControlCommand() {
		super("SETUP_DO_CNTRL");
	}
	
	@Override
	public void onExecute() {
		DoControl control = new DoControl();
		Parser.getParser().addFlowControl(control);
	}

}
