package de.gravitex.bpm.traindepartment.listener.complete;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

public class WaggonEvaluationProcessCompletementListener implements ExecutionListener {

	@SuppressWarnings({ "unchecked" })
	@Override
	public void notify(DelegateExecution execution) throws Exception {
		/*
		List<WaggonRepairInfo> assumedWaggons = (List<WaggonRepairInfo>) execution.getProcessEngine().getRuntimeService()
				.getVariable(execution.getId(), DepartTrainProcessConstants.VAR_ASSUMED_WAGGONS);
				*/
	}
}