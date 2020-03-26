package de.gravitex.bpm.traindepartment.listener.complete;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;
import de.gravitex.bpm.traindepartment.util.HashMapBuilder;

public class RepairWaggonComplementListener implements TaskListener {

	@SuppressWarnings("unchecked")
	@Override
	public void notify(DelegateTask delegateTask) {
		String parentInstanceBusinessKey = (String) delegateTask.getProcessEngine().getRuntimeService()
				.getVariable(delegateTask.getExecutionId(), DtpConstants.NotQualified.VAR.VAR_DEP_PROC_BK);
		// the actually repaired waggon (back to main process...)
		WaggonProcessInfo repairedWaggon = (WaggonProcessInfo) delegateTask.getProcessEngine().getRuntimeService()
				.getVariable(delegateTask.getExecution().getId(),
						DtpConstants.Facility.VAR.VAR_SINGLE_FACILITY_PROCESS_WAGGON);
		repairedWaggon.setWaggonState(WaggonState.OK);
		delegateTask.getProcessEngine().getRuntimeService().correlateMessage(
				DtpConstants.DepartTrain.MESSAGE.MSG_REPAIR_DONE, parentInstanceBusinessKey, HashMapBuilder.create()
						.withValuePair(DtpConstants.NotQualified.VAR.VAR_REPAIRED_WAGGON, repairedWaggon).build());
	}
}