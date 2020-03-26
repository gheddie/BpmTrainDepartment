package de.gravitex.bpm.traindepartment.listener.complete;

import org.camunda.bpm.engine.delegate.DelegateTask;

import de.gravitex.bpm.traindepartment.listener.base.TrainDepartmentTaskListener;
import de.gravitex.bpm.traindepartment.logic.DtpConstants;

public class ChooseExitTrackComplementListener extends TrainDepartmentTaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		getProcessData(delegateTask.getExecution()).getDepartingOrder().setExitTrack((String) delegateTask.getProcessEngine().getRuntimeService()
				.getVariable(delegateTask.getExecution().getId(), DtpConstants.NotQualified.VAR.VAR_EXIT_TRACK));
	}
}