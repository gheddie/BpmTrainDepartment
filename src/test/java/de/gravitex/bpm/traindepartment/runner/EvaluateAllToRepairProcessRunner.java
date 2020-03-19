package de.gravitex.bpm.traindepartment.runner;

import org.camunda.bpm.engine.ProcessEngineServices;

import de.gravitex.bpm.traindepartment.runner.base.DepartmentProcessRunner;

public class EvaluateAllToRepairProcessRunner extends DepartmentProcessRunner {

	public EvaluateAllToRepairProcessRunner(ProcessEngineServices aProcessEngine) {
		super(aProcessEngine);
	}
}