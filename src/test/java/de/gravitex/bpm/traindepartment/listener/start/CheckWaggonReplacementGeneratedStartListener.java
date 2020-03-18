package de.gravitex.bpm.traindepartment.listener.start;

import java.util.List;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.DepartmentProcessData;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public class CheckWaggonReplacementGeneratedStartListener implements ExecutionListener {

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		RuntimeService runtimeService = execution.getProcessEngine().getRuntimeService();
		List<WaggonProcessInfo> waggonReplacements = ((DepartmentProcessData) execution.getProcessEngine().getRuntimeService()
				.getVariable(execution.getId(), DepartTrainProcessConstants.VAR_DEPARTMENT_PROCESS_DATA))
						.getWaggonsByEvaluationResult(WaggonState.REPLACE_WAGGON);
		/*
		runtimeService.setVariable(execution.getId(), DepartTrainProcessConstants.VAR_WG_REPLS_GEN,
				(waggonReplacements != null && waggonReplacements.size() > 0));
				*/
		((DepartmentProcessData) runtimeService.getVariable(execution.getId(), DepartTrainProcessConstants.VAR_DEPARTMENT_PROCESS_DATA)).markReplacementWaggonsRequested();
	}
}