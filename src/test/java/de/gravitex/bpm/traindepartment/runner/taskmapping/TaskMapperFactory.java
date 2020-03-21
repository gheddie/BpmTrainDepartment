package de.gravitex.bpm.traindepartment.runner.taskmapping;

import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;
import de.gravitex.bpm.traindepartment.util.HashMapBuilder;

public class TaskMapperFactory {

	private static HashMap<TaskMappingType, TaskMapper> taskMappers = new HashMap<TaskMappingType, TaskMapper>();
	static {
		taskMappers.put(TaskMappingType.EVAULATE_WAGGON, new EvaluateWaggonTaskMapper());
		taskMappers.put(TaskMappingType.PROMPT_WAGGON_REPAIR, new PromptWaggonRepairTaskMapper());
	}
	
	public static String mapWaggonNumberToTaskId(TaskMappingType taskMappingType, ProcessInstance processInstance, String waggonNumber,
			ProcessEngineServices processEngine) {
		TaskMapper taskMapper = taskMappers.get(taskMappingType);
		HashMap<String, String> waggonNumberToTaskIdMapping = getWaggonNumberToTaskIdMapping(
				processEngine.getTaskService().createTaskQuery().taskDefinitionKey(taskMapper.getTaskName())
						.processInstanceId(processInstance.getId()).taskAssignee(taskMapper.getRole()).list(),
				taskMapper.getVariableName(), processEngine);
		return waggonNumberToTaskIdMapping.get(waggonNumber);
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