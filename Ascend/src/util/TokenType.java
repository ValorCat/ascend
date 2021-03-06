package util;

public enum TokenType {

	IDENTIFIER, OPERATOR, CONTROL, LITERAL, INVALID;
	
	public static final String ID_REGEX = "[\\\\p{Alpha}_][\\\\w\\\\.]*";
	public static final String TYPE_REGEX = ID_REGEX + "(?:\\\\[\\\\])*";
	public static final String EQUALS_REGEX = "[\\\\p{Punct}]?=";
	private static final String ID_REGEX_NORMAL = "[\\p{Alpha}_][\\w\\.]*";
	
	public static final String[] OPERATORS = {
			"+", "-", "*", "/", "%", "%%", "^",
			"==", "<>", "<", ">", "<=", ">=",
			"not", "and", "or",
			"to", "is",
			".", "?", "#", "@",
			"(", ")", "{", "}", "[", "]"
	};
	
	public static final String[] CONTROLS = {
			"=", "+=", "-=", "*=", "/=", "%=", "^=",
			"break", "by", "del", "else", "end", "for", "if", "then", "return", "while"
	};
	
	public static TokenType getType(String token) {
		for (String control : CONTROLS) {
			if (token.equals(control)) {
				return CONTROL;
			}
		}
		for (String operator : OPERATORS) {
			if (token.equals(operator)) {
				return OPERATOR;
			}
		}
		if (isIntegerLiteral(token) || isFloatLiteral(token) || isBooleanLiteral(token) || isStringLiteral(token)) {
			return LITERAL;
		}
		String cutToken = token;
		while (cutToken.endsWith("[]")) {
			cutToken = cutToken.substring(0, cutToken.length() - 2);
		}
		if (cutToken.matches(ID_REGEX_NORMAL)) {
			return IDENTIFIER;
		}
		return INVALID;
	}
	
	public static boolean isIdentifier(String token) {
		return getType(token) == IDENTIFIER;
	}
	
	public static boolean isOperator(String token) {
		return getType(token) == OPERATOR;
	}
	
	public static boolean isAssignmentOperator(String token) {
		for (int i = 0; i < 7; i++) {
			if (token.equals(CONTROLS[i])) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isIntegerLiteral(String token) {
		return token.matches("\\-?\\d+");
	}
	
	public static boolean isFloatLiteral(String token) {
		return token.matches("\\-?\\d+\\.\\d+");
	}
	
	public static boolean isBooleanLiteral(String token) {
		return token.equals("true") || token.equals("false");
	}
	
	public static boolean isStringLiteral(String token) {
		return token.startsWith("\"") && token.endsWith("\"");
	}
	
}
