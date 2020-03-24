package de.gravitex.bpm.traindepartment.delegate.base;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public abstract class FacilityProcessDelegate implements JavaDelegate {

	protected WaggonProcessInfo getFacilityWaggon(DelegateExecution execution) {
		return (WaggonProcessInfo) execution
				.getVariable(DtpConstants.Facility.VAR.VAR_SINGLE_FACILITY_PROCESS_WAGGON);
	}
}