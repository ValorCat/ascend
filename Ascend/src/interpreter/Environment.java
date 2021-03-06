package interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import util.AscendException;
import util.ErrorCode;
import util.Value;

public class Environment {
	
	// Old Environment: pastebin.com/K7eW86pK
	// Old Environment #2: pastebin.com/7Na0iNju
	
	public static final String[] PRIMITIVE_TYPES = {"bool", "float", "func", "int", "obj", "proc", "str", "type"};
	
	private List<MemoryItem> memory;
	private List<Namespace> env;
	private List<ArrayObject> arrays;
	private Namespace global;
	private int builtinIndexEnd;
	
	private class MemoryItem {
		
		private Value value;
		private int refCount;
		
		public MemoryItem(Value value) {
			this.value = value;
			this.refCount = 1;
		}
		
		public Value getValue() {
			return value;
		}
		
		public void addRef() {
			refCount++;
		}
		
		/* returns true if item can be garbage collected */
		public boolean dropRef() {
			refCount--;
			boolean canBeDeleted = refCount == 0;
			if (canBeDeleted && value.isArray() && value.value() != null) {
				arrays.get((int) value.value()).dropRefs();
			}
			return canBeDeleted;
		}
		
		public String toString() {
			return String.format("%s#%d", value.toString(), refCount);
		}
		
	}
	
	private class Namespace {
		
		private Map<String, Integer> vars;
		private Map<String, Integer> blocks;
		
		public Namespace() {
			this.vars = new HashMap<>();
			this.blocks = new HashMap<>();
		}
		
		public int getMemoryID(String varName) {
			Integer id = vars.get(varName);
			return id == null ? -1 : id.intValue();
		}
		
		public MemoryItem getMemoryItem(String varName) {
			Integer id = vars.get(varName);
			if (id == null) {
				throw new AscendException(ErrorCode.REFERENCE, "The '" + varName + "' variable is undefined");
			}
			return memory.get(id.intValue());
		}
		
		public void mapNameToID(String varName, int id) {
			if (vars.containsKey(varName)) {
				verifyTypeContinuity(varName, id);
				vars.replace(varName, id);
			} else {
				vars.put(varName, id);
				blocks.put(varName, Parser.getParser().getBlockLevel());
			}
		}
		
		public void mapCoreNameToID(String varName, int id) {
			vars.put(varName, id);
			blocks.put(varName, 0);
		}
		
		private void verifyTypeContinuity(String varName, int newID) {
			int initID = vars.get(varName).intValue();
			String initType = memory.get(initID).getValue().type();
			String newType = memory.get(newID).getValue().type();
			if (!initType.equals(newType)) {
				throw new AscendException(ErrorCode.TYPE, String.format("Cannot assign value of type %s to %s variable %s", newType, initType, varName));
			}
		}
		
		public void unmapName(String varName) {
			Integer id = vars.get(varName);
			if (vars.get(varName) == null) {
				throw new AscendException(ErrorCode.REFERENCE, "The '" + varName + "' variable is undefined");
			}
			vars.remove(varName);
			blocks.remove(varName);
			MemoryItem item = memory.get(id);
			if (item.dropRef()) {
				memory.set(id, null);
			}
		}
		
		public void unmapLevel(int blockLevel) {
			ArrayList<String> removeIDs = new ArrayList<String>();
			for (Entry<String, Integer> item : blocks.entrySet()) {
				if (item.getValue() >= blockLevel) {
					removeIDs.add(item.getKey());
				}
			}
			for (String id : removeIDs) {
				unmapName(id);
			}
		}
		
	}
	
	private class ArrayObject {
		
		private String type;
		private int length;
		private int[] array;
		
		public ArrayObject(String type, int length) {
			this.type = type;
			this.length = length;
			this.array = new int[length];
			Arrays.fill(array, -1);
		}
		
		public ArrayObject(String type, int[] valueIDs) {
			this.type = type;
			this.length = valueIDs.length;
			this.array = valueIDs;
		}
		
		public String getType() {
			return type;
		}
		
		public int getLength() {
			return length;
		}
		
		public int[] getValueIDs() {
			return array;
		}
		
		public int getIDFromIndex(int index) {
			if (index < 0 || index >= length) {
				throw new AscendException(ErrorCode.INDEX, "Index out of bounds, [%d] of length %d array", index, length);
			}
			return array[index];
		}
		
		public void setIndexToID(int index, int id) {
			if (index < 0 || index >= length) {
				throw new AscendException(ErrorCode.INDEX, "Index out of bounds, [%d] of length %d array", index, length);
			}
			array[index] = id;
		}
		
