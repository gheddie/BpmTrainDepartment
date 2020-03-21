package de.gravitex.bpm.traindepartment.delegate.base;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;

public abstract class FacilityProcessDelegate implements JavaDelegate {

	protected String getFacilityWaggon(DelegateExecution execution) {
		return (String) execution.getVariable(DepartTrainProcessConstants.VAR_SINGLE_FACILITY_PROCESS_WAGGON);
	}
}