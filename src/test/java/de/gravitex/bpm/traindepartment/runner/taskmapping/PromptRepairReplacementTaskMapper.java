package de.gravitex.bpm.traindepartment.runner.taskmapping;

import de.gravitex.bpm.traindepartment.logic.DtpConstants;

public class PromptRepairReplacementTaskMapper implements TaskMapper {

	@Override
	public String getTaskName() {
		return DtpConstants.NotQualified.TASK.TASK_PROMPT_REPAIR_WAGGON_REPLACEMENT;
	}

	@Override
	public String getRole() {
		return DtpConstants.NotQualified.ROLE.ROLE_DISPONENT;
	}

	@Override
	public String getListVariableName() {
		return DtpConstants.NotQualified.VAR.VAR_WAGGON_REPAIR_TIME_EXCEEDED;
	}
}