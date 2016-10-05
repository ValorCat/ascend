package control;

public class DoControl extends FlowControl {
	
	public DoControl() {
		super("DO");
	}
	
	@Override
	public boolean atEnd() {
		return false;
	}
	
}
