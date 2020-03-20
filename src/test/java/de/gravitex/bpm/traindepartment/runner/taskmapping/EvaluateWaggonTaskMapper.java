package de.gravitex.bpm.traindepartment.runner.taskmapping;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;

public class EvaluateWaggonTaskMapper implements TaskMapper {

	@Override
	public String getTaskName() {
		return DepartTrainProcessConstants.TASK_EVALUATE_WAGGON;
	}

	@Override
	public String getRole() {
		return DepartTrainProcessConstants.ROLE_SUPERVISOR;
	}

	@Override
	public String getVariableName() {
		return DepartTrainProcessConstants.VAR_ASSUMED_WAGGON;
	}
}