package de.gravitex.bpm.traindepartment.logic;

import java.util.List;

import de.gravitex.bpm.traindepartment.entity.DepartingOrder;
import de.gravitex.bpm.traindepartment.exception.RailWayException;

public interface IRailwayStationBusinessLogic {

	DepartingOrder createDepartingOrder(List<String> waggons, String businessKey) throws RailWayException;
	
	void cancelDepartureOrder(String businessKey);
	
	void removeWaggons(List<String> waggonNumbers);
	
	int countWaggons();
	
	boolean isWaggonCritical(String waggonNumber);
	
	boolean isExitTrack(String trackNumber);
}