package util;

public class Value {
	
	private String type;
	private Object value;
	
	public Value() {
		this(null, null);
	}
	
	public Value(String type) {
		this(type, null);
	}
	
	public Value(String type, Object value) {
		this.type = type;
		this.value = value;
	}
	
	public String type() {
		return type;
	}
	
	public Object value() {
		return value;
	}
	
	public boolean hasValue() {
		return value != null;
	}
	
	public boolean isA(String type) {
		return this.type.equals(type);
	}
	
	public boolean isPrimitive() {
		return interpreter.Environment.isPrimitive(type);
	}
	
	public boolean equals(Object o) {
		if (o instanceof Value && value != null) {
			Value other = (Value) o;
			return type.equals(other.type()) && value.equals(other.value());
		}
		return false;
	}
	
	public String toString() {
		String strValue;
		if (isA("str")) {
			strValue = String.format("\"%s\"", value);
		} else if (value == null) {
			return String.format("(%s?)", type);
		} else {
			strValue = value.toString();
		}
		return "(" + type + " " + strValue + ")";
	}
	
	public String toCleanString() {
		switch (type) {
		case "array":
			return String.format("arr@%s", value);
		case "func":
			return String.format("func@%s", value);
		case "proc":
			return String.format("proc@%s", value);
		default:
			if (value == null) {
				return "null";
			}
			return value.toString();
		}
	}

}
