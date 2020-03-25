package de.gravitex.bpm.traindepartment.runner.taskmapping;

public enum TaskMappingType {
	
	// waggon evaluation
	EVAULATE_WAGGON,
	
	// waggon repair	
	PROMPT_WAGGON_REPAIR,

	// waggon replacemnt after repair timeout
	PROMPT_WAGGON_REPAIR_REPLACEMENT,
	
	// waggon replacemnt after evaluation	
	PROMPT_WAGGON_EVALUATION_REPLACEMENT
}