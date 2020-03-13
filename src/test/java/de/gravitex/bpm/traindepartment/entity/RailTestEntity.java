package de.gravitex.bpm.traindepartment.entity;

public abstract class RailTestEntity<T> {
	
	private static final String MAIN_DIVIDER = "@";
	
	private static final String SUB_DIVIDER = "#";

	public abstract T fromString(String value);

	public String getPrimaryValue(String value) {
		return split(value)[0];
	}
	
	public Object getSecondaryValue(String value) {
		return split(value)[1];
	}
	
	private String[] split(String value) {
		return value.split(MAIN_DIVIDER);
	}
	
	protected boolean hasSecondaryValue(String value) {
		return (value.contains(MAIN_DIVIDER));
	}
	
	protected String[] splitValues(String value) {
		return value.split(SUB_DIVIDER);
	}
}