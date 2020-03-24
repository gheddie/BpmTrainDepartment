package de.gravitex.bpm.traindepartment.logic.facade;

import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public interface IFacilityFacade {
	
	// ---------------------------------------------------------------------
	// --- main process -> facility
	// ---------------------------------------------------------------------
	
	void startFacilityProcess(WaggonProcessInfo waggonProcessInfo);
	
	void executeWaggonRepair(String waggonNumber);
	
	void abortWaggonRepair(String waggonNumber);
	
	// ---------------------------------------------------------------------
	// --- facility -> main process
	// ---------------------------------------------------------------------

	void callbackWaggonAssumement(String waggonNumber, int assumedHours);
	
	void callbackWaggonRepair(String waggonNumber, boolean assumedTimeExceeded);
}