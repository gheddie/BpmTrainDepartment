package de.gravitex.bpm.traindepartment.listener.start;

import java.util.List;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

import de.gravitex.bpm.traindepartment.enumeration.RepairEvaluationResult;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonList;
import de.gravitex.bpm.traindepartment.logic.WaggonRepairInfo;

public class CheckWaggonReplacementGeneratedStartListener implements ExecutionListener {

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		RuntimeService runtimeService = execution.getProcessEngine().getRuntimeService();

		/*
		 * List<WaggonRepairInfo> waggonReplacements = (List<WaggonRepairInfo>)
		 * runtimeService .getVariable(execution.getId(),
		 * DepartTrainProcessConstants.VAR_PROMPT_REPLACE_WAGGONS_LIST);
		 */

		List<WaggonRepairInfo> waggonReplacements = ((WaggonList) execution.getProcessEngine().getRuntimeService()
				.getVariable(execution.getId(), DepartTrainProcessConstants.VAR_WAGGON_LIST))
						.getWaggonsByEvaluationResult(RepairEvaluationResult.REPLACE_WAGGON);

		runtimeService.setVariable(execution.getId(), DepartTrainProcessConstants.VAR_WG_REPLS_GEN,
				(waggonReplacements != null && waggonReplacements.size() > 0));
	}
}