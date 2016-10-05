package control;

import interpreter.Evaluator;
import util.AscendException;
import util.ErrorCode;
import util.TokenArray;
import util.Value;

public class WhileControl extends FlowControl {
	
	private TokenArray controlExpr;
	
	public WhileControl(TokenArray controlExpr) {
		super("WHILE");
		this.controlExpr = controlExpr;
	}

	public boolean evaluate() {
		Value result = Evaluator.evaluate(controlExpr);
		if (!result.isA("bool")) {
			throw new AscendException(ErrorCode.TYPE, "Conditional expression evaluated to '" + result.type() + "', expected 'bool'");
		}
		boolean resultAsBool = (boolean) result.value();
		if (!resultAsBool) {
			parser.setCommandIndex(endIndex);
		}
		return resultAsBool;
	}
	
	@Override
	public boolean atEnd() {
		boolean result = evaluate();
		if (result) {
			parser.setCommandIndex(headerIndex + 1);
		}
		return result;
	}
	
}
