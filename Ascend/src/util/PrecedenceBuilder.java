package util;

import java.util.ArrayList;
import java.util.List;

public class PrecedenceBuilder {
	
	private static final String[][] PRECEDENCE = {
		{"?", "#", "@"},
		{"not"},
		{"^"},
		{"*", "/", "%", "%%"},
		{"+", "-"},
		{"<", ">", "<=", ">="},
		{"==", "<>", "is", "to"},
		{"and", "or"},
	};
	
	private class Row {
		
		private List<Integer> values;
		
		public Row() {
			this.values = new ArrayList<>();
		}
		
		public List<Integer> getRow() {
			return values;
		}
		
	}
	
	private Row[] levels;
	
	public PrecedenceBuilder() {
		levels = new Row[PRECEDENCE.length];
	}
	
	public void addOper(int pos, String oper) {
		int level = getLevelFromOper(oper);
		if (level == -1) {
			throw new AscendException(ErrorCode.SYNTAX, "The '%s' operator has no defined precedence", oper);
		}
		if (levels[level] == null) {
			levels[level] = new Row();
		}
		levels[level].getRow().add(pos);
	}
	
	public List<Integer> compile() {
		List<Integer> result = new ArrayList<>();
		for (int i = 0; i < levels.length; i++) {
			if (levels[i] != null) {
				List<Integer> level = levels[i].getRow();
				if (level.size() > 0)
					result.addAll(level);
			}
		}
		return result;
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
