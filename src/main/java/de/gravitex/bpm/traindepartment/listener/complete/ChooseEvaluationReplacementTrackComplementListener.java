package de.gravitex.bpm.traindepartment.listener.complete;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateTask;

import de.gravitex.bpm.traindepartment.listener.base.TrainDepartmentTaskListener;
import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;

public class ChooseEvaluationReplacementTrackComplementListener extends TrainDepartmentTaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		String replacementTrack = (String) delegateTask.getExecution()
				.getVariable(DtpConstants.DepartTrain.VAR.VAR_CHOSEN_REPLACEMENT_WAGGON_TRACK);
		if (!(RailwayStationBusinessLogic.getInstance().isTrackPresent(replacementTrack))) {
			throw new BpmnError(DtpConstants.DepartTrain.ERROR.ERR_TRACK_NOT_PRESENT);
		}
		getProcessData(delegateTask.getExecution()).getDepartingOrder().setReplacementTrack(replacementTrack);
	}
}