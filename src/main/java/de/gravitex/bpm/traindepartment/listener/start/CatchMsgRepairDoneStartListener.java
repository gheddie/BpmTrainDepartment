package de.gravitex.bpm.traindepartment.listener.start;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

import de.gravitex.bpm.traindepartment.logic.DepartmentProcessData;
import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public class CatchMsgRepairDoneStartListener implements ExecutionListener {

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		WaggonProcessInfo repairedWaggon = (WaggonProcessInfo) execution
				.getVariable(DtpConstants.NotQualified.VAR.VAR_REPAIRED_WAGGON);
		// update process waggon
		((DepartmentProcessData) execution.getVariable(DtpConstants.DepartTrain.VAR.VAR_DEPARTMENT_PROCESS_DATA))
				.processWaggonCallback(repairedWaggon);
	}
}