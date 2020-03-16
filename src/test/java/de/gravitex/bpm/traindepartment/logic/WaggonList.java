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

	public void processRepairAssumption(String waggonNumber, RepairEvaluationResult repairEvaluationResult) {
		waggonRepairInfoHash.get(waggonNumber).setRepairEvaluationResult(repairEvaluationResult);
	}

	public boolean allWaggonsAssumed() {
		for (WaggonRepairInfo waggonRepairInfo : waggonRepairInfoHash.values()) {
			if (waggonRepairInfo.getRepairEvaluationResult().equals(RepairEvaluationResult.UNDEFINED)) {
				return false;
			}
		}
		return true;
	}
}