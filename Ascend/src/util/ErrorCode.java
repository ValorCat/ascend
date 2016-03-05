package util;

public enum ErrorCode {

	// unchecked
	INTERNAL("InternalError"),
	SYNTAX("SyntaxError"),
	TYPE("TypeError"),
	REFERENCE("ReferenceError"),
	INDEX("IndexError"),
	
	// checked
	ASSERT("AssertionError"),
	MATH("MathError");
	
	private String name;
	
	ErrorCode(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isCritical() {
		return this.ordinal() <= 4;
	}
	
}
