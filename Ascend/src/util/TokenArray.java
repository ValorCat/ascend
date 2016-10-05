package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenArray implements Iterable<String> {
	
	private List<String> tokens;
	private int size;
	
	public TokenArray(String... tokenStream) {
		this.tokens = Arrays.asList(tokenStream);
		this.size = tokens.size();
	}
	
	public TokenArray(List<String> tokenStream) {
		int newSize = tokenStream.size();
		this.tokens = new ArrayList<>(newSize);
		this.size = newSize;
		for (String token : tokenStream) {
			tokens.add(token);
		}
	}
	
	public TokenArray(TokenArray other) {
		this.tokens = other.asList();
		this.size = tokens.size();
	}

	public void add(int pos, String... newTokens) {
		tokens.addAll(pos, Arrays.asList(newTokens));
		size += newTokens.length;
	}

	public void append(String... newTokens) {
		add(tokens.size(), newTokens);
	}

	public String[] asArray() {
		return tokens.toArray(new String[0]);
	}
	
	public List<String> asList() {
		return tokens;
	}
	
	public String asString() {
		StringBuilder concat = new StringBuilder();
		for (String token : tokens) {
			concat.append(" ").append(token);
		}
		if (concat.length() > 0) {
			concat.deleteCharAt(0);
		}
		return concat.toString();
	}
	
	public boolean contains(String token) {
		return tokens.contains(token);
	}
	
	public boolean endsWith(String token) {
		if (size < 1) {
			return false;
		}
		return tokens.get(tokens.size() - 1).equals(token);
	}
	
	public String get(int pos) {
		if (pos < 0) {
			pos = tokens.size() + pos;
		}
		return tokens.get(pos);
	}
	
	public int get(String token) {
		return tokens.indexOf(token);
	}

	public Matcher match(String regex) {
		regex = regex
				.replaceAll("\\{Ident\\}", TokenType.ID_REGEX)
				.replaceAll("\\{Type\\}", TokenType.TYPE_REGEX)
				.replaceAll("\\{Equals\\}", TokenType.EQUALS_REGEX);
		String concat = asString();
		Matcher matcher = Pattern.compile(regex).matcher(concat.subSequence(0, concat.length()));
		matcher.matches();
		return matcher;
	}
	
	public void prepend(String... newTokens) {
		add(0, newTokens);
	}
	
	public TokenArray range(int start) {
		return range(start, tokens.size());
	}
	
	public TokenArray range(int start, int end) {
		if (end < 0) {
			end = tokens.size() + end;
		}
		return new TokenArray(Arrays.copyOfRange(tokens.toArray(new String[0]), start, end));
	}
	
	public void set(int pos, String token) {
		tokens.set(pos, token);
	}

	public int size() {
		return size;
	}
	
	public boolean startsWith(String token) {
		if (size < 0) {
			return false;
		}
		return tokens.get(0).equals(token);
	}

	public String toString() {
		return "[" + toCleanString() + "]";
	}
	
	public String toCleanString() {
		return asString().replaceAll("\n", "\\\\n");
	}

	@Override
	public Iterator<String> iterator() {
		return tokens.iterator();
	}
	
}
