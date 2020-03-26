package de.gravitex.bpm.traindepartment.runner.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.h2.util.HashBase;

import de.gravitex.bpm.traindepartment.entity.Waggon;
import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import de.gravitex.bpm.traindepartment.logic.DepartmentProcessData;
import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;
import de.gravitex.bpm.traindepartment.runner.taskmapping.TaskMapperFactory;
import de.gravitex.bpm.traindepartment.runner.taskmapping.TaskMappingType;
import de.gravitex.bpm.traindepartment.util.HashMapBuilder;
import lombok.Data;

@Data
public class DepartmentProcessRunner extends ProcessRunner {

	public static final Logger logger = Logger.getLogger(DepartmentProcessRunner.class);

	private String[] waggonNumbers;

	public DepartmentProcessRunner(ProcessEngineServices aProcessEngine) {
		super(aProcessEngine);
	}

	@SuppressWarnings("unchecked")
	public ProcessInstance startDepartureProcess(LocalDateTime plannedDepartureTime, String... waggonNumbers) {
		List<String> extractedWaggonNumbers = Waggon.getWaggonNumbers(waggonNumbers);
		String generatedBusinessKey = RailwayStationBusinessLogic.getInstance().generateBusinessKey(
				DtpConstants.DepartTrain.DEFINITION.PROCESS_DEPART_TRAIN, HashMapBuilder.create().build(), null);
		DepartmentProcessData departmentProcessData = DepartmentProcessData.fromWaggonNumbers(extractedWaggonNumbers);
		ProcessInstance instance = getProcessEngine().getRuntimeService().startProcessInstanceByMessage(
				DtpConstants.DepartTrain.MESSAGE.MSG_DEPARTURE_PLANNED, generatedBusinessKey,
				HashMapBuilder.create()
						.withValuePair(DtpConstants.DepartTrain.VAR.VAR_DEPARTMENT_PROCESS_DATA, departmentProcessData)
						.withValuePair(DtpConstants.DepartTrain.VAR.VAR_PLANNED_DEPARTMENT_DATE, plannedDepartureTime)
						.build());
		logger.info("started departure process...");
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
	public void assumeWaggonRepairs(ProcessInstance processInstance, int hours, String... waggonNumbers) {
		for (String waggonNumber : waggonNumbers) {
			Task assumeRepairTimeTask = getRepairFacilityProcessTask(waggonNumber,
					DtpConstants.Facility.TASK.TASK_ASSUME_REPAIR_TIME, processInstance);
			getProcessEngine().getTaskService().complete(assumeRepairTimeTask.getId(), HashMapBuilder.create()
					.withValuePair(DtpConstants.NotQualified.VAR.VAR_ASSUMED_TIME, hours).build());
		}
	}

	@SuppressWarnings("unchecked")
	public void evaluateWaggonRepairs(ProcessInstance processInstance, WaggonState waggonState,
			String... waggonNumbers) {
		TaskService taskService = getProcessEngine().getTaskService();
		for (String waggonNumber : waggonNumbers) {
			taskService.complete(
					TaskMapperFactory.mapWaggonNumberToTaskId(TaskMappingType.EVAULATE_WAGGON, processInstance,
							waggonNumber, getProcessEngine()),
					HashMapBuilder.create()
							.withValuePair(DtpConstants.NotQualified.VAR.VAR_WAGGON_EVALUATION_RESULT, waggonState)
							.build());
		}
	}

	public void promptWaggonRepairs(ProcessInstance processInstance, String... waggonNumbers) {
		TaskService taskService = getProcessEngine().getTaskService();
		for (String waggonNumber : waggonNumbers) {
			taskService.complete(TaskMapperFactory.mapWaggonNumberToTaskId(TaskMappingType.PROMPT_WAGGON_REPAIR,
					processInstance, waggonNumber, getProcessEngine()));
		}
	}

	public void promptWaggonReplacements(ProcessInstance processInstance, String... waggonNumbers) {
		TaskService taskService = getProcessEngine().getTaskService();
		for (String waggonNumber : waggonNumbers) {
			taskService.complete(
					TaskMapperFactory.mapWaggonNumberToTaskId(TaskMappingType.PROMPT_WAGGON_EVALUATION_REPLACEMENT,
							processInstance, waggonNumber, getProcessEngine()));
		}
	}

	public void finishWaggonRepair(ProcessInstance processInstance, String waggonNumber) {
		logger.info("finishing waggon repair for waggon: " + waggonNumber);
		Task repairWaggonTask = getRepairFacilityProcessTask(waggonNumber,
				DtpConstants.NotQualified.TASK.TASK_REPAIR_WAGGON, processInstance);
		getProcessEngine().getTaskService().complete(repairWaggonTask.getId());
	}

	public void timeoutWaggonRepairs(ProcessInstance processInstance, String... waggonNumbers) {
		for (String waggonNumber : waggonNumbers) {
			logger.info("timing out waggon repair for waggon: " + waggonNumber);
			ProcessInstance repairFacilityProcess = resolveRepairFacilityProcessesForWaggonNumber(processInstance,
					waggonNumber).get(0);
			List<Job> jobs = getProcessEngine().getManagementService().createJobQuery()
					.processInstanceId(repairFacilityProcess.getId()).active().timers().list();
			assertEquals(1, jobs.size());
			Job job = jobs.get(0);
			List<ProcessInstance> executionList = getProcessEngine().getRuntimeService().createProcessInstanceQuery()
					.processInstanceId(job.getProcessInstanceId()).list();
			assertEquals(1, executionList.size());
			logger.info("firing timer for waggon " + waggonNumber + " execution with business key: "
					+ repairFacilityProcess.getBusinessKey());
			getProcessEngine().getManagementService().executeJob(job.getId());
		}
	}

	public void promptRepairWaggonReplacements(ProcessInstance processInstance, String... waggonNumbers) {
		for (String waggonNumber : waggonNumbers) {
			getProcessEngine().getTaskService().complete(
					TaskMapperFactory.mapWaggonNumberToTaskId(TaskMappingType.PROMPT_WAGGON_REPAIR_REPLACEMENT,
							processInstance, waggonNumber, getProcessEngine()));
		}
	}

	@SuppressWarnings("unchecked")
	public void deliverRepairReplacementWaggons(ProcessInstance processInstance, WaggonProcessInfo... replacements) {
		getProcessEngine().getRuntimeService().correlateMessage(DtpConstants.NotQualified.MESSAGE.MSG_REP_REPLACE_ARR,
				processInstance.getBusinessKey(), HashMapBuilder.create().withValuePair(
						DtpConstants.DepartTrain.VAR.VAR_DELIVERED_REP_TIMEOUT_REPLACMENT_WAGGONS, replacements).build());
	}

	@SuppressWarnings("unchecked")
	public void deliverEvaluationReplacementWaggons(ProcessInstance processInstance,
			WaggonProcessInfo... replacements) {
		getProcessEngine().getRuntimeService().correlateMessage(DtpConstants.DepartTrain.MESSAGE.MSG_REP_WAGG_ARRIVED,
				processInstance.getBusinessKey(),
				HashMapBuilder.create()
						.withValuePair(DtpConstants.DepartTrain.VAR.VAR_DELIVERED_EVALUATION_REPLACMENT_WAGGONS,
								replacements)
						.build());
	}

	public void chooseEvaluationReplacementTrack(ProcessInstance processInstance, String trackNumber) {
		TaskService taskService = getProcessEngine().getTaskService();
		List<Task> taskList = taskService.createTaskQuery()
				.taskDefinitionKey(DtpConstants.DepartTrain.TASK.TASK_CHOOSE_EVALUATION_REPLACEMENT_TRACK)
				.processInstanceId(processInstance.getId()).list();
		assertEquals(1, taskList.size());
		taskService.complete(taskList.get(0).getId(), HashMapBuilder.create()
				.withValuePair(DtpConstants.DepartTrain.VAR.VAR_CHOSEN_REPLACEMENT_WAGGON_TRACK, trackNumber).build());
	}

	private Task getRepairFacilityProcessTask(String waggonNumber, String taskDefinitionKey,
			ProcessInstance processInstance) {
		ProcessInstance instance = resolveRepairFacilityProcessesForWaggonNumber(processInstance, waggonNumber).get(0);
		List<Task> tasksAssumeRepairTime = getProcessEngine().getTaskService().createTaskQuery()
				.taskDefinitionKey(taskDefinitionKey).processInstanceBusinessKey(instance.getBusinessKey()).list();
		assertEquals(1, tasksAssumeRepairTime.size());
		return tasksAssumeRepairTime.get(0);
	}

	public List<ProcessInstance> resolveRepairFacilityProcessesForWaggonNumber(ProcessInstance parentInstance,
			String... waggonNumbers) {
		List<ProcessInstance> instances = new ArrayList<ProcessInstance>();
		for (String waggonNumber : waggonNumbers) {
			ProcessInstance instance = RailwayStationBusinessLogic.getInstance().resolveProcessInstance(
					getProcessInstances(), DtpConstants.Facility.DEFINITION.PROCESS_REPAIR_FACILITY, waggonNumber,
					parentInstance);
			assertNotNull(instance);
			// backwards check
			assertEquals(waggonNumber,
					RailwayStationBusinessLogic.getInstance().getAdditionalValueForProcessInstance(instance));
			instances.add(instance);
		}
		return instances;
	}

	@SuppressWarnings("unchecked")
	public List<WaggonProcessInfo> getWaggonsToBePromptedOnRepairTimeout(ProcessInstance processInstance) {
		return (List<WaggonProcessInfo>) getProcessEngine().getRuntimeService().getVariable(processInstance.getId(),
				DtpConstants.NotQualified.VAR.VAR_WAGGONS_REPAIR_TIME_EXCEEDED_LIST);
	}
}