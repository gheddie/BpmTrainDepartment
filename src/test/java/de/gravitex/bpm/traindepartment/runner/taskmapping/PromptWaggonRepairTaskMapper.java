package de.gravitex.bpm.traindepartment.runner.taskmapping;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;

public class PromptWaggonRepairTaskMapper implements TaskMapper {

	@Override
	public String getTaskName() {
		return DepartTrainProcessConstants.TASK_PROMPT_WAGGON_REPAIR;
	}

	@Override
	public String getRole() {
		return DepartTrainProcessConstants.ROLE_DISPONENT;
	}

	@Override
	public String getVariableName() {
		return DepartTrainProcessConstants.VAR_PROMPT_REPAIR_WAGGON;
	}
}