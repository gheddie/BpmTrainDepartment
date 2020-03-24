package de.gravitex.bpm.traindepartment.runner.taskmapping;

import de.gravitex.bpm.traindepartment.logic.DtpConstants;

public class PromptWaggonRepairTaskMapper implements TaskMapper {

	@Override
	public String getTaskName() {
		return DtpConstants.NotQualified.TASK.TASK_PROMPT_WAGGON_REPAIR;
	}

	@Override
	public String getRole() {
		return DtpConstants.NotQualified.ROLE.ROLE_DISPONENT;
	}

	@Override
	public String getListVariableName() {
		return DtpConstants.NotQualified.VAR.VAR_PROMPT_REPAIR_WAGGON;
	}
}