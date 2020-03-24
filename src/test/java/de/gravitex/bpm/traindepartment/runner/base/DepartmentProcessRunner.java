package de.gravitex.bpm.traindepartment.runner.base;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.log4j.Logger;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

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
				DtpConstants.NotQualified.DEFINITION.PROCESS_DEPART_TRAIN, HashMapBuilder.create().build(), null);
		DepartmentProcessData departmentProcessData = DepartmentProcessData.fromWaggonNumbers(extractedWaggonNumbers);
		ProcessInstance instance = getProcessEngine().getRuntimeService().startProcessInstanceByMessage(
				DtpConstants.NotQualified.MESSAGE.MSG_DEPARTURE_PLANNED, generatedBusinessKey,
				HashMapBuilder.create()
						.withValuePair(DtpConstants.NotQualified.VAR.VAR_DEPARTMENT_PROCESS_DATA, departmentProcessData)
						.withValuePair(DtpConstants.NotQualified.VAR.VAR_PLANNED_DEPARTMENT_DATE, plannedDepartureTime)
						.build());
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
					DtpConstants.NotQualified.TASK.TASK_ASSUME_REPAIR_TIME, processInstance);
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

	public void finishWaggonRepair(ProcessInstance processInstance, String waggonNumber) {
		logger.info("finishing waggon repair for waggon: " + waggonNumber);
		Task repairWaggonTask = getRepairFacilityProcessTask(waggonNumber,
				DtpConstants.NotQualified.TASK.TASK_REPAIR_WAGGON, processInstance);
		getProcessEngine().getTaskService().complete(repairWaggonTask.getId());
	}

	public void timeoutWaggonRepair(ProcessInstance processInstance, String waggonNumber) {
		logger.info("timing out waggon repair for waggon: " + waggonNumber);
		ProcessInstance repairFacilityProcess = resolveRepairFacilityProcessForWaggonNumber(waggonNumber,
				processInstance);
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

	public void promptRepairWaggonReplacement(ProcessInstance processInstance, String waggonNumber) {
		getProcessEngine().getTaskService().complete(TaskMapperFactory.mapWaggonNumberToTaskId(
				TaskMappingType.PROMPT_REPAIR_REPLACEMENT, processInstance, waggonNumber, getProcessEngine()));
	}

	public void deliverRepairReplacementWaggon(ProcessInstance processInstance, String WaggonNumber) {
		getProcessEngine().getRuntimeService().correlateMessage(DtpConstants.NotQualified.MESSAGE.MSG_REP_REPLACE_ARR,
				processInstance.getBusinessKey());
	}

	private Task getRepairFacilityProcessTask(String waggonNumber, String taskDefinitionKey,
			ProcessInstance processInstance) {
		ProcessInstance instance = resolveRepairFacilityProcessForWaggonNumber(waggonNumber, processInstance);
		List<Task> tasksAssumeRepairTime = getProcessEngine().getTaskService().createTaskQuery()
				.taskDefinitionKey(taskDefinitionKey).processInstanceBusinessKey(instance.getBusinessKey()).list();
		assertEquals(1, tasksAssumeRepairTime.size());
		return tasksAssumeRepairTime.get(0);
	}

	private ProcessInstance resolveRepairFacilityProcessForWaggonNumber(String waggonNumber,
			ProcessInstance parentInstance) {
		ProcessInstance instance = RailwayStationBusinessLogic.getInstance().resolveProcessInstance(
				getProcessInstances(), DtpConstants.NotQualified.DEFINITION.PROCESS_REPAIR_FACILITY, waggonNumber,
				parentInstance);
		// backwards check
		assertEquals(waggonNumber,
				RailwayStationBusinessLogic.getInstance().getAdditionalValueForProcessInstance(instance));
		return instance;
	}

	@SuppressWarnings("unchecked")
	public List<WaggonProcessInfo> getWaggonsToBePromptedOnRepairTimeout(ProcessInstance processInstance) {
		return (List<WaggonProcessInfo>) getProcessEngine().getRuntimeService().getVariable(processInstance.getId(),
				DtpConstants.NotQualified.VAR.VAR_WAGGONS_REPAIR_TIME_EXCEEDED_LIST);
	}
}