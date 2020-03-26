package de.gravitex.bpm.traindepartment.logic;

import java.io.Serializable;

import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import lombok.Data;

@Data
public class WaggonProcessInfo implements Serializable {

	private static final long serialVersionUID = -4796291683290246151L;

	private WaggonProcessInfo() {
		// ...
	}
	
	private String replacementForWaggon;

	private String waggonNumber;

	// hours
	private Integer assumedRepairDuration = null;

	// the business key of the repair process
	private String facilityProcessBusinessKey;

	private WaggonState waggonState = WaggonState.NOMINAL;

	public static WaggonProcessInfo fromValues(String waggonNumber) {
		WaggonProcessInfo fromValues = fromValues(waggonNumber, null, null);
		fromValues.setWaggonState(WaggonState.NOMINAL);
		return fromValues;
	}

	public static WaggonProcessInfo fromValues(String waggonNumber, Integer assumedRepairDuration, String businessKey) {
		WaggonProcessInfo waggonProcessInfo = new WaggonProcessInfo();
		waggonProcessInfo.setWaggonNumber(waggonNumber);
		waggonProcessInfo.setAssumedRepairDuration(assumedRepairDuration);
		// waggonProcessInfo.setFacilityProcessBusinessKey(businessKey);
		return waggonProcessInfo;
	}
	
	public static WaggonProcessInfo fromValues(String waggonNumber, WaggonState waggonState) {
		WaggonProcessInfo waggonProcessInfo = new WaggonProcessInfo();
		waggonProcessInfo.setWaggonNumber(waggonNumber);
		waggonProcessInfo.setWaggonState(waggonState);
		return waggonProcessInfo;
	}

	public boolean repairDone() {
		return (waggonState.equals(WaggonState.REPAIRED));
	}

	public static WaggonProcessInfo fromValues(String aWaggonNumber, String aReplacementForWaggon) {
		WaggonProcessInfo waggon = fromValues(aWaggonNumber);
		waggon.setReplacementForWaggon(aReplacementForWaggon);
		return waggon;
	}
}