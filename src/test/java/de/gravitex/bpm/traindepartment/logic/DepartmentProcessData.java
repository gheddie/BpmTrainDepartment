package de.gravitex.bpm.traindepartment.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.assertj.core.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import de.gravitex.bpm.traindepartment.util.RailTestUtil;
import lombok.Data;

@Data
public class DepartmentProcessData implements IDepartmentProcessData {

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

	public void processRepairAssumption(String waggonNumber, Integer assumedRepairDuration, String facilityProcessBusinessKey) {
		WaggonProcessInfo waggonProcessInfo = waggonRepairInfoHash.get(waggonNumber);
		waggonProcessInfo.setAssumedRepairDuration(assumedRepairDuration);
		waggonProcessInfo.setFacilityProcessBusinessKey(facilityProcessBusinessKey);
	}

	public void processWaggonEvaluation(String waggonNumber, WaggonState waggonState) {
		waggonRepairInfoHash.get(waggonNumber).setWaggonState(waggonState);
	}

	public boolean allWaggonsAssumed() {
		for (WaggonProcessInfo waggonProcessInfo : waggonRepairInfoHash.values()) {
			if (waggonProcessInfo.getAssumedRepairDuration() == null) {
				return false;
			}
		}
		return true;
	}

	public boolean allRepairsDone() {
		for (WaggonProcessInfo waggonProcessInfo : waggonRepairInfoHash.values()) {
			if (waggonProcessInfo.getWaggonState().equals(WaggonState.REPAIR_WAGGON)) {
				if (!(waggonProcessInfo.isRepaired())) {
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
			if (waggonProcessInfo.wasRepaired()) {
				count++;
			}
		}
		return count;
	}

	public void processRepairCallback(String waggonNumber) {
		waggonRepairInfoHash.get(waggonNumber).setRepaired(true);
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
		List<String> waggonNumbers = getWaggonNumbers();
		waggonPositionsOk = RailwayStationBusinessLogic.getInstance().checkTrackWaggons(exitTrack,
				waggonNumbers.toArray(new String[waggonNumbers.size()]));
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