package interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import util.AscendException;
import util.ErrorCode;
import util.TokenArray;
import util.TokenType;

public class Tokenizer {

	// original Tokenizer.java: pastebin.com/5b7TK2ur
	
	private static final String PATTERN_SPLIT = "((?<=%1$s)|(?=%1$s))";
	private static final String PATTERN_KEY = "[\\p{Punct}\n\t ]";
	private static final String[] COMPOUND_OPS = {
			"+=", "-=", "*=", "/=", "^=", "==", "<>", "<=", ">=", "++", "--", "%%"
	};
	
	private static List<String> tokens = new ArrayList<>();
	
	public static TokenArray tokenize(String textStream) {
		tokens.clear();
		basicSplit(textStream, PATTERN_KEY);
		removeComments();
		optimize();
		return new TokenArray(tokens);
	}
	
	public static TokenArray retokenize(String textStream) {
		tokens.clear();
		basicSplit(textStream, " ");
		if (tokens.size() > 1)
			handleStringsOnly();
		return new TokenArray(tokens);
	}
	
	private static void basicSplit(String text, String key) {
		String pattern = String.format(PATTERN_SPLIT, key);
		tokens.addAll(Arrays.asList(text.split(pattern)));
	}
	
	private static void removeComments() {
		boolean inComment = false;
		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			if (token.equals("\n")) {
				inComment = false;
			}
			if (!inComment) {
				if (token.equals("/") && i < tokens.size() - 2 && tokens.get(i + 1).equals("/")) {
					tokens.remove(i);
					i--;
					inComment = true;
				}
			} else {
				tokens.remove(i);
				i--;
			}
		}
	}
	
	private static void optimize() {
		List<String> newTokens = new ArrayList<>();
		boolean inStringLiteral = false;
		boolean skipEscapedQuote = false;
		String currentString = "";
		String currentField = "";
		int lastIndex = tokens.size() - 1;
		outer:
		for (int i = 0; i < lastIndex + 1; i++) {
			String token = tokens.get(i);
			String nextToken = i < lastIndex ? tokens.get(i + 1) : null;
			
			if (newTokens.size() > i && newTokens.get(i) == null) {
				continue;
			}
			
			// beginning/ending string literals
			if (token.equals("\"") && !skipEscapedQuote) {
				inStringLiteral = !inStringLiteral;
				skipEscapedQuote = false;
				if (inStringLiteral) {
					currentString = "";
					newTokens.add(null);
				} else {
					newTokens.add("\"" + currentString + "\"");
				}
				continue;
			}
			
			if (nextToken != null) {
				// string literals
				if (inStringLiteral) {
					skipEscapedQuote = false;
					if (token.equals("\\") && nextToken.equals("\"")) {
						skipEscapedQuote = true;
					}
					currentString += token;
					newTokens.add(null);
					continue;
				}
				
				// whitespace
				if (token.equals(" ") || token.equals("\t")) {
					newTokens.add(null);
					continue;
				}
				
				if (nextToken.equals(".") && i < lastIndex - 1) {
					String doubleNextToken = tokens.get(i + 2);
					if (token.matches("\\-?\\p{Digit}+") && doubleNextToken.matches("\\p{Digit}+")) {
						// float literal
						newTokens.add(token + "." + doubleNextToken);
						newTokens.add(null);
						newTokens.add(null);
						continue;
					} else if (TokenType.isIdentifier(token) && TokenType.isIdentifier(doubleNextToken)) {
						// field or method
						currentField = token + "." + doubleNextToken;
						newTokens.add(null);
						newTokens.add(null);
						newTokens.add(null);
						continue;
					}
				}
				
				// continued field/method
				if (token.equals(".") && TokenType.isIdentifier(nextToken) && currentField.length() > 0) {
					currentField += "." + nextToken;
					newTokens.add(null);
					newTokens.add(null);
					continue;
				}
				
				// end of field/method
				if (currentField.length() > 0) {
					newTokens.set(newTokens.size() - 1, currentField);
					currentField = "";
				}
				
				// negative numbers
				if (token.equals("-") && nextToken.matches("\\p{Digit}+")) {
					TokenType lastType = i > 0 ? TokenType.getType(tokens.get(i - 1)) : null;
					if (i == 0 || lastType == TokenType.OPERATOR || lastType == TokenType.CONTROL) {
						tokens.set(i + 1, "-" + nextToken);
						newTokens.add(null);
						continue;
					}
				}
				
				// semicolons
				if (token.equals(";") && (nextToken.equals("\n") || i == lastIndex)) {
					// redundant semicolon
					newTokens.add(null);
					continue;
				}
				
				// compound operators
				if (token.matches("\\p{Punct}") && nextToken.matches("\\p{Punct}")) {
					String possibleOperator = token + nextToken;
					for (String operator : COMPOUND_OPS) {
						if (operator.equals(possibleOperator)) {
							newTokens.add(possibleOperator);
							newTokens.add(null);
							continue outer;
						}
					}
				}
				
				// array types
				if (TokenType.isIdentifier(token) && nextToken.equals("[")) {
					if (i < tokens.size() - 2 && tokens.get(i + 2).equals("]")) {
						newTokens.add(token + "[]");
						newTokens.add(null);
						newTokens.add(null);
						continue;
					}
				}
				
				newTokens.add(token);
			} else if (inStringLiteral) {
				throw new AscendException(ErrorCode.SYNTAX, "Insert '\\' to complete string literal");
			} else {
				// end of field/method
				if (currentField.length() > 0) {
					newTokens.set(newTokens.size() - 1, currentField);
					currentField = "";
				}
				newTokens.add(token);
			}
		}
		while (newTokens.remove(null));
		tokens = newTokens;
	}
	

	private static void handleStringsOnly() {
		List<String> newTokens = new ArrayList<>();
		boolean inStringLiteral = false;
		boolean skipEscapedQuote = false;
		StringBuilder currentString = new StringBuilder();
		for (String token : tokens) {
			if (skipEscapedQuote && token.contains("\"")) {
				newTokens.add(token);
				skipEscapedQuote = false;
			} else if (token.startsWith("\"") && !inStringLiteral) {
				if (token.endsWith("\"") && token.length() > 1) {
					newTokens.add(token);
				} else {
					currentString.append(token);
					inStringLiteral = true;
				}
			} else if (token.endsWith("\"") && inStringLiteral) {
				currentString.append(token);
				newTokens.add(currentString.toString());
				currentString.delete(0, currentString.length());
				inStringLiteral = false;
			} else if (inStringLiteral) {
				if (token.equals("\\"))
					skipEscapedQuote = true;
				currentString.append(token);
			} else if (!token.equals(" ")) {
				newTokens.add(token);
			}
		}
		tokens = newTokens;
	}

	
}
