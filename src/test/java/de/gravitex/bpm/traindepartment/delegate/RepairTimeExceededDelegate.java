package de.gravitex.bpm.traindepartment.delegate;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;

public class RepairTimeExceededDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		RuntimeService runtimeService = execution.getProcessEngine().getRuntimeService();
		runtimeService.correlateMessage(DepartTrainProcessConstants.MSG_REPAIR_TIME_EXCEEDED,
				(String) runtimeService.getVariable(execution.getId(),
						DepartTrainProcessConstants.VAR_DEP_PROC_BK));
	}
}