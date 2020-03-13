package de.gravitex.bpm.traindepartment.listener.start;

import java.util.List;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

import de.gravitex.bpm.traindepartment.delegate.WaggonRepairInfo;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;

public class CheckWaggonReplacementGeneratedStartListener implements ExecutionListener {

	@SuppressWarnings("unchecked")
	@Override
	public void notify(DelegateExecution execution) throws Exception {
		RuntimeService runtimeService = execution.getProcessEngine().getRuntimeService();
		List<WaggonRepairInfo> waggonReplacements = (List<WaggonRepairInfo>) runtimeService
				.getVariable(execution.getId(), DepartTrainProcessConstants.VAR_PROMPT_REPLACE_WAGGONS_LIST);
		runtimeService.setVariable(execution.getId(), DepartTrainProcessConstants.VAR_WG_REPLS_GEN,
				(waggonReplacements != null && waggonReplacements.size() > 0));		
	}
}