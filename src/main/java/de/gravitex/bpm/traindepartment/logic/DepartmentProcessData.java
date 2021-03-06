package de.gravitex.bpm.traindepartment.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.gravitex.bpm.traindepartment.delegate.AllAssumementsDoneDelegate;
import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import lombok.Data;

@Data
public class DepartmentProcessData implements IDepartmentProcessData {

	public static final Logger logger = Logger.getLogger(DepartmentProcessData.class);

	private HashMap<String, WaggonProcessInfo> waggonRepairInfoHash = new HashMap<String, WaggonProcessInfo>();

	// wurden 'waggon' replacements angefragt?
	private boolean replacementWaggonsRequested = false;

	private String exitTrack;

	private boolean waggonPositionsOk;;

	private DepartmentProcessData() {
		super();
	}

	public static DepartmentProcessData fromWaggonNumbers(List<String> waggonNumbers) {
		DepartmentProcessData result = new DepartmentProcessData();
		HashMap<String, WaggonProcessInfo> waggonRepairInfoHash = new HashMap<String, WaggonProcessInfo>();
		for (String waggonNumber : waggonNumbers) {
			waggonRepairInfoHash.put(waggonNumber, WaggonProcessInfo.fromValues(waggonNumber));
		}
		result.setWaggonRepairInfoHash(waggonRepairInfoHash);
		return result;
	}

	@JsonIgnore
	public Collection<WaggonProcessInfo> getWaggons() {
		return waggonRepairInfoHash.values();
	}

	public List<String> getWaggonNumbers() {
		List<String> result = new ArrayList<String>();
		for (WaggonProcessInfo waggonProcessInfo : waggonRepairInfoHash.values()) {
			result.add(waggonProcessInfo.getWaggonNumber());
		}
		return result;
	}

	public void processRepairAssumption(String waggonNumber, Integer assumedRepairDuration, String masterProcessBusinessKey) {
		WaggonProcessInfo waggonProcessInfo = waggonRepairInfoHash.get(waggonNumber);
		waggonProcessInfo.setAssumedRepairDuration(assumedRepairDuration);
		// waggonProcessInfo.setFacilityProcessBusinessKey(masterProcessBusinessKey);
	}

	public void processWaggonEvaluation(String waggonNumber, WaggonState waggonState) {
		waggonRepairInfoHash.get(waggonNumber).setWaggonState(waggonState);
	}

	public boolean allWaggonsAssumed() {
		for (WaggonProcessInfo waggonProcessInfo : waggonRepairInfoHash.values()) {
			if (waggonProcessInfo.getAssumedRepairDuration() == null) {
				logger.info("waggon [" + waggonProcessInfo.getWaggonNumber() + "] was NOT assumed --> returning false.");
				return false;
			}
		}
		logger.info("all waggons were assumed...");
		return true;
	}

	public boolean allRepairsDone() {
		logger.info("checking all repairs done...");
		for (WaggonProcessInfo waggonProcessInfo : waggonRepairInfoHash.values()) {
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
		for (WaggonProcessInfo waggonProcessInfo : waggonRepairInfoHash.values()) {
			if (waggonProcessInfo.repairDone()) {
				count++;
			}
		}
		return count;
	}

	public void processRepairCallback(WaggonProcessInfo waggonProcessInfo) {
		logger.info("updating waggon state of waggon " + waggonProcessInfo.getWaggonNumber() + " to :"
				+ waggonProcessInfo.getWaggonState());
		waggonRepairInfoHash.get(waggonProcessInfo.getWaggonNumber()).setWaggonState(waggonProcessInfo.getWaggonState());
	}

	public List<WaggonProcessInfo> getWaggonsEvaluatedAsRepair() {
		return getWaggonsByEvaluationResult(WaggonState.REPAIR_WAGGON);
	}

	public List<WaggonProcessInfo> getWaggonsEvaluatedAsReplacement() {
		return getWaggonsByEvaluationResult(WaggonState.REPLACE_WAGGON);
	}

	public List<WaggonProcessInfo> getWaggonsByEvaluationResult(WaggonState waggonState) {
		List<WaggonProcessInfo> result = new ArrayList<WaggonProcessInfo>();
		for (WaggonProcessInfo waggonProcessInfo : waggonRepairInfoHash.values()) {
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
		this.exitTrack = exitTrack;
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

	public List<String> getUsableWaggonNumbers() {
		List<String> usableWaggonNumbers = new ArrayList<String>();
		for (WaggonProcessInfo waggonProcessInfo : getUsableWaggons()) {
			usableWaggonNumbers.add(waggonProcessInfo.getWaggonNumber());
		}
		return usableWaggonNumbers;
	}

	public List<WaggonProcessInfo> getUsableWaggons() {
		List<WaggonProcessInfo> usableWaggons = new ArrayList<WaggonProcessInfo>();
		for (WaggonProcessInfo waggonProcessInfo : waggonRepairInfoHash.values()) {
			if (waggonProcessInfo.isUsable()) {
				usableWaggons.add(waggonProcessInfo);
			}
		}
		return usableWaggons;
	}

	public void removeWaggon(String waggonNumber) {
		waggonRepairInfoHash.remove(waggonNumber);
	}

	public void addWaggon(WaggonProcessInfo waggonProcessInfo) {
		waggonRepairInfoHash.put(waggonProcessInfo.getWaggonNumber(), waggonProcessInfo);
	}
}