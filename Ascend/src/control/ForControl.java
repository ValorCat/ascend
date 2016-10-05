package control;

import interpreter.Environment;
import interpreter.Evaluator;
import util.AscendException;
import util.ErrorCode;
import util.Operation;
import util.TokenArray;
import util.Value;

public class ForControl extends FlowControl {
	
	private TokenArray finalExpr;
	private Value finalValue;
	private Value updateValue;
	private String varName;
	private String operator;

	public ForControl(TokenArray finalExpr, Value updateValue, String varName, boolean reverse) {
		super("FOR");
		this.finalExpr = finalExpr;
		this.finalValue = Evaluator.getConstValue(finalExpr);
		this.updateValue = updateValue;
		this.varName = varName;
		this.operator = reverse ? ">=" : "<=";
	}

	public boolean evaluate() {
		Value endValue = finalValue != null ? finalValue : Evaluator.evaluate(finalExpr);
		TokenArray continueExpr = new TokenArray(varName, operator, String.valueOf(endValue.value()));
		Value result = Evaluator.evaluate(continueExpr);
		if (!result.isA("bool")) {
			throw new AscendException(ErrorCode.TYPE, String.format("End expression evaluated to '%s', expected 'bool'", result.type()));
		}
		boolean resultAsBool = (boolean) result.value();
		if (!resultAsBool) {
			parser.setCommandIndex(endIndex);
		}
		return resultAsBool;
	}
	
	@Override
	public boolean atEnd() {
		Environment env = parser.getEnv();
		Value oldValue = env.getValueFromName(varName);
		Value newValue = Operation.operate("+", oldValue, updateValue);
		env.mapNameToValue(varName, newValue);
		boolean result = evaluate();
		if (result) {
			parser.setCommandIndex(headerIndex + 1);
		}
		return result;
	}
	
	public String getIterName() {
		return varName;
	}
	
}
