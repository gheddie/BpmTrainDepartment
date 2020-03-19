package de.gravitex.bpm.traindepartment.runner.base;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

import de.gravitex.bpm.traindepartment.entity.Waggon;
import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.DepartmentProcessData;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;
import de.gravitex.bpm.traindepartment.util.HashMapBuilder;
import lombok.Data;

@Data
public abstract class DepartmentProcessRunner extends ProcessRunner {

	private String[] waggonNumbers;

	public DepartmentProcessRunner(ProcessEngineServices aProcessEngine) {
		super(aProcessEngine);
	}

	@SuppressWarnings("unchecked")
	public ProcessInstance startDepartureProcess(LocalDateTime plannedDepartureTime, String... waggonNumbers) {
		List<String> extractedWaggonNumbers = Waggon.getWaggonNumbers(waggonNumbers);
		String generatedBusinessKey = RailwayStationBusinessLogic.getInstance()
				.generateBusinessKey(DepartTrainProcessConstants.PROCESS_DEPART_TRAIN, HashMapBuilder.create().build(), null);
		DepartmentProcessData departmentProcessData = DepartmentProcessData.fromWaggonNumbers(extractedWaggonNumbers);
		ProcessInstance instance = getProcessEngine().getRuntimeService().startProcessInstanceByMessage(
				DepartTrainProcessConstants.MSG_DEPARTURE_PLANNED, generatedBusinessKey,
				HashMapBuilder.create()
						.withValuePair(DepartTrainProcessConstants.VAR_DEPARTMENT_PROCESS_DATA, departmentProcessData)
						.withValuePair(DepartTrainProcessConstants.VAR_PLANNED_DEPARTMENT_DATE, plannedDepartureTime).build());
		return instance;
	}

	public DepartmentProcessRunner withTracks(String... trackNumbers) {
		RailwayStationBusinessLogic.getInstance().withTracks(trackNumbers);
		return this;
	}

	public DepartmentProcessRunner withWaggons(String trackNumber, String... waggonNumbers) {
		RailwayStationBusinessLogic.getInstance().withWaggons(trackNumber, waggonNumbers);
		return this;
	}

	@SuppressWarnings("unchecked")
	public void assumeWaggonRepair(ProcessInstance processInstance, String waggonNumber, int hours) {
		Task assumeRepairTimeTask = getRepairFacilityProcessTask(waggonNumber,
				DepartTrainProcessConstants.TASK_ASSUME_REPAIR_TIME, processInstance);
		getProcessEngine().getTaskService().complete(assumeRepairTimeTask.getId(),
				HashMapBuilder.create().withValuePair(DepartTrainProcessConstants.VAR_ASSUMED_TIME, hours).build());
	}

	@SuppressWarnings("unchecked")
	public void evaluateWaggonRepair(ProcessInstance processInstance, String waggonNumber, WaggonState waggonState) {
		TaskService taskService = getProcessEngine().getTaskService();
		List<Task> evaluationTasks = getProcessEngine().getTaskService().createTaskQuery()
				.taskDefinitionKey(DepartTrainProcessConstants.TASK_EVALUATE_WAGGON).processInstanceId(processInstance.getId())
				.taskAssignee(DepartTrainProcessConstants.ROLE_SUPERVISOR).list();
		String taskId = getWaggonNumberToTaskIdMapping(evaluationTasks, DepartTrainProcessConstants.VAR_ASSUMED_WAGGON,
				getProcessEngine()).get(waggonNumber);
		taskService.complete(taskId, HashMapBuilder.create()
				.withValuePair(DepartTrainProcessConstants.VAR_WAGGON_EVALUATION_RESULT, waggonState).build());
	}

	public void promptWaggonRepair(ProcessInstance processInstance, String waggonNumber) {
		List<Task> promptRepairTasks = getProcessEngine().getTaskService().createTaskQuery()
				.taskDefinitionKey(DepartTrainProcessConstants.TASK_PROMPT_WAGGON_REPAIR)
				.processInstanceId(processInstance.getId()).taskAssignee(DepartTrainProcessConstants.ROLE_DISPONENT).list();
		getProcessEngine().getTaskService().complete(getWaggonNumberToTaskIdMapping(promptRepairTasks,
				DepartTrainProcessConstants.VAR_PROMPT_REPAIR_WAGGON, getProcessEngine()).get(waggonNumber));
	}

	public void finishWaggonRepair(ProcessInstance processInstance, String waggonNumber) {
		Task repairWaggonTask = getRepairFacilityProcessTask(waggonNumber, DepartTrainProcessConstants.TASK_REPAIR_WAGGON,
				processInstance);
		getProcessEngine().getTaskService().complete(repairWaggonTask.getId());
	}

	public void timeoutWaggonRepair(ProcessInstance processInstance, String waggonNumber) {
		List<Job> jobs = getProcessEngine().getManagementService().createJobQuery()
				.processInstanceId(resolveRepairFacilityProcessForWaggonNumber(waggonNumber, processInstance).getId()).active()
				.timers().list();
		assertEquals(1, jobs.size());
		getProcessEngine().getManagementService().executeJob(jobs.get(0).getId());
	}

	private Task getRepairFacilityProcessTask(String waggonNumber, String taskDefinitionKey, ProcessInstance processInstance) {
		ProcessInstance instance = resolveRepairFacilityProcessForWaggonNumber(waggonNumber, processInstance);
		List<Task> tasksAssumeRepairTime = getProcessEngine().getTaskService().createTaskQuery()
				.taskDefinitionKey(taskDefinitionKey).processInstanceBusinessKey(instance.getBusinessKey()).list();
		assertEquals(1, tasksAssumeRepairTime.size());
		return tasksAssumeRepairTime.get(0);
	}

	private ProcessInstance resolveRepairFacilityProcessForWaggonNumber(String waggonNumber, ProcessInstance parentInstance) {
		ProcessInstance instance = RailwayStationBusinessLogic.getInstance().resolveProcessInstance(getProcessInstances(),
				DepartTrainProcessConstants.PROCESS_REPAIR_FACILITY, waggonNumber, parentInstance);
		return instance;
	}

	@SuppressWarnings("unchecked")
	protected HashMap<String, String> getWaggonNumberToTaskIdMapping(List<Task> tasks, String waggonRepairInfoVariable,
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