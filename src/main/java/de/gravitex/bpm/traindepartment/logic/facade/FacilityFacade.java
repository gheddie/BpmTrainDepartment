package de.gravitex.bpm.traindepartment.logic.facade;

import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public class FacilityFacade implements IFacilityFacade {

	@Override
	public void startFacilityProcess(WaggonProcessInfo waggonProcessInfo) {
		// TODO Auto-generated method stub
	}

	@Override
	public void executeWaggonRepair(String waggonNumber) {
		// TODO Auto-generated method stub
	}

	@Override
	public void abortWaggonRepair(String waggonNumber) {
		// TODO Auto-generated method stub
	}

	@Override
	public void callbackWaggonAssumement(String waggonNumber, int assumedHours) {
		// TODO Auto-generated method stub
	}

	@Override
	public void callbackWaggonRepair(String waggonNumber, boolean assumedTimeExceeded) {
		// TODO Auto-generated method stub
	}
}