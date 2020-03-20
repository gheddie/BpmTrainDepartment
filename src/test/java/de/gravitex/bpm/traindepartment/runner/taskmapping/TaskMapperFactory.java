package de.gravitex.bpm.traindepartment.runner.taskmapping;

import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;
import de.gravitex.bpm.traindepartment.util.HashMapBuilder;

public class TaskMapperFactory {
	
	private static HashMap<TaskMappingType, TaskMapper> taskMappers = new HashMap<TaskMappingType, TaskMapper>();
	static {
		taskMappers.put(TaskMappingType.EVAULATE_WAGGON, new EvaluateWaggonTaskMapper());
		taskMappers.put(TaskMappingType.PROMPT_WAGGON_REPAIR, new PromptWaggonRepairTaskMapper());
	}

	public static String map(TaskMappingType taskMappingType, ProcessInstance processInstance, String waggonNumber,
			ProcessEngineServices processEngine) {
		switch (taskMappingType) {
		case EVAULATE_WAGGON:
			return mapIntern(processEngine, processInstance, DepartTrainProcessConstants.TASK_EVALUATE_WAGGON,
					DepartTrainProcessConstants.ROLE_SUPERVISOR, DepartTrainProcessConstants.VAR_ASSUMED_WAGGON, waggonNumber);
		case PROMPT_WAGGON_REPAIR:
			return mapIntern(processEngine, processInstance, DepartTrainProcessConstants.TASK_PROMPT_WAGGON_REPAIR,
					DepartTrainProcessConstants.ROLE_DISPONENT, DepartTrainProcessConstants.VAR_PROMPT_REPAIR_WAGGON,
					waggonNumber);
		}
		return null;
	}

	private static String mapIntern(ProcessEngineServices processEngine, ProcessInstance processInstance, String taskName,
			String role, String variableName, String waggonNumber) {
		return getWaggonNumberToTaskIdMapping(processEngine.getTaskService().createTaskQuery()
				.taskDefinitionKey(taskName).processInstanceId(processInstance.getId()).taskAssignee(role).list(), variableName,
				processEngine).get(waggonNumber);
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