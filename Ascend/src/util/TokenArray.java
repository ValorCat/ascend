package util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TokenArray implements Iterator<String> {

	private String[] tokens;
	private int index;
	
	public TokenArray(String... tokenStream) {
		tokens = tokenStream;
		index = 0;
	}
	
	public TokenArray(List<String> tokenStream) {
		tokens = tokenStream.toArray(new String[0]);
		index = 0;
	}
	
	public TokenArray(TokenArray other) {
		tokens = other.toArray();
		index = 0;
	}
	
	public String get(int pos) {
		if (pos < 0) {
			pos = tokens.length + pos;
		}
		return tokens[pos];
	}
	
	public int get(String token) {
		for (int i = 0; i < tokens.length; i++) {
			if (token.equals(tokens[i])) {
				return i;
			}
		}
		return -1;
	}
	
	public void set(int pos, String token) {
		tokens[pos] = token;
	}
	
	public TokenArray range(int start) {
		return range(start, tokens.length);
	}
	
	public TokenArray range(int start, int end) {
		if (end < 0) {
			end = tokens.length + end;
		}
		return new TokenArray(Arrays.copyOfRange(tokens, start, end));
	}
	
	public void add(int pos, String... newTokens) {
		int priorLength = tokens.length;
		int addedLength = newTokens.length;
		int finalLength = priorLength + addedLength;
		int offset = pos + addedLength;
		String[] firstPart = Arrays.copyOfRange(tokens, 0, pos);
		String[] lastPart = Arrays.copyOfRange(tokens, pos, priorLength);
		tokens = new String[finalLength];
		for (int i = 0; i < firstPart.length; i++) {
			tokens[i] = firstPart[i];
		}
		for (int i = 0; i < addedLength; i++) {
			tokens[pos + i] = newTokens[i];
		}
		for (int i = 0; i < lastPart.length; i++) {
			tokens[offset + i] = lastPart[i];
		}
	}
	
	public void prepend(String... newTokens) {
		int priorLength = tokens.length;
		int addedLength = newTokens.length;
		int finalLength = priorLength + addedLength;
		String[] original = Arrays.copyOf(tokens, priorLength);
		tokens = new String[finalLength];
		for (int i = 0; i < addedLength; i++) {
			tokens[i] = newTokens[i];
		}
		for (int i = 0; i < original.length; i++) {
			tokens[addedLength + i] = original[i];
		}
	}
	
	public void append(String... newTokens) {
		int priorLength = tokens.length;
		int addedLength = newTokens.length;
		int finalLength = priorLength + addedLength;
		String[] original = Arrays.copyOf(tokens, priorLength);
		tokens = new String[finalLength];
		for (int i = 0; i < original.length; i++) {
			tokens[i] = original[i];
		}
		for (int i = 0; i < addedLength; i++) {
			tokens[priorLength + i] = newTokens[i];
		}
	}
	
	@Override
	public boolean hasNext() {
		boolean next = index < tokens.length;
		if (next) {
			return true;
		} else {
			index = 0;
			return false;
		}
	}

	@Override
	public String next() {
		return tokens[index++];
	}
	
	public int size() {
		return tokens.length;
	}
	
	public List<String> toList() {
		return Arrays.asList(tokens);
	}
	
	public String[] toArray() {
		return tokens.clone();
	}
	
	public String toString() {
		return "[" + toCleanString() + "]";
	}
	
	public String toCleanString() {
		String repr = "";
		for (String token : tokens) {
			repr += " " + token;
		}
		if (repr.length() > 0) {
			repr = repr.substring(1);
		}
		return repr.replaceAll("\n", "\\\\n");
	}
	
}
