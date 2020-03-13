package de.gravitex.bpm.traindepartment.listener.complete;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import de.gravitex.bpm.traindepartment.delegate.WaggonRepairInfo;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;

public class PromptWaggonRepairComplementListener implements TaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		RuntimeService runtimeService = delegateTask.getProcessEngine().getRuntimeService();
		// single info stored in 'VAR_PROMPT_REPAIR_WAGGON'...
		WaggonRepairInfo info = (WaggonRepairInfo) runtimeService.getVariable(delegateTask.getExecution().getId(), DepartTrainProcessConstants.VAR_PROMPT_REPAIR_WAGGON);
		runtimeService.correlateMessage(DepartTrainProcessConstants.MSG_START_REPAIR, info.getBusinessKey());
	}
}