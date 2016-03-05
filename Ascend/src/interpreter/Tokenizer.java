package interpreter;

import java.util.ArrayList;
import java.util.Arrays;

import util.AscendException;
import util.ErrorCode;
import util.TokenArray;
import util.TokenType;

public class Tokenizer {

	// original Tokenizer.java: pastebin.com/5b7TK2ur
	
	private static final String PATTERN_SPLIT = "((?<=%1$s)|(?=%1$s))";
	private static final String PATTERN_KEY = "[\\p{Punct}\n\t ]";
	private static final String[] COMPOUND_OPS = new String[] {
			"+=", "-=", "*=", "/=", "^=", "==", "<>", "<=", ">=", "++", "--", "[]"
	};
	
	private static ArrayList<String> tokens = new ArrayList<String>();
	
	public static TokenArray tokenize(String textStream) {
		tokens.clear();
		basicSplit(textStream);
		removeComments();
		optimize();
		return new TokenArray(tokens);
	}
	
	private static void basicSplit(String text) {
		String pattern = String.format(PATTERN_SPLIT, PATTERN_KEY);
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
		ArrayList<String> newTokens = new ArrayList<String>();
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
					tokens.set(i + 1, "-" + nextToken);
					newTokens.add(null);
					continue;
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
	
}
