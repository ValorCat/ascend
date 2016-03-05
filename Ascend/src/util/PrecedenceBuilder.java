package util;

import java.util.ArrayList;

public class PrecedenceBuilder {
	
	private static final String[][] PRECEDENCE = {
		{"?", "#", "@"},
		{"not"},
		{"^"},
		{"*", "/", "%"},
		{"+", "-"},
		{"<", ">", "<=", ">="},
		{"==", "<>", "is", "to"},
		{"and", "or"},
	};
	
	private ArrayList<Integer>[] levels;
	
	@SuppressWarnings("unchecked")
	public PrecedenceBuilder() {
		levels = (ArrayList<Integer>[]) new ArrayList[PRECEDENCE.length];
	}
	
	public void addOper(int pos, String oper) {
		int level = getLevelFromOper(oper);
		if (level == -1) {
			throw new AscendException(ErrorCode.SYNTAX, "The '%s' operator is undefined", oper);
		}
		if (levels[level] == null) {
			levels[level] = new ArrayList<Integer>();
		}
		levels[level].add(pos);
	}
	
	public Integer[] compile() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < levels.length; i++) {
			ArrayList<Integer> level = levels[i];
			if (level != null && level.size() > 0) {
				result.addAll(levels[i]);
			}
		}
		return result.toArray(new Integer[0]);
	}
	
	private int getLevelFromOper(String oper) {
		for (int row = 0; row < PRECEDENCE.length; row++) {
			for (int item = 0; item < PRECEDENCE[row].length; item++) {
				if (oper.equals(PRECEDENCE[row][item])) {
					return row;
				}
			}
		}
		return -1;
	}

}
