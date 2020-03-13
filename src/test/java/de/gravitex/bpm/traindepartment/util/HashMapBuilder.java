package de.gravitex.bpm.traindepartment.util;

import java.util.HashMap;

public class HashMapBuilder {
	
	private HashMap<String, Object> values = new HashMap<String, Object>();

	private HashMapBuilder() {
		// ...
	}

	public static HashMapBuilder create() {
		return new HashMapBuilder();
	}

	public HashMapBuilder withValuePair(String key, Object value) {
		values.put(key, value);
		return this;
	}

	public HashMap<String, Object> build() {
		return values;
	}
}