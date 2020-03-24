package de.gravitex.bpm.traindepartment.runner.taskmapping;

import de.gravitex.bpm.traindepartment.logic.DtpConstants;

public class EvaluateWaggonTaskMapper implements TaskMapper {

	@Override
	public String getTaskName() {
		return DtpConstants.NotQualified.TASK.TASK_EVALUATE_WAGGON;
	}

	@Override
	public String getRole() {
		return DtpConstants.NotQualified.ROLE.ROLE_SUPERVISOR;
	}

	@Override
	public String getListVariableName() {
		return DtpConstants.NotQualified.VAR.VAR_ASSUMED_WAGGON;
	}
}