package de.gravitex.bpm.traindepartment.runner.base;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;
import lombok.Data;

@Data
public abstract class ProcessRunner {

	private ProcessEngineServices processEngine;

	public ProcessRunner(ProcessEngineServices aProcessEngine) {
		super();
		this.processEngine = aProcessEngine;
	}

	public ProcessRunner clear() {
		RailwayStationBusinessLogic.getInstance().reset();
		return this;
	}
	
	protected List<ProcessInstance> getProcessInstances() {
		return processEngine.getRuntimeService().createProcessInstanceQuery().list();
	}
}