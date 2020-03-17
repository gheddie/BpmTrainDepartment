package de.gravitex.bpm.traindepartment.delegate;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import de.gravitex.bpm.traindepartment.delegate.base.TrainDepartmentJavaDelegate;
import de.gravitex.bpm.traindepartment.enumeration.RepairEvaluationResult;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonRepairInfo;

public class AbortRepairsDelegate extends TrainDepartmentJavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		// all waggons which are to be replaced will not be repaired here...
		List<WaggonRepairInfo> evaluationWaggons = getWaggonList(execution)
				.getWaggonsByEvaluationResult(RepairEvaluationResult.REPLACE_WAGGON);
		for (WaggonRepairInfo waggonRepairInfo : evaluationWaggons) {
			execution.getProcessEngine().getRuntimeService().correlateMessage(DepartTrainProcessConstants.MSG_ABORT_REPAIR,
					waggonRepairInfo.getFacilityProcessBusinessKey());
		}
	}
}