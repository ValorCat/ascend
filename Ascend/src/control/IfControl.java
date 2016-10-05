package control;

import java.util.ArrayList;

import interpreter.Evaluator;
import util.AscendException;
import util.ErrorCode;
import util.TokenArray;
import util.Value;

public class IfControl extends FlowControl {
	
	private ArrayList<Integer> elseBlockIndices;
	private int terminateIndex = endIndex;
	
	public IfControl() {
		super("IF");
		elseBlockIndices = new ArrayList<Integer>();
		for (int i = headerIndex + 3; i < endIndex; i++) {
			if (blockLevels[i] == headerBlockLevel) {
				String commandName = commands.get(i).getName();
				if (commandName.equals("OPEN_IF_BRANCH")) {
					elseBlockIndices.add(i);
				} else if (!commandName.equals("CLOSE_STRUCT")) {
					throw new AscendException(ErrorCode.SYNTAX, "Else clause expected");
				}
			}
		}
	}
	
	public boolean evaluate(TokenArray expr) {
		int commandIndex = parser.getCommandIndex();
		if (commandIndex == terminateIndex) {
			commandIndex = endIndex - 1;
			return false;
		}
		Value result = Evaluator.evaluate(expr);
		if (!result.isA("bool")) {
			throw new AscendException(ErrorCode.TYPE, "Conditional expression evaluated to '" + result.type() + "', expected 'bool'");
		}
		if (elseBlockIndices.size() > 0 && commandIndex == elseBlockIndices.get(0)) {
			elseBlockIndices.remove(0);
		}
		boolean resultAsBool = (boolean) result.value();
		boolean elsesRemaining = elseBlockIndices.size() > 0;
		if (resultAsBool) {
			if (elsesRemaining) {
				terminateIndex = elseBlockIndices.get(0);
			}
		} else {
			if (elsesRemaining) {
				parser.setCommandIndex(elseBlockIndices.get(0) - 1);
			} else {
				parser.setCommandIndex(endIndex - 1);
			}
		}
		return resultAsBool;
	}
	
	@Override
	public boolean atEnd() {
		return false;
	}
	
}