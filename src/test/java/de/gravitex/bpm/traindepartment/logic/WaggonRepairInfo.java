package de.gravitex.bpm.traindepartment.logic;

import java.io.Serializable;

import de.gravitex.bpm.traindepartment.enumeration.RepairEvaluationResult;
import lombok.Data;

@Data
public class WaggonRepairInfo implements Serializable {
	
	private static final long serialVersionUID = -4796291683290246151L;

	private WaggonRepairInfo() {
		// ...
	}

	private String waggonNumber;
	
	// hours
	private Integer assumedRepairDuration = null;
	
	// the business key of the repair process
	private String facilityProcessBusinessKey;
	
	private RepairEvaluationResult repairEvaluationResult = RepairEvaluationResult.UNDEFINED;
	
	public static WaggonRepairInfo fromWaggonNumber(String waggonNumber) {
		WaggonRepairInfo fromValues = fromValues(waggonNumber, null, null);
		fromValues.setRepairEvaluationResult(RepairEvaluationResult.UNDEFINED);
		return fromValues;
	}

	public static WaggonRepairInfo fromValues(String waggonNumber, Integer assumedRepairDuration, String businessKey) {
		WaggonRepairInfo waggonRepairInfo = new WaggonRepairInfo();
		waggonRepairInfo.setWaggonNumber(waggonNumber);
		waggonRepairInfo.setAssumedRepairDuration(assumedRepairDuration);
		waggonRepairInfo.setFacilityProcessBusinessKey(businessKey);
		return waggonRepairInfo;
	}
}