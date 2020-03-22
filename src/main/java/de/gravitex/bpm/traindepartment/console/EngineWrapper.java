package de.gravitex.bpm.traindepartment.console;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class EngineWrapper {

	public static final Logger logger = Logger.getLogger(EngineWrapper.class);

	private static final String DEPLOYMENT_NAME = "DEPLOYMENT";
	
	private static final String RESOURCE_NAME = "RESOURCE";

	private static final String MODEL_NAME = "departTrainProcess.bpmn";

	private static final String PROCESS_START_MESSAGE = "MSG_DEPARTURE_PLANNED";

	// ---

	public static void main(String[] args) {

		// new EngineWrapper();

		System.out.println("building process engine...");

		ProcessEngine processEngine = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration()
				.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE)
				.setJdbcUrl("jdbc:h2:mem:my-own-db;DB_CLOSE_DELAY=1000").setJobExecutorActivate(true)
				.setDatabaseSchemaUpdate("create-drop").buildProcessEngine();

		System.out.println("process engine is up...");

		RepositoryService repositoryService = processEngine.getRepositoryService();
		repositoryService.createDeployment().name(DEPLOYMENT_NAME)
				.addModelInstance(RESOURCE_NAME, createProcess(MODEL_NAME)).deploy();

		List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
		System.out.println(deployments.size() + " processes deployed...");
		List<ProcessDefinition> definitionList = repositoryService.createProcessDefinitionQuery().list();
		for (ProcessDefinition processDefinition : definitionList) {
			System.out.println("deployed: " + processDefinition.getKey());
		}
		
		// start a process...
		RuntimeService runtimeService = processEngine.getRuntimeService();
		// runtimeService.startProcessInstanceByMessage(PROCESS_START_MESSAGE);
		
		System.out.println(runtimeService.createProcessInstanceQuery().list().size() + " processes running...");
	}

	private static BpmnModelInstance createProcess(String resourceName) {
		File modelFile = new File(EngineWrapper.class.getClassLoader().getResource(resourceName).getFile());
		return Bpmn.readModelFromFile(modelFile);
	}
}