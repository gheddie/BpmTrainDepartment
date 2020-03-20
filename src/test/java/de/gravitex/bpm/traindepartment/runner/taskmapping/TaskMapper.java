package de.gravitex.bpm.traindepartment.runner.taskmapping;

import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;
import de.gravitex.bpm.traindepartment.util.HashMapBuilder;

public class TaskMapper {

	public static String map(String taskName, ProcessInstance processInstance, String waggonNumber,
			ProcessEngineServices processEngine) {
		switch (taskName) {
		case DepartTrainProcessConstants.TASK_EVALUATE_WAGGON:
			return evaluate(processInstance, waggonNumber, processEngine);
		case DepartTrainProcessConstants.TASK_PROMPT_WAGGON_REPAIR:
			return prompt(processInstance, waggonNumber, processEngine);
		}
		return null;
	}

	private static String evaluate(ProcessInstance processInstance, String waggonNumber, ProcessEngineServices processEngine) {
		String taskId = getWaggonNumberToTaskIdMapping(processEngine.getTaskService().createTaskQuery()
				.taskDefinitionKey(DepartTrainProcessConstants.TASK_EVALUATE_WAGGON).processInstanceId(processInstance.getId())
				.taskAssignee(DepartTrainProcessConstants.ROLE_SUPERVISOR).list(), DepartTrainProcessConstants.VAR_ASSUMED_WAGGON,
				processEngine).get(waggonNumber);
		return taskId;
	}

	private static String prompt(ProcessInstance processInstance, String waggonNumber, ProcessEngineServices processEngine) {
		String taskId = getWaggonNumberToTaskIdMapping(processEngine.getTaskService().createTaskQuery()
				.taskDefinitionKey(DepartTrainProcessConstants.TASK_PROMPT_WAGGON_REPAIR)
				.processInstanceId(processInstance.getId()).taskAssignee(DepartTrainProcessConstants.ROLE_DISPONENT).list(),
				DepartTrainProcessConstants.VAR_PROMPT_REPAIR_WAGGON, processEngine).get(waggonNumber);
		return taskId;
	}

	@SuppressWarnings("unchecked")
	private static HashMap<String, String> getWaggonNumberToTaskIdMapping(List<Task> tasks, String waggonRepairInfoVariable,
			ProcessEngineServices processEngine) {
		String waggonNumber = null;
		HashMapBuilder<String, String> builder = HashMapBuilder.create();
		if (waggonRepairInfoVariable != null) {
			for (Task task : tasks) {
				waggonNumber = ((WaggonProcessInfo) processEngine.getTaskService().getVariable(task.getId(),
						waggonRepairInfoVariable)).getWaggonNumber();
				builder.withValuePair(waggonNumber, task.getId());
			}
		}
		return builder.build();
	}
}