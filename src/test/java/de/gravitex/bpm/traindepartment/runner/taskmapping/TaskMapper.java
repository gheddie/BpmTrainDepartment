package de.gravitex.bpm.traindepartment.runner.taskmapping;

public interface TaskMapper {

	// the name of the task(s)
	String getTaskName();

	// the role of the task(s) 
	String getRole();

	// the name of the list variable name
	String getListVariableName();
}