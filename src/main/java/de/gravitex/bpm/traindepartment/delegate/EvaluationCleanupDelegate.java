package de.gravitex.bpm.traindepartment.delegate;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import de.gravitex.bpm.traindepartment.delegate.base.TrainDepartmentJavaDelegate;
import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public class EvaluationCleanupDelegate extends TrainDepartmentJavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		// all waggons which are to be replaced will not be repaired here...
		List<WaggonProcessInfo> evaluationWaggons = getProcessData(execution)
				.getWaggonsByWaggonState(WaggonState.REPLACE_WAGGON);
		for (WaggonProcessInfo waggonProcessInfo : evaluationWaggons) {
			// remove repair process
			execution.getProcessEngine().getRuntimeService().correlateMessage(
					DtpConstants.Facility.MESSAGE.MSG_ABORT_REPAIR,
					waggonProcessInfo.getFacilityProcessBusinessKey());
		}
	}
}