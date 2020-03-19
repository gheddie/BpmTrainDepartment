package de.gravitex.bpm.traindepartment.listener.complete;

import org.camunda.bpm.engine.delegate.DelegateTask;

import de.gravitex.bpm.traindepartment.listener.base.TrainDepartmentTaskListener;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;

public class AlertRepairTimeExceededComplementListener extends TrainDepartmentTaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		delegateTask.getProcessEngine().getRuntimeService().correlateMessage(DepartTrainProcessConstants.MSG_REPAIR_TIME_EXCEEDED,
				(String) delegateTask.getProcessEngine().getRuntimeService().getVariable(delegateTask.getExecutionId(),
						DepartTrainProcessConstants.VAR_DEP_PROC_BK));
	}
}