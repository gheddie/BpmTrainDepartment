package de.gravitex.bpm.traindepartment.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.gravitex.bpm.traindepartment.entity.DepartmentOrder;
import de.gravitex.bpm.traindepartment.entity.Track;
import de.gravitex.bpm.traindepartment.enumeration.DepartmentOrderState;
import de.gravitex.bpm.traindepartment.exception.RailWayException;

public class RailwayStationBusinessLogic implements IRailwayStationBusinessLogic {

	private static RailwayStationBusinessLogic instance;

	private StationData stationData = new StationData();

	private static final Random random = new Random();

	private RailwayStationBusinessLogic() {
		// ...
	}

	@Override
	public void createDepartureOrder(List<String> waggonNumbers, String businessKey) throws RailWayException {
		
		// no active order --> OK
		List<DepartmentOrder> activeDepartureOrders = findActiveDepartureOrders();
		if ((activeDepartureOrders != null) && (activeDepartureOrders.size() > 0)) {
			throw new RailWayException("");
		}
		
		// all waggons must be present in station
		if (!(stationData.allWaggonsPresent(waggonNumbers))) {
			throw new RailWayException("");
		}
		
		// none of the waggons must be planned in active departure order!!
		for (DepartmentOrder activeDepartureOrder : activeDepartureOrders) {
			if (activeDepartureOrder.containsAnyWaggon(waggonNumbers)) {
				throw new RailWayException("");		
			}
		}
		
		// now, create a department order of state 'ACTIVE'...
		stationData.createDepartmentOrder(businessKey);
	}

	private List<DepartmentOrder> findActiveDepartureOrders() {
		List<DepartmentOrder> activeOrders = new ArrayList<DepartmentOrder>();
		for (DepartmentOrder departmentOrder : stationData.getDepartmentOrders().values()) {
			if (departmentOrder.getDepartmentOrderState().equals(DepartmentOrderState.ACTIVE)) {
				activeOrders.add(departmentOrder);
			}
		}
		return activeOrders;
	}

	public String generateBusinessKey(String processDefinitionKey) {
		String result = processDefinitionKey + "_" + String.valueOf(System.currentTimeMillis()) + "_" + String.valueOf(random.nextInt(1000));
		return result;
	}
	
	@Override
	public void cancelDepartureOrder(String businessKey) {
		stationData.getDepartmentOrders().get(businessKey).setDepartmentOrderState(DepartmentOrderState.CANCELLED);
	}
	
	public int countWaggons() {
		return stationData.getAllWaggons().size();
	}
	
	@Override
	public void removeWaggons(List<String> waggonNumbers) {
		for (String waggonNumber : waggonNumbers) {
			stationData.removeWaggon(waggonNumber);
		}
	}
	
	@Override
	public boolean isWaggonCritical(String waggonNumber) {
		boolean critical = stationData.isWaggonCritical(waggonNumber);
		return critical;
	}
	
	public boolean isExitTrack(String trackNumber) {
		Track track = stationData.findTrack(trackNumber);
		if (track == null) {
			return false;
		}
		return track.isExitTrack();
	}

	// ---

	public static RailwayStationBusinessLogic getInstance() {
		if (instance == null) {
			instance = new RailwayStationBusinessLogic();
		}
		return instance;
	}

	public RailwayStationBusinessLogic withTracks(String... trackNumbers) {
		for (String track : trackNumbers) {
			stationData.addTrack(track);
		}
		return this;
	}
	
	public RailwayStationBusinessLogic withWaggons(String trackNumber, String... waggonNumbers) {
		stationData.addWaggons(trackNumber, waggonNumbers);
		return this;
	}
	
	public RailwayStationBusinessLogic withRoles(String... roles) {
		return this;
	}

	public void reset() {
		stationData.reset();
	}

	public void print(boolean showWaggonDefects) {
		stationData.print(showWaggonDefects);
	}

	public void addWaggonsToTrack(String trackNumber, List<String> waggonNumbers) {
		stationData.addWaggonsToTrack(trackNumber, waggonNumbers);
	}

	public boolean checkTrackWaggons(String trackNumber, String... waggonNumbers) {
		return stationData.checkTrackWaggons(trackNumber, waggonNumbers);
	}
}