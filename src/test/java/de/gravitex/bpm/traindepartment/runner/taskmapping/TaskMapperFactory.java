package de.gravitex.bpm.traindepartment.runner.taskmapping;

import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;
import de.gravitex.bpm.traindepartment.util.HashMapBuilder;

public class TaskMapperFactory {

	private static HashMap<TaskMappingType, TaskMapper> taskMappers = new HashMap<TaskMappingType, TaskMapper>();
	static {
		taskMappers.put(TaskMappingType.EVAULATE_WAGGON, new EvaluateWaggonTaskMapper());
		taskMappers.put(TaskMappingType.PROMPT_WAGGON_REPAIR, new PromptWaggonRepairTaskMapper());
		taskMappers.put(TaskMappingType.PROMPT_REPAIR_REPLACEMENT, new PromptRepairReplacementTaskMapper());
	}
	
	public static String mapWaggonNumberToTaskId(TaskMappingType taskMappingType, ProcessInstance processInstance, String waggonNumber,
			ProcessEngineServices processEngine) {
		TaskMapper taskMapper = taskMappers.get(taskMappingType);
		String taskName = taskMapper.getTaskName();
		String role = taskMapper.getRole();
		String processInstanceId = processInstance.getId();
		List<Task> taskList = processEngine.getTaskService().createTaskQuery().taskDefinitionKey(taskName)
				.processInstanceId(processInstanceId).taskAssignee(role).list();
		HashMap<String, String> waggonNumberToTaskIdMapping = getWaggonNumberToTaskIdMapping(
				taskList,
				taskMapper.getListVariableName(), processEngine);
		String taskId = waggonNumberToTaskIdMapping.get(waggonNumber);
		return taskId;
	}

	@SuppressWarnings("unchecked")
	private static HashMap<String, String> getWaggonNumberToTaskIdMapping(List<Task> tasks, String waggonRepairInfoVariable,
			ProcessEngineServices processEngine) {
		String waggonNumber = null;
		HashMapBuilder<String, String> builder = HashMapBuilder.create();
		if (waggonRepairInfoVariable != null) {
			TaskService taskService = processEngine.getTaskService();
			for (Task task : tasks) {
				waggonNumber = ((WaggonProcessInfo) taskService.getVariable(task.getId(),
						waggonRepairInfoVariable)).getWaggonNumber();
				builder.withValuePair(waggonNumber, task.getId());
			}
		}
		HashMap<String, String> result = builder.build();
		return result;
	}
}