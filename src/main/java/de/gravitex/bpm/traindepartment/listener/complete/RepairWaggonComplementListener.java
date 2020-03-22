package de.gravitex.bpm.traindepartment.listener.complete;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;
import de.gravitex.bpm.traindepartment.util.HashMapBuilder;

public class RepairWaggonComplementListener implements TaskListener {

	@SuppressWarnings("unchecked")
	@Override
	public void notify(DelegateTask delegateTask) {
		String parentInstanceBusinessKey = (String) delegateTask.getProcessEngine().getRuntimeService()
				.getVariable(delegateTask.getExecutionId(), DepartTrainProcessConstants.VAR_DEP_PROC_BK);
		// the actually repaired waggon (back to main process...)
		WaggonProcessInfo repairedWaggon = (WaggonProcessInfo) delegateTask.getProcessEngine().getRuntimeService()
				.getVariable(delegateTask.getExecution().getId(), DepartTrainProcessConstants.VAR_SINGLE_FACILITY_PROCESS_WAGGON);
		repairedWaggon.setWaggonState(WaggonState.REPAIRED);
		delegateTask.getProcessEngine().getRuntimeService().correlateMessage(DepartTrainProcessConstants.MSG_REPAIR_DONE,
				parentInstanceBusinessKey,
				HashMapBuilder.create().withValuePair(DepartTrainProcessConstants.VAR_REPAIRED_WAGGON, repairedWaggon).build());
	}
}