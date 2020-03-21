package de.gravitex.bpm.traindepartment.listener.complete;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.util.HashMapBuilder;

public class RepairWaggonComplementListener implements TaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		String parentInstanceBusinessKey = (String) delegateTask.getProcessEngine().getRuntimeService()
				.getVariable(delegateTask.getExecutionId(), DepartTrainProcessConstants.VAR_DEP_PROC_BK);
		// the actually repaired waggon
		String repairedWaggon = (String) delegateTask.getProcessEngine().getRuntimeService()
				.getVariable(delegateTask.getExecution().getId(), DepartTrainProcessConstants.VAR_SINGLE_FACILITY_PROCESS_WAGGON);
		delegateTask.getProcessEngine().getRuntimeService().correlateMessage(DepartTrainProcessConstants.MSG_REPAIR_DONE,
				parentInstanceBusinessKey, HashMapBuilder.create().withValuePair(DepartTrainProcessConstants.VAR_REPAIRED_WAGGON, repairedWaggon).build());
	}
}