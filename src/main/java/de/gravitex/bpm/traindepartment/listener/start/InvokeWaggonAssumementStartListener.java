package de.gravitex.bpm.traindepartment.listener.start;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class InvokeWaggonAssumementStartListener implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		// TODO

		/*
		RuntimeService runtimeService = execution.getProcessEngine().getRuntimeService();

		// TODO access to main process variables here? --> NO!!! (why?)
		DepartmentProcessData departmentProcessData = (DepartmentProcessData) runtimeService.getVariable(execution.getId(),
				DepartTrainProcessConstants.VAR_DEPARTMENT_PROCESS_DATA);

		// pass master business key
		((WaggonProcessInfo) runtimeService.getVariable(execution.getId(),
				DepartTrainProcessConstants.VAR_SINGLE_FACILITY_PROCESS_WAGGON)).setFacilityProcessBusinessKey(
						(String) runtimeService.getVariable(execution.getId(), DepartTrainProcessConstants.VAR_DEP_PROC_BK));
						*/
	}
}