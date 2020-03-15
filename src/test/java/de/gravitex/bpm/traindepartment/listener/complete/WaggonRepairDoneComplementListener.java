package de.gravitex.bpm.traindepartment.listener.complete;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;

public class WaggonRepairDoneComplementListener implements TaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		String parentInstanceBusinessKey = (String) delegateTask.getProcessEngine().getRuntimeService().getVariable(delegateTask.getExecutionId(),
				DepartTrainProcessConstants.VAR_DEP_PROC_BK);
		int werner = 5;
	}
}