package de.gravitex.bpm.traindepartment.enumeration;

public enum WaggonState {
	
	// on initialiazation
	UNDEFINED(false),
	
	// repair and use waggon for planned department
	REPAIR_WAGGON(true),
	
	// replace waggon
	REPLACE_WAGGON(false),
	
	// delivered as a replacement
	WAS_REPLACED(true);
	
	private boolean usable;
	
	private WaggonState(final boolean aUsable) {
        this.usable = aUsable;
    }

	public boolean isUsable() {
		return usable;
	}
}
