package de.gravitex.bpm.traindepartment.enumeration;

public enum WaggonState {
	
	// on initialiazation
	OK,
	
	// repair and use waggon for planned department
	REPAIR_WAGGON,
	
	// replace waggon (for a repair time assumed too long)
	REPLACE_WAGGON,
	
	// repair time was exceeded
	REPAIR_TIME_EXCEEDED,
	
	ASSUMED,
	
	TO_BE_ASSUMED;
}
