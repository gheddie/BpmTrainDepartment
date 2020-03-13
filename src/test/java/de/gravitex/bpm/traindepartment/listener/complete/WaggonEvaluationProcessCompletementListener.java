package de.gravitex.bpm.traindepartment.listener.complete;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

import de.gravitex.bpm.traindepartment.delegate.WaggonRepairInfo;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;

public class WaggonEvaluationProcessCompletementListener implements ExecutionListener {

	@SuppressWarnings({ "unchecked" })
	@Override
	public void notify(DelegateExecution execution) throws Exception {
		List<WaggonRepairInfo> assumedWaggons = (List<WaggonRepairInfo>) execution.getProcessEngine().getRuntimeService()
				.getVariable(execution.getId(), DepartTrainProcessConstants.VAR_ASSUMED_WAGGONS);
	}
}