		public void dropRefs() {
			for (int id : array) {
				dropRef(id);
			}
		}
		
	}
	
	public Environment() {
		// initialize fields
		this.memory = new ArrayList<>();
		this.env = new ArrayList<>();
		this.arrays = new ArrayList<>();
		this.env.add(new Namespace());
		this.global = env.get(0);
		
		// reserve memory for primitives
		for (String prim : PRIMITIVE_TYPES) {
			memory.add(new MemoryItem(new Value("type", prim)));
			global.mapCoreNameToID(prim, memory.size() - 1);
		}
		
		// import standard library
		importLibrary("core", lib.StandardLib.FUNCTIONS, lib.StandardLib.PROCEDURES, global);
		
		// import math library
		importLibrary("math", lib.MathLib.FUNCTIONS, lib.MathLib.PROCEDURES);
		env.get(1).mapCoreNameToID("E", storeInMemory(new Value("float", Math.E)));
		env.get(1).mapCoreNameToID("PI", storeInMemory(new Value("float", Math.PI)));
		
		// import string library
		importLibrary("string", lib.StringLib.FUNCTIONS, lib.StringLib.PROCEDURES);
		env.get(2).mapCoreNameToID("DIGIT", storeInMemory(new Value("str", "0123456789")));
		env.get(2).mapCoreNameToID("LOWER", storeInMemory(new Value("str", "abcdefghijklmnopqrstuvwxyz")));
		env.get(2).mapCoreNameToID("UPPER", storeInMemory(new Value("str", "ABCDEFGHIJKLMNOPQRSTUVWXYZ")));
		
		// import random library
		importLibrary("random", lib.RandomLib.FUNCTIONS, lib.RandomLib.PROCEDURES);
		
		builtinIndexEnd = memory.size() - 1;
	}
	
	private void importLibrary(String name, String[] funcList, String[] procList) {
		env.add(new Namespace());
		int id = env.size() - 1;
		global.mapCoreNameToID(name, storeInMemory(new Value("obj", id)));
		importLibrary(name, funcList, procList, env.get(id));
	}
	
	private void importLibrary(String name, String[] funcList, String[] procList, Namespace space) {
		for (String func : funcList) {
			memory.add(new MemoryItem(new Value("func", name + "/" + func)));
			space.mapCoreNameToID(func, memory.size() - 1);
		}
		for (String proc : procList) {
			memory.add(new MemoryItem(new Value("proc", name + "/" + proc)));
			space.mapCoreNameToID(proc, memory.size() - 1);
		}
	}
	
	private Namespace getNamespace(String varName) {
		String[] parts = varName.split("\\.");
		if (parts.length == 1) {
			return global;
		}
		return getNamespaceInternal(parts, 0, global);
	}
	
	private Namespace getNamespaceInternal(String[] nameParts, int pos, Namespace currentSpace) {
		int id = currentSpace.getMemoryID(nameParts[pos]);
		if (id == -1) {
			if (pos == 0) {
				throw new AscendException(ErrorCode.REFERENCE, "The '%s' type is undefined", nameParts[pos]);
			} else {
				throw new AscendException(ErrorCode.REFERENCE, "The '%s' attribute is undefined for the type '%s'", nameParts[pos], nameParts[pos - 1]);
			}
		}
		Value value = memory.get(id).getValue();
		if (!value.type().equals("obj")) {
			throw new AscendException(ErrorCode.TYPE, "The '" + nameParts[pos - 1] + "' type does not support attributes");
		}
		Namespace space = env.get((int) value.value());
		if (pos == nameParts.length - 2) {
			return space;
		}
		return getNamespaceInternal(nameParts, pos + 1, space);
	}
	
	private int storeInMemory(Value value) {
		for (int i = 0; i < memory.size(); i++) {
			MemoryItem item = memory.get(i);
			if (item == null) {
				// fill existing slot
				memory.set(i, new MemoryItem(value));
				return i;
			} else if (value.equals(item.getValue())) {
				// value already stored
				item.addRef();
				return i;
			}
		}
		// add value anew
		memory.add(new MemoryItem(value));
		return memory.size() - 1;
	}
	
	private void dropRef(int memoryID) {
		if (memoryID > -1 && memory.get(memoryID).dropRef()) {
			memory.set(memoryID, null);
		}
	}
	
	public int getIDFromName(String varName) {
		Namespace space = getNamespace(varName);
		String localName = getLocalName(varName);
		return space.getMemoryID(localName);
	}
	
	public Value getValueFromName(String varName) {
		Namespace space = getNamespace(varName);
		String localName = getLocalName(varName);
		return space.getMemoryItem(localName).getValue();
	}
	
	public Value getValueFromID(int id) {
		if (id >= memory.size() || id < 0) {
			throw new AscendException(ErrorCode.REFERENCE, "The memory location %d is unmapped", id);
		}
		return memory.get(id).getValue();
	}
	
	public void mapNameToValue(String varName, Value value) {
		Namespace space = getNamespace(varName);
		String localName = getLocalName(varName);
		int currentID = space.getMemoryID(varName);
		int newID = storeInMemory(value);
		space.mapNameToID(localName, newID);
		dropRef(currentID);
	}
	
	public void unmapName(String varName) {
		Namespace space = getNamespace(varName);
		String localName = getLocalName(varName);
		space.unmapName(localName);
	}
	
	public void unmapBlockLevel(int blockLevel) {
		global.unmapLevel(blockLevel);
	}
	
	public Value defineArray(String type, Value[] values) {
		int length = values.length;
		ArrayObject newArray = new ArrayObject(type, length);
		for (int i = 0; i < length; i++) {
			Value value = values[i];
			if (value != null) {
				int id = storeInMemory(value);
				newArray.setIndexToID(i, id);
			}
		}
		arrays.add(newArray);
		return new Value(type + "[]", arrays.size() - 1);
	}
	
	public Value defineArray(ArrayObject copy) {
		String type = copy.getType();
		int[] valueIDs = copy.getValueIDs().clone();
		for (int id : valueIDs) {
			memory.get(id).addRef();
		}
		arrays.add(new ArrayObject(type, valueIDs));
		return new Value(type + "[]", arrays.size() - 1);
	}
	
	public ArrayObject getArray(Value array) {
		int arrayID = (int) array.value();
		if (arrayID < 0 || arrayID > arrays.size()) {
			throw new AscendException(ErrorCode.INTERNAL, "Attempted to reference array ID %d of %d", arrayID, arrays.size());
		}
		return arrays.get(arrayID);
	}
	
	public Value getValueFromArrayIndex(Value array, int index) {
		int arrayID = (int) array.value();
		if (arrayID < 0 || arrayID > arrays.size()) {
			throw new AscendException(ErrorCode.INTERNAL, "Attempted to reference array ID %d of %d", arrayID, arrays.size());
		}
		int valueID = arrays.get(arrayID).getIDFromIndex(index);
		if (valueID == -1) {
			throw new AscendException(ErrorCode.INDEX, "Cannot reference uninitialized array index %d", index);
		}
		return memory.get(valueID).getValue();
	}
	
	public void mapArrayIndexToValue(Value array, int index, Value value) {
		int arrayID = (int) array.value();
		if (arrayID < 0 || arrayID > arrays.size()) {
			throw new AscendException(ErrorCode.INTERNAL, "Attempted to reference array ID %i of %i", arrayID, arrays.size());
		}
		ArrayObject obj = arrays.get(arrayID);
		if (!value.isA(obj.getType())) {
			throw new AscendException(ErrorCode.TYPE, "Cannot assign %s to %s array", value.type(), obj.getType());
		}
		int currentID = obj.getIDFromIndex(index);
		int newID = storeInMemory(value);
		obj.setIndexToID(index, newID);
		dropRef(currentID);
	}
	
	public int getArrayLength(Value array) {
		int arrayID = (int) array.value();
		if (arrayID < 0 || arrayID > arrays.size()) {
			throw new AscendException(ErrorCode.INTERNAL, "Attempted to reference array ID %i of %i", arrayID, arrays.size());
		}
		return arrays.get(arrayID).getLength();
	}
	
	public String getArrayType(Value array) {
		int arrayID = (int) array.value();
		if (arrayID < 0 || arrayID > arrays.size()) {
			throw new AscendException(ErrorCode.INTERNAL, "Attempted to reference array ID %i of %i", arrayID, arrays.size());
		}
		return arrays.get(arrayID).getType();
	}
	
	public String toString() {
		String repr = "";
		// builtinIndexEnd = 0;
		for (int i = builtinIndexEnd + 1; i < memory.size(); i++) {
			MemoryItem item = memory.get(i);
			if (item != null) {
				Value value = item.getValue();
				if (value != null) {
					repr += " " + i + ":" + value;
				}
			}
		}
		return "[" + (repr.length() > 0 ? repr.substring(1) : repr) + "]";
	}
	
	/* e.g. System.out.println -> println */
	public static String getLocalName(String varName) {
		String[] parts = varName.split("\\.");
		return parts[parts.length - 1];
	}
	
	public static boolean isPrimitive(String id) {
		for (String prim : PRIMITIVE_TYPES) {
			if (id.equals(prim)) {
				return true;
			}
		}
		return false;
	}

}