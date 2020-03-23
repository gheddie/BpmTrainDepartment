package de.gravitex.bpm.traindepartment.runner.taskmapping;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;

public class PromptRepairReplacementTaskMapper implements TaskMapper {

	@Override
	public String getTaskName() {
		return DepartTrainProcessConstants.TASK_PROMPT_REPAIR_WAGGON_REPLACEMENT;
	}

	@Override
	public String getRole() {
		return DepartTrainProcessConstants.ROLE_DISPONENT;
	}

	@Override
	public String getListVariableName() {
		return DepartTrainProcessConstants.VAR_WAGGON_REPAIR_TIME_EXCEEDED;
	}
}