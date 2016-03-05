package util;

public class AscendException extends RuntimeException {

	private static final long serialVersionUID = 5533079138079047322L;
	private ErrorCode code;

	public AscendException() {}
	
	public AscendException(ErrorCode code, String message, Object... args) {
		super(String.format(message, args));
		this.code = code;
	}
	
	public ErrorCode getCode() {
		return code;
	}
	
	public String getName() {
		return code.getName();
	}
	
	public boolean isCritical() {
		return code.isCritical();
	}
	
}
