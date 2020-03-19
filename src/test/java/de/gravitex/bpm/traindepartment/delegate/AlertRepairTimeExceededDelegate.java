package de.gravitex.bpm.traindepartment.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;

public class AlertRepairTimeExceededDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		execution.getProcessEngine().getRuntimeService().correlateMessage(DepartTrainProcessConstants.MSG_REPAIR_TIME_EXCEEDED,
				(String) execution.getProcessEngine().getRuntimeService().getVariable(execution.getId(),
						DepartTrainProcessConstants.VAR_DEP_PROC_BK));
	}
}