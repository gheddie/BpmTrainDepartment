package de.gravitex.bpm.traindepartment.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.gravitex.bpm.traindepartment.enumeration.WaggonErrorCode;
import lombok.Data;

@Data
public class Waggon extends RailTestEntity<Waggon> {

	private String waggonNumber;
	
	private Set<WaggonErrorCode> waggonErrorCodes;
	
	public boolean isDefect() {
		if (waggonErrorCodes == null) {
			return false;
		}
		for (WaggonErrorCode waggonErrorCode : waggonErrorCodes) {
			if (waggonErrorCode.isCritical()) {
				return true;		
			}
		}
		return false;
	}

	@Override
	public Waggon fromString(String value) {
		setWaggonNumber(getPrimaryValue(value));
		if (hasSecondaryValue(value)) {
			setWaggonErrorCodes(extractErrorCodes((String) getSecondaryValue(value)));			
		}
		return this;
	}

	private Set<WaggonErrorCode> extractErrorCodes(String value) {
		Set<WaggonErrorCode> result = new HashSet<WaggonErrorCode>();
		for (String singleErrorCode : splitValues(value)) {
			result.add(WaggonErrorCode.valueOf(singleErrorCode));
		}
		return result;
	}

	public static List<String> getWaggonNumbers(String[] waggonNumbers) {
		ArrayList<String> result = new ArrayList<String>();
		for (String waggonNumber : waggonNumbers) {
			result.add(new Waggon().fromString(waggonNumber).getWaggonNumber());
		}
		return result;
	}
}