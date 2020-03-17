package de.gravitex.bpm.traindepartment.listener.complete;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import de.gravitex.bpm.traindepartment.enumeration.RepairEvaluationResult;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.DepartProcessData;
import de.gravitex.bpm.traindepartment.logic.WaggonRepairInfo;

public class EvaluateWaggonCompletementListener implements TaskListener {

	@SuppressWarnings("unchecked")
	@Override
	public void notify(DelegateTask delegateTask) {
		
		RuntimeService runtimeService = delegateTask.getProcessEngine().getRuntimeService();
		
		// get assumed waggon
		WaggonRepairInfo info = (WaggonRepairInfo) runtimeService.getVariable(delegateTask.getExecution().getId(),
				DepartTrainProcessConstants.VAR_ASSUMED_WAGGON);
		
		// set evaluation result in waggon list
		RepairEvaluationResult evaluationResult = (RepairEvaluationResult) runtimeService
				.getVariable(delegateTask.getExecution().getId(), DepartTrainProcessConstants.VAR_WAGGON_EVALUATION_RESULT);
		info.setRepairEvaluationResult(evaluationResult);
		((DepartProcessData) delegateTask.getProcessEngine().getRuntimeService().getVariable(delegateTask.getExecution().getId(), DepartTrainProcessConstants.VAR_DEPARTMENT_PROCESS_DATA)).processWaggonEvaluation(info.getWaggonNumber(), evaluationResult);
	}
}