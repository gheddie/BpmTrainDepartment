package de.gravitex.bpm.traindepartment.entity;

import java.util.HashMap;
import java.util.List;

import de.gravitex.bpm.traindepartment.enumeration.DepartmentOrderState;
import de.gravitex.bpm.traindepartment.util.RailUtil;
import lombok.Data;

@Data
public class DepartmentOrder {
	
	private DepartmentOrderState departmentOrderState = DepartmentOrderState.ACTIVE;

	private Track targetTrack;
	
	private List<Waggon> waggons;

	public boolean containsAnyWaggon(List<String> waggonNumbers) {
		HashMap<String, Waggon> hashedWaggons = RailUtil.hashWaggons(waggons);
		for (String waggonNumber : waggonNumbers) {
			if (hashedWaggons.get(waggonNumber) != null) {
				return true;
			}
		}
		return false;
	}
}