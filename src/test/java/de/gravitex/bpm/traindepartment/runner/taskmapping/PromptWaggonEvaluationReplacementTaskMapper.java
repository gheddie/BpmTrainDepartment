package de.gravitex.bpm.traindepartment.runner.taskmapping;

import de.gravitex.bpm.traindepartment.logic.DtpConstants;

public class PromptWaggonEvaluationReplacementTaskMapper implements TaskMapper {

	@Override
	public String getTaskName() {
		return DtpConstants.DepartTrain.TASK.TASK_PROMPT_WAGGON_REPLACEMENT;
	}

	@Override
	public String getRole() {
		return DtpConstants.NotQualified.ROLE.ROLE_DISPONENT;
	}

	@Override
	public String getListVariableName() {
		return DtpConstants.NotQualified.VAR.VAR_PROMPT_REPLACE_WAGGON;
	}
}