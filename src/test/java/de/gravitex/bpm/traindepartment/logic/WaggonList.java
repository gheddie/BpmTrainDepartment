package de.gravitex.bpm.traindepartment.logic;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class WaggonList {
	
	private WaggonList() {
		super();
	}

	private List<WaggonRepairInfo> waggonRepairInfos = new ArrayList<WaggonRepairInfo>();

	public static WaggonList fromWaggonNumbers(List<String> waggonNumbers) {
		WaggonList result = new WaggonList();
		List<WaggonRepairInfo> waggonRepairInfoList = new ArrayList<WaggonRepairInfo>();
		for (String waggonNumber : waggonNumbers) {
			waggonRepairInfoList.add(WaggonRepairInfo.fromWaggonNumber(waggonNumber));
		}
		result.setWaggonRepairInfos(waggonRepairInfoList);
		return result;
	}

	public List<String> getWaggonNumbers() {
		List<String> result = new ArrayList<String>();
		for (WaggonRepairInfo waggonRepairInfo : waggonRepairInfos) {
			result.add(waggonRepairInfo.getWaggonNumber());
		}
		return result;
	}
}