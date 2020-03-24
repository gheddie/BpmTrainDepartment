package de.gravitex.bpm.traindepartment.util;

import java.util.HashMap;

public class HashMapBuilder<X, Y> {
	
	private HashMap<X, Y> values = new HashMap<X, Y>();

	private HashMapBuilder() {
		// ...
	}

	@SuppressWarnings("rawtypes")
	public static HashMapBuilder create() {
		return new HashMapBuilder<Object, Object>();
	}

	public HashMapBuilder<X, Y> withValuePair(X key, Y value) {
		values.put(key, value);
		return this;
	}

	public HashMap<X, Y> build() {
		return values;
	}
}