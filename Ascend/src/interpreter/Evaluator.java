package interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import util.AscendException;
import util.ErrorCode;
import util.Operation;
import util.PrecedenceBuilder;
import util.TokenArray;
import util.TokenType;
import util.Value;

public class Evaluator {
	
	// Old Evaluator: pastebin.com/JCzi6kaS
	// Old Evaluator #2: pastebin.com/X4Z2vAic
	
	private static final String[] UNARY_OPS = {"?", "#", "@", "not"};
	private static final String[] LEFT_ASSOC = {"?"};
	
	private static interface Token {
		
		String getType();
		
	}
	
	private static class Expression implements Token {
		
		private List<Token> expr;
		
		public Expression(List<Token> expr) { this.expr = expr; }
		
		public String getType() { return "expression"; }
		
		public Token get(int pos) { return expr.get(pos); }
		
		public void set(int pos, Token token) { expr.set(pos,  token); }
		
		public int size() { return expr.size(); }
		
		public String toString() { return expr.toString(); }
		
		public void clean() {
			List<Token> temp = new ArrayList<>();
			for (Token token : expr) {
				if (token != null) {
					temp.add(token);
				}
			}
			expr = temp;
		}
		
	}
	
	private static class Literal implements Token {
		
		private Value value;
		
		public Literal(Value value) { this.value = value; }
		
		public String getType() { return "literal"; }
		
		public Value toValue() { return value; }
		
		public String toString() { return value.toCleanString(); }
		
	}
	
	private static class Variable implements Token {
		
		private String name;
		
		public Variable(String name) { this.name = name; }
		
		public String getType() { return "variable"; }
		
		public String getName() { return name; }
		
		public String toString() { return name; }
		
	}
	
	private static class Function implements Token {
		
		private String name;
		private TokenArray args;
		
		public Function(String name) { this.name = name; }
		
		public Function(String name, TokenArray args) { this.name = name; this.args = args; }
		
		public String getType() { return "function"; }
		
		public String getName() { return name; }
		
		public TokenArray getArgs() { return args; }
		
		public String toString() {
			return String.format("%s(%s)", name, args != null ? args.toCleanString() : "");
		}
		
	}
	
	private static class Index implements Token {
		
		private String sequence;
		private Expression startIndex;
		private Expression endIndex;
		
		public Index(String sequence, Expression startIndex, Expression endIndex) {
			this.sequence = sequence;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}
		
		public String getType() { return "index"; }
		
		public String getName() { return sequence; }
		
		public Expression getStartIndex() { return startIndex; }
		
		public Expression getEndIndex() { return endIndex; }
		
		public String toString() {
			String end = endIndex != null ? endIndex.toString() : "end";
			return String.format("%s[%s,%s]", sequence, startIndex.toString(), end);
		}
		
	}
	
	private static class Operator implements Token {
		
		private String oper;
		
		public Operator(String oper) { this.oper = oper; }
		
		public String getType() { return "operator"; }
		
		public String getOper() { return oper; }
		
		public boolean equals(String other) { return oper.equals(other); }
		
		public String toString() { return oper; }
		
	}
	
	private enum OperatorType {
		
		BINARY, UNARY_LEFT, UNARY_RIGHT;
		
		public static OperatorType getType(String oper) {
			boolean unary = false;
			for (String unaryOper : UNARY_OPS) {
				if (oper.equals(unaryOper)) {
					unary = true;
					break;
				}
			}
			if (!unary) {
				return BINARY;
			}
			for (String unaryOper : LEFT_ASSOC) {
				if (oper.equals(unaryOper)) {
					return UNARY_LEFT;
				}
			}
			return UNARY_RIGHT;
		}
		
	}
	
	public static Value evaluate(TokenArray tokens) {
		Expression expr = toExprFormat(tokens);
		return evaluate(expr);
	}
	
	private static Value evaluate(Expression expr) {
		evalVariables(expr);
		evalSubExpressions(expr);
		return simplify(expr);
	}

	private static Value evaluateConst(TokenArray tokens) {
		Expression expr = toExprFormat(tokens);
		evalSubExpressions(expr);
		return simplify(expr);
	}
	
	public static Value getConstValue(TokenArray expr) {
		for (String token : expr) {
			if (TokenType.isIdentifier(token)) {
				return null;
			}
		}
		return evaluateConst(expr);
	}
	
	private static Expression toExprFormat(TokenArray tokens) {
		List<Token> expr = new ArrayList<>();
		int end = tokens.size();
		for (int i = 0; i < end; i++) {
			String token = tokens.get(i);
			switch (TokenType.getType(token)) {
			case IDENTIFIER:
				if (i < end - 1) {
					if (tokens.get(i + 1).equals("(")) {
						int endParens = findMatchingWrapper(tokens, i + 1);
						expr.add(setupFunction(tokens, i, endParens));
						i = endParens;
						continue;
					} else if (tokens.get(i + 1).equals("[")) {
						int endBracket = findMatchingWrapper(tokens, i + 1);
						expr.add(setupIndex(tokens, i, endBracket));
						i = endBracket;
						continue;
					}
				}
				expr.add(new Variable(token));
				break;
			case LITERAL:
				expr.add(setupLiteral(token));
				break;
			case OPERATOR:
				if (token.equals("(")) {
					int match = findMatchingWrapper(tokens, i);
					expr.add(toExprFormat(tokens.range(i + 1, match)));
					i = match;
				} else {
					expr.add(new Operator(token));
				}
				break;
			default:
				throw new AscendException(ErrorCode.SYNTAX, "Unknown token '%s'", token);
			}
		}
		return new Expression(expr);
	}
	
	private static Function setupFunction(TokenArray tokens, int start, int end) {
		String name = tokens.get(start);
		if (end == start + 2) {
			// no args
			return new Function(name);
		} else {
			// specified args
			TokenArray args = tokens.range(start + 2, end);
			return new Function(name, args);
		}
	}
	
	private static Index setupIndex(TokenArray tokens, int start, int end) {
		String name = tokens.get(start);
		TokenArray[] indices = divideExprList(tokens.range(start + 2, end), ",");
		if (indices.length == 0) {
			throw new AscendException(ErrorCode.INDEX, "Missing sequence index value");
		} else if (indices.length > 2) {
			throw new AscendException(ErrorCode.INDEX, "Expected one or two indices, got %i", indices.length);
		}
		Expression startIndex = toExprFormat(indices[0]);
		Expression endIndex = indices.length == 2 ? toExprFormat(indices[1]) : null;
		return new Index(name, startIndex, endIndex);
	}
	
	private static Literal setupLiteral(String token) {
		Value value;
		if (TokenType.isIntegerLiteral(token)) {
			value = new Value("int", Integer.parseInt(token));
		} else if (TokenType.isFloatLiteral(token)) {
			value = new Value("float", Double.parseDouble(token));
		} else if (TokenType.isBooleanLiteral(token)) {
			value = new Value("bool", Boolean.parseBoolean(token));
		} else if (TokenType.isStringLiteral(token)) {
			String str = token.substring(1, token.length() - 1);
			value = new Value("str", str.replaceAll("\\\\n", "\n"));
		} else {
			throw new AscendException(ErrorCode.TYPE, "Token '%s' (%s) was incorrectly flagged as a literal", token, TokenType.getType(token).toString());
		}
		return new Literal(value);
	}
	
	private static void evalVariables(Expression expr) {
		int end = expr.size();
		for (int i = 0; i < end; i++) {
			Token token = expr.get(i);
			if (token instanceof Variable) {
				String varName = ((Variable) token).getName();
				Value value = Parser.getParser().getEnv().getValueFromName(varName);
				expr.set(i, new Literal(value));
			} else if (token instanceof Function) {
				Value result = evalFunction((Function) token);
				expr.set(i, new Literal(result));
			} else if (token instanceof Index) {
				Index index = (Index) token;
				Value sequence = Parser.getParser().getEnv().getValueFromName(index.getName());
				Value endIndex = index.getEndIndex() != null ? evaluate(index.getEndIndex()) : null;
				Operation.index(sequence, evaluate(index.getStartIndex()), endIndex);
				Value result = Operation.poll();
				expr.set(i, new Literal(result));
			} else if (token instanceof Operator && ((Operator) token).equals("@")) {
				evalPointer(expr, i, end);
				i++;
			}
		}
		expr.clean();
	}
	
	private static void evalPointer(Expression expr, int pos, int end) {
		if (pos < end - 1) {
			Token nextToken = expr.get(pos + 1);
			if (nextToken instanceof Variable) {
				String varName = ((Variable) nextToken).getName();
				int id = Parser.getParser().getEnv().getIDFromName(varName);
				expr.set(pos, new Literal(new Value("int", id)));
				expr.set(pos + 1, null);
			} else {
				throw new AscendException(ErrorCode.TYPE, "Cannot evaluate pointer of type %s", nextToken.getClass().getName());
			}
		} else {
			throw new AscendException(ErrorCode.SYNTAX, "Missing pointer target");
		}
	}
	
	private static Value evalFunction(Function func) {
		String varName = func.getName();
		Value value = Parser.getParser().getEnv().getValueFromName(varName);
		if (value.isA("proc")) {
			throw new AscendException(ErrorCode.TYPE, "Cannot call procedure '%s' within an expression", varName);
		} else if (!value.isA("func")) {
			throw new AscendException(ErrorCode.TYPE, "Cannot call '%s', a non-executable", varName);
		}
		List<Value> args;
		if (func.getArgs() != null) {
			TokenArray[] argExpr = divideExprList(func.getArgs(), ",");
			args = evalExprList(Arrays.asList(argExpr));
		} else {
			args = new ArrayList<>();
		}
		if (value.value() instanceof String) {
			// native
			return Parser.callExecutable(varName, value, args.toArray(new Value[0]));
		} else {
			// user-defined
			return null;
		}
	}
	
	public static TokenArray[] divideExprList(TokenArray expr, String breakToken) {
		ArrayList<TokenArray> list = new ArrayList<TokenArray>();
		ArrayList<String> tokens = new ArrayList<String>();
		int subExprCount = 0;
		for (String token : expr) {
			if (token.equals(breakToken) && subExprCount == 0) {
				list.add(new TokenArray(tokens));
				tokens.clear();
			} else {
				if (token.equals("(") || token.equals("{") || token.equals("[")) {
					subExprCount++;
				} else if (token.equals(")") || token.equals("}") || token.equals("]")) {
					subExprCount--;
				}
				tokens.add(token);
			}
		}
		list.add(new TokenArray(tokens));
		return list.toArray(new TokenArray[0]);
	}
	
	private static List<Value> evalExprList(List<TokenArray> list) {
		List<Value> argValues = new ArrayList<>(list.size());
		for (TokenArray arg : list) {
			argValues.add(evaluate(arg));
		}
		return argValues;
	}
	
	private static void evalSubExpressions(Expression expr) {
		for (int i = 0; i < expr.size(); i++) {
			Token token = expr.get(i);
			if (token instanceof Expression) {
				Value result = evaluate((Expression) token);
				expr.set(i, new Literal(result));
			}
		}
	}
	
	private static Value simplify(Expression expr) {
		List<Integer> operPositions = getOperPrecedence(expr);
		if (expr.size() > 1) {
			for (int pos : operPositions) {
				Token token = expr.get(pos);
				if (!(token instanceof Operator)) {
					throw new AscendException(ErrorCode.INTERNAL, "Expected operator, got %s", token.getType());
				}
				operate(expr, pos, ((Operator) token).getOper());
			}
			expr.clean();
			if (expr.size() != 1) {
				throw new AscendException(ErrorCode.SYNTAX, "Expression cannot be resolved to a single value: %s", expr.toString());
			}
		}
		Token lastToken = expr.get(0);
		if (!(lastToken instanceof Literal)) {
			throw new AscendException(ErrorCode.SYNTAX, "Expression cannot be resolved to a value: %s", lastToken.toString());
		}
		return ((Literal) lastToken).toValue();
	}

	private static List<Integer> getOperPrecedence(Expression expr) {
		PrecedenceBuilder order = new PrecedenceBuilder();
		for (int i = 0; i < expr.size(); i++) {
			Token token = expr.get(i);
			if (token instanceof Operator) {
				Operator oper = (Operator) token;
				order.addOper(i, oper.getOper());
			}
		}
		return order.compile();
	}
	
	private static void operate(Expression expr, int pos, String oper) {
		Value leftOperand, rightOperand;
		int leftMostPos, rightMostPos;
		Value result;
		switch (OperatorType.getType(oper)) {
		case UNARY_LEFT:
			leftMostPos = pos - 1;
			rightMostPos = pos;
			leftOperand = getOperand(expr, leftMostPos, oper, "left");
			result = Operation.operate(oper, leftOperand);
			break;
		case UNARY_RIGHT:
			leftMostPos = pos;
			rightMostPos = pos + 1;
			while (expr.get(rightMostPos) == null) {
				rightMostPos++;
			}
			rightOperand = getOperand(expr, rightMostPos, oper, "right");
			result = Operation.operate(oper, rightOperand);
			break;
		case BINARY:
			leftMostPos = pos - 1;
			rightMostPos = pos + 1;
			while (expr.get(rightMostPos) == null) {
				rightMostPos++;
			}
			leftOperand = getOperand(expr, leftMostPos, oper, "left");
			rightOperand = getOperand(expr, rightMostPos, oper, "right");
			result = Operation.operate(oper, leftOperand, rightOperand);
			break;
		default:
			throw new AscendException(ErrorCode.INTERNAL, "Unknown operator type '%s'", OperatorType.getType(oper).toString());
		}
		for (int i = leftMostPos; i < rightMostPos; i++) {
			expr.set(i, null);
		}
		expr.set(rightMostPos, new Literal(result));
	}
	
	private static Value getOperand(Expression expr, int pos, String oper, String side) {
		try {
			return ((Literal) expr.get(pos)).toValue();
		} catch (ClassCastException | IndexOutOfBoundsException e) {
			throw new AscendException(ErrorCode.SYNTAX, "The '%s' operator expects a value on the %s", oper, side);
		}
	}
	
	public static int findMatchingWrapper(TokenArray expr, int startPos) {
		String openWrapper = expr.get(startPos);
		String closeWrapper;
		switch(openWrapper) {
		case "(": closeWrapper = ")"; break;
		case "{": closeWrapper = "}"; break;
		case "[": closeWrapper = "]"; break;
		default: throw new AscendException(ErrorCode.SYNTAX, "Invalid wrapper '%s'", openWrapper);
		}
		int level = 1;
		for (int i = startPos + 1; i < expr.size(); i++) {
			String token = expr.get(i);
			if (token.equals(openWrapper)) {
				level++;
			} else if (token.equals(closeWrapper)) {
				level--;
				if (level == 0) {
					return i;
				}
			}
		}
		throw new AscendException(ErrorCode.SYNTAX, "Unmatched '%s' token", openWrapper);
	}

}