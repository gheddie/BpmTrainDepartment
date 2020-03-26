package de.gravitex.bpm.traindepartment.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import de.gravitex.bpm.traindepartment.entity.DepartingOrder;
import de.gravitex.bpm.traindepartment.entity.Track;
import de.gravitex.bpm.traindepartment.entity.Waggon;
import de.gravitex.bpm.traindepartment.enumeration.DepartmentOrderState;
import de.gravitex.bpm.traindepartment.exception.RailWayException;
import de.gravitex.bpm.traindepartment.logic.businesskey.BusinessKeyCreator;
import de.gravitex.bpm.traindepartment.logic.businesskey.DepartTrainBusinessKeyCreator;
import de.gravitex.bpm.traindepartment.logic.businesskey.RepairFacilityBusinessKeyCreator;
import de.gravitex.bpm.traindepartment.logic.businesskey.ShunterBusinessKeyCreator;

public class RailwayStationBusinessLogic implements IRailwayStationBusinessLogic {

	private static RailwayStationBusinessLogic instance;

	private StationData stationData = new StationData();

	private static final HashMap<String, BusinessKeyCreator> businessKeyCreators = new HashMap<String, BusinessKeyCreator>();
	static {
		businessKeyCreators.put(DtpConstants.DepartTrain.DEFINITION.PROCESS_DEPART_TRAIN,
				new DepartTrainBusinessKeyCreator(DtpConstants.DepartTrain.DEFINITION.PROCESS_DEPART_TRAIN));
		businessKeyCreators.put(DtpConstants.Facility.DEFINITION.PROCESS_REPAIR_FACILITY,
				new RepairFacilityBusinessKeyCreator(DtpConstants.Facility.DEFINITION.PROCESS_REPAIR_FACILITY));
		businessKeyCreators.put(DtpConstants.Shunter.DEFINITION.PROCESS_SHUNTER,
				new ShunterBusinessKeyCreator(DtpConstants.Shunter.DEFINITION.PROCESS_SHUNTER));
	}

	private RailwayStationBusinessLogic() {
		// ...
	}

	@Override
	public DepartingOrder createDepartingOrder(List<String> waggonNumbers, String businessKey) throws RailWayException {

		// no active order --> OK
		List<DepartingOrder> activeDepartureOrders = findActiveDepartureOrders();
		if ((activeDepartureOrders != null) && (activeDepartureOrders.size() > 0)) {
			throw new RailWayException("");
		}

		// all waggons must be present in station
		if (!(stationData.allWaggonsPresent(waggonNumbers))) {
			throw new RailWayException("");
		}

		// none of the waggons must be planned in active departure order!!
		for (DepartingOrder activeDepartureOrder : activeDepartureOrders) {
			if (activeDepartureOrder.containsAnyWaggon(waggonNumbers)) {
				throw new RailWayException("");
			}
		}

		// now, create a department order of state 'ACTIVE'...
		return stationData.createDepartmentOrder(businessKey);
	}

	private List<DepartingOrder> findActiveDepartureOrders() {
		List<DepartingOrder> activeOrders = new ArrayList<DepartingOrder>();
		for (DepartingOrder departingOrder : stationData.getDepartingOrders().values()) {
			if (departingOrder.getDepartmentOrderState().equals(DepartmentOrderState.ACTIVE)) {
				activeOrders.add(departingOrder);
			}
		}
		return activeOrders;
	}

	public String generateBusinessKey(String processDefinitionKey, HashMap<String, Object> additionalValues,
			String parentBusinessKey) {
		return businessKeyCreators.get(processDefinitionKey).generate(additionalValues, parentBusinessKey);
	}

	@Override
	public void cancelDepartureOrder(String businessKey) {
		stationData.getDepartingOrders().get(businessKey).setDepartmentOrderState(DepartmentOrderState.CANCELLED);
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
		return stationData.isWaggonCritical(waggonNumber);
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
		// TODO ?!?
		return this;
	}

	public void reset() {
		stationData.reset();
	}

	public void print(String header, boolean showWaggonDefects) {
		stationData.print(header, showWaggonDefects);
	}

	public void addWaggonsToTrack(String trackNumber, List<String> waggonNumbers) {
		stationData.addWaggonsToTrack(trackNumber, waggonNumbers);
	}

	public boolean checkTrackWaggons(String trackNumber, String... waggonNumbers) {
		return stationData.checkTrackWaggons(trackNumber, waggonNumbers);
	}
	
	public List<Waggon> getTrackWaggons(String trackNumber) {
		return stationData.getTrackWaggons(trackNumber);
	}

	public ProcessInstance resolveProcessInstance(List<ProcessInstance> processInstances, String aProcessDefinitionKey,
			String value, ProcessInstance parentInstance) {
		return BusinessKeyCreator.resolveProcessInstance(processInstances, aProcessDefinitionKey, value, parentInstance);
	}
	
	public static DepartmentProcessData getProcessData(DelegateExecution execution) {
		return (DepartmentProcessData) execution.getProcessEngine().getRuntimeService().getVariable(execution.getId(),
				DtpConstants.DepartTrain.VAR.VAR_DEPARTMENT_PROCESS_DATA);
	}

	public List<String> getAllWaggonNumbers() {
		return stationData.getAllWaggonNumbers();
	}

	public String getAdditionalValueForProcessInstance(ProcessInstance processInstance) {
		return BusinessKeyCreator.getAdditionalKey(processInstance.getBusinessKey());
	}

	public DepartingOrder getDepartingOrder(String businessKey) {
		return stationData.getDepartingOrder(businessKey);
	}

	public boolean isTrackPresent(String trackNumber) {
		return stationData.findTrack(trackNumber) != null;
	}
}