package de.gravitex.bpm.traindepartment.enumeration;

public enum WaggonState {
	
	// on initialiazation
	UNDEFINED(false),
	
	// repair and use waggon for planned department
	REPAIR_WAGGON(true),
	
	// replace waggon
	REPLACE_WAGGON(false),
	
	// delivered as a replacement
	WAGGON_REPLACED(true),
	
	REPAIR_TIMEOUT(false);
	
	private boolean usable;
	
	private WaggonState(final boolean aUsable) {
        this.usable = aUsable;
    }

	public boolean isUsable() {
		return usable;
	}
}
