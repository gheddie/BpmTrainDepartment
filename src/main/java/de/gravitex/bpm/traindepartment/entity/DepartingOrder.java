package de.gravitex.bpm.traindepartment.entity;

import java.util.HashMap;
import java.util.List;

import de.gravitex.bpm.traindepartment.enumeration.DepartmentOrderState;
import de.gravitex.bpm.traindepartment.util.RailUtil;
import lombok.Data;

@Data
public class DepartingOrder {
	
	private DepartmentOrderState departmentOrderState = DepartmentOrderState.ACTIVE;
	
	private String orderId;

	private String exitTrack;
	
	private String replacementTrack;
	
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