package de.gravitex.bpm.traindepartment.listener.complete;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.DepartmentProcessData;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public class EvaluateWaggonCompletementListener implements TaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {

		RuntimeService runtimeService = delegateTask.getProcessEngine().getRuntimeService();

		// get assumed waggon
		WaggonProcessInfo info = (WaggonProcessInfo) runtimeService.getVariable(delegateTask.getExecution().getId(),
				DtpConstants.NotQualified.VAR.VAR_ASSUMED_WAGGON);

		// set evaluation result in waggon list
		WaggonState evaluationResult = (WaggonState) runtimeService.getVariable(delegateTask.getExecution().getId(),
				DtpConstants.NotQualified.VAR.VAR_WAGGON_EVALUATION_RESULT);
		info.setWaggonState(evaluationResult);
		((DepartmentProcessData) delegateTask.getProcessEngine().getRuntimeService()
				.getVariable(delegateTask.getExecution().getId(), DtpConstants.NotQualified.VAR.VAR_DEPARTMENT_PROCESS_DATA))
						.processWaggonEvaluation(info.getWaggonNumber(), evaluationResult);
	}
}