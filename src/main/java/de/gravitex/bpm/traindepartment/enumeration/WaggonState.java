package de.gravitex.bpm.traindepartment.enumeration;

public enum WaggonState {
	
	// on initialiazation
	UNDEFINED(false),
	
	// repair and use waggon for planned department
	REPAIR_WAGGON(true),
	
	// replace waggon (for a repair time assumed too long)
	REPLACE_WAGGON(false),
	
	// delivered as a replacement (for replacement on 'REPLACE_WAGGON' or a timed out repair)
	REPLACED(true),
	
	// repair time was exceeded
	REPAIR_EXCEEDED(false),
	
	REPAIRED(true);
	
	private boolean usable;
	
	private WaggonState(final boolean aUsable) {
        this.usable = aUsable;
    }

	public boolean isUsable() {
		return usable;
	}
}
