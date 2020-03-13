package de.gravitex.bpm.traindepartment.delegate;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;

public class CheckWaggonPositionsDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		// exit track must have been chosen...
		String exitTrack = (String) execution.getVariable(DepartTrainProcessConstants.VAR_EXIT_TRACK);
		if (exitTrack == null || !(RailwayStationBusinessLogic.getInstance().isExitTrack(exitTrack))) {
			System.out.println(exitTrack == null ? "no exit track set!!" : "track " + exitTrack + " is no ext track!!");
			throw new BpmnError(DepartTrainProcessConstants.ERR_NO_EXIT_TR);
		}
		// TODO shunting?
		execution.setVariable(DepartTrainProcessConstants.VAR_POSITIONS_OK, true);
	}
}