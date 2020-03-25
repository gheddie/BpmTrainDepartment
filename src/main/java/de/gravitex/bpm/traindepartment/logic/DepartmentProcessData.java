package de.gravitex.bpm.traindepartment.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.gravitex.bpm.traindepartment.entity.DepartingOrder;
import de.gravitex.bpm.traindepartment.entity.Track;
import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import lombok.Data;

@Data
public class DepartmentProcessData implements IDepartmentProcessData {

	public static final Logger logger = Logger.getLogger(DepartmentProcessData.class);

	private HashMap<String, WaggonProcessInfo> waggons = new HashMap<String, WaggonProcessInfo>();

	// wurden 'waggon' replacements angefragt?
	// TODO needed?
	private boolean replacementWaggonsRequested = false;

	private DepartingOrder departingOrder;

	// TODO needed?
	private boolean waggonPositionsOk;

	private DepartmentProcessData() {
		super();
	}

	public static DepartmentProcessData fromWaggonNumbers(List<String> waggonNumbers) {
		DepartmentProcessData result = new DepartmentProcessData();
		HashMap<String, WaggonProcessInfo> waggonRepairInfoHash = new HashMap<String, WaggonProcessInfo>();
		for (String waggonNumber : waggonNumbers) {
			waggonRepairInfoHash.put(waggonNumber, WaggonProcessInfo.fromValues(waggonNumber));
		}
		result.setWaggons(waggonRepairInfoHash);
		return result;
	}

	@JsonIgnore
	public Collection<WaggonProcessInfo> getWaggonList() {
		return waggons.values();
	}
	
	public Collection<WaggonProcessInfo> getAssumedWaggons() {
		return getWaggonsByWaggonState(WaggonState.ASSUMED);
	}

	public List<String> getWaggonNumbers() {
		List<String> result = new ArrayList<String>();
		for (WaggonProcessInfo waggonProcessInfo : waggons.values()) {
			result.add(waggonProcessInfo.getWaggonNumber());
		}
		return result;
	}

	public void processRepairAssumption(String waggonNumber, Integer assumedRepairDuration, String masterProcessBusinessKey) {
		WaggonProcessInfo waggonProcessInfo = waggons.get(waggonNumber);
		waggonProcessInfo.setAssumedRepairDuration(assumedRepairDuration);
		waggonProcessInfo.setWaggonState(WaggonState.ASSUMED);
	}

	public void processWaggonEvaluation(String waggonNumber, WaggonState waggonState) {
		waggons.get(waggonNumber).setWaggonState(waggonState);
	}

	public boolean allWaggonsAssumed() {
		for (WaggonProcessInfo waggonProcessInfo : waggons.values()) {
			if (waggonProcessInfo.getWaggonState().equals(WaggonState.TO_BE_ASSUMED)) {
				logger.info("waggon [" + waggonProcessInfo.getWaggonNumber() + "] was NOT assumed [state:"+WaggonState.TO_BE_ASSUMED+"] --> returning false.");
				return false;
			}
		}
		logger.info("all waggons were assumed...");
		return true;
	}

	public boolean allRepairsDone() {
		logger.info("checking all repairs done...");
		for (WaggonProcessInfo waggonProcessInfo : waggons.values()) {
			if (waggonProcessInfo.getWaggonState().equals(WaggonState.REPAIR_WAGGON)) {
				if (!(waggonProcessInfo.repairDone())) {
					logger.info("waggon " + waggonProcessInfo.getWaggonNumber() + " has not been repaired.");
					return false;
				}
			}
		}
		return true;
	}

	@JsonIgnore
	public int getRepairedWaggonCount() {
		int count = 0;
		for (WaggonProcessInfo waggonProcessInfo : waggons.values()) {
			if (waggonProcessInfo.repairDone()) {
				count++;
			}
		}
		return count;
	}

	public void processRepairCallback(WaggonProcessInfo waggonProcessInfo) {
		logger.info("updating waggon state of waggon " + waggonProcessInfo.getWaggonNumber() + " to :"
				+ waggonProcessInfo.getWaggonState());
		waggons.get(waggonProcessInfo.getWaggonNumber()).setWaggonState(waggonProcessInfo.getWaggonState());
	}

	public List<WaggonProcessInfo> getWaggonsEvaluatedAsRepair() {
		return getWaggonsByWaggonState(WaggonState.REPAIR_WAGGON);
	}

	public List<WaggonProcessInfo> getWaggonsEvaluatedAsReplacement() {
		return getWaggonsByWaggonState(WaggonState.REPLACE_WAGGON);
	}

	public List<WaggonProcessInfo> getWaggonsByWaggonState(WaggonState waggonState) {
		List<WaggonProcessInfo> result = new ArrayList<WaggonProcessInfo>();
		for (WaggonProcessInfo waggonProcessInfo : waggons.values()) {
			if (waggonProcessInfo.getWaggonState().equals(waggonState)) {
				result.add(waggonProcessInfo);
			}
		}
		return result;
	}

	public boolean exitTrackEmpty() {
		return true;
	}

	public void markReplacementWaggonsRequested() {
		replacementWaggonsRequested = true;
	}

	public void setExitTrack(String exitTrack) {
		departingOrder.setExitTrack(exitTrack);
	}

	public void checkWaggonPositions() {
		// waggons must be 'alone' on the wxit track
		// TODO
		waggonPositionsOk = true;
		/*
		 * List<String> waggonNumbers = getWaggonNumbers(); waggonPositionsOk =
		 * RailwayStationBusinessLogic.getInstance().checkTrackWaggons(exitTrack,
		 * waggonNumbers.toArray(new String[waggonNumbers.size()]));
		 */
	}

	public void removeWaggon(String waggonNumber) {
		waggons.remove(waggonNumber);
	}

	public void addWaggon(WaggonProcessInfo waggonProcessInfo) {
		waggons.put(waggonProcessInfo.getWaggonNumber(), waggonProcessInfo);
	}

	@JsonIgnore
	public String getExitTrack() {
		return departingOrder.getExitTrack();
	}
}