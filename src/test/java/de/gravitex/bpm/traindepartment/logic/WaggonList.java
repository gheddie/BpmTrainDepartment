package de.gravitex.bpm.traindepartment.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.gravitex.bpm.traindepartment.enumeration.RepairEvaluationResult;
import lombok.Data;

@Data
public class WaggonList {
	
	private WaggonList() {
		super();
	}

	private HashMap<String, WaggonRepairInfo> waggonRepairInfoHash = new HashMap<String, WaggonRepairInfo>();

	public static WaggonList fromWaggonNumbers(List<String> waggonNumbers) {
		WaggonList result = new WaggonList();
		HashMap<String, WaggonRepairInfo> waggonRepairInfoHash = new HashMap<String, WaggonRepairInfo>();
		for (String waggonNumber : waggonNumbers) {
			waggonRepairInfoHash.put(waggonNumber, WaggonRepairInfo.fromWaggonNumber(waggonNumber));
		}
		result.setWaggonRepairInfoHash(waggonRepairInfoHash);
		return result;
	}

	public List<String> getWaggonNumbers() {
		List<String> result = new ArrayList<String>();
		for (WaggonRepairInfo waggonRepairInfo : waggonRepairInfoHash.values()) {
			result.add(waggonRepairInfo.getWaggonNumber());
		}
		return result;
	}

	public void processRepairAssumption(String waggonNumber, Integer assumedRepairDuration, String facilityProcessBusinessKey) {
		WaggonRepairInfo waggonRepairInfo = waggonRepairInfoHash.get(waggonNumber);
		waggonRepairInfo.setAssumedRepairDuration(assumedRepairDuration);
		waggonRepairInfo.setFacilityProcessBusinessKey(facilityProcessBusinessKey);
	}
	
	public void processWaggonEvaluation(String waggonNumber, RepairEvaluationResult repairEvaluationResult) {
		waggonRepairInfoHash.get(waggonNumber).setRepairEvaluationResult(repairEvaluationResult);
	}

	public boolean allWaggonsAssumed() {
		for (WaggonRepairInfo waggonRepairInfo : waggonRepairInfoHash.values()) {
			if (waggonRepairInfo.getAssumedRepairDuration() == null) {
				return false;
			}
		}
		return true;
	}
	
	public boolean allRepairsDone() {
		for (WaggonRepairInfo waggonRepairInfo : waggonRepairInfoHash.values()) {
			if (waggonRepairInfo.getRepairEvaluationResult().equals(RepairEvaluationResult.REPAIR_WAGGON)) {
				if (!(waggonRepairInfo.isRepaired())) {
					return new Boolean(false);					
				}
			}
		}
		// important to return 'big' boolean, 'small' can not be serialized!!
		return new Boolean(true);
	}

	public int getRepairedWaggonCount() {
		int count = 0;
		for (WaggonRepairInfo waggonRepairInfo : waggonRepairInfoHash.values()) {
			if (waggonRepairInfo.wasRepaired()) {
				count++;				
			}
		}
		return count;
	}

	public void processRepairCallback(String waggonNumber) {
		waggonRepairInfoHash.get(waggonNumber).setRepaired(true);
	}
}