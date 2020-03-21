package de.gravitex.bpm.traindepartment.delegate;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import de.gravitex.bpm.traindepartment.delegate.base.TrainDepartmentJavaDelegate;
import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public class EvaluationCleanupDelegate extends TrainDepartmentJavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		// all waggons which are to be replaced will not be repaired here...
		List<WaggonProcessInfo> evaluationWaggons = getProcessData(execution)
				.getWaggonsByEvaluationResult(WaggonState.REPLACE_WAGGON);
		for (WaggonProcessInfo waggonProcessInfo : evaluationWaggons) {
			// remove repair process
			execution.getProcessEngine().getRuntimeService().correlateMessage(DepartTrainProcessConstants.MSG_ABORT_REPAIR,
					waggonProcessInfo.getFacilityProcessBusinessKey());
			// remove from data model -> NO, still needed to prompt waggon replacements later!!
			// getDepartmentProcessData(execution).removeWaggon(waggonProcessInfo.getWaggonNumber());
		}
	}
}