package de.gravitex.bpm.traindepartment.enumeration;

public enum WaggonState {
	
	// on initialiazation
	NOMINAL,
	
	// repair and use waggon for planned department
	REPAIR_WAGGON,
	
	// replace waggon (for a repair time assumed too long)
	REPLACE_WAGGON,
	
	// delivered as a replacement (for replacement on 'REPLACE_WAGGON' or a timed out repair)
	REPLACED,
	
	// repair time was exceeded
	REPAIR_EXCEEDED,
	
	REPAIRED,
	
	ASSUMED,
	
	TO_BE_ASSUMED;
}
