package de.gravitex.bpm.traindepartment.listener.complete;

import org.camunda.bpm.engine.delegate.DelegateTask;

import de.gravitex.bpm.traindepartment.listener.base.TrainDepartmentTaskListener;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;

public class ChooseExitTrackComplementListener extends TrainDepartmentTaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		getDepartmentProcessData(delegateTask.getExecution()).setExitTrack((String) delegateTask.getProcessEngine().getRuntimeService()
				.getVariable(delegateTask.getExecution().getId(), DepartTrainProcessConstants.VAR_EXIT_TRACK));
	}
}