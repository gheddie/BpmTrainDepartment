package de.gravitex.bpm.traindepartment;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.gravitex.bpm.traindepartment.entity.Waggon;
import de.gravitex.bpm.traindepartment.enumeration.RepairEvaluationResult;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogicException;
import de.gravitex.bpm.traindepartment.logic.WaggonList;
import de.gravitex.bpm.traindepartment.logic.WaggonRepairInfo;
import de.gravitex.bpm.traindepartment.util.HashMapBuilder;

public class DepartTrainTestCase extends BpmTestCase {

	@Rule
	public ProcessEngineRule processEngine = new ProcessEngineRule();

	/**
	 * awaits a {@link RailwayStationBusinessLogicException} on creating an invalid
	 * {@link RailwayStationBusinessConfig}.
	 */
	@Test(expected = RailwayStationBusinessLogicException.class)
	public void testInvalidRailwayStationBusinessLogic() {
		RailwayStationBusinessLogic.getInstance().withTracks("Track1", "Track2").withWaggons("Track3", "W1", "W2");
	}

	@Test
	@Deployment(resources = { "departTrainProcess.bpmn" })
	public void testDeployment() {
		// ...
	}

	@Test
	@Deployment(resources = { "departTrainProcess.bpmn" })
	public void testStraightAssumement() {

		RailwayStationBusinessLogic.getInstance().reset();

		// prepare test data
		RailwayStationBusinessLogic.getInstance().withTracks("Track1@true", "TrackExit@true", "TrackReplacement")
				.withWaggons("Track1", "W1@C1#N1", "W2@C1", "W3@C1", "W4@C1", "W5")
				.withRoles(DepartTrainProcessConstants.ROLE_DISPONENT, DepartTrainProcessConstants.ROLE_SHUNTER,
						DepartTrainProcessConstants.ROLE_REPAIR_DUDE, DepartTrainProcessConstants.ROLE_WAGGON_MASTER);

		RailwayStationBusinessLogic.getInstance().print("In the beginning", false);

		ProcessInstance processInstance = startDepartureProcess(getDefaultPlannedDepartureTime(), "W1", "W2", "W3", "W4");

		// we have 4 facility processes, so 4 assumement tasks..
		assertEquals(4, ensureProcessInstanceCount(DepartTrainProcessConstants.PROCESS_REPAIR_FACILITY));
		ensureTaskCountPresent(DepartTrainProcessConstants.TASK_ASSUME_REPAIR_TIME, null,
				DepartTrainProcessConstants.ROLE_REPAIR_DUDE, 4);

		// assume a waggon (for all 4 waggons)
		processWaggonRepairAssumement("W1", 11, processInstance);
		processWaggonRepairAssumement("W2", 4, processInstance);
		processWaggonRepairAssumement("W3", 3, processInstance);
		processWaggonRepairAssumement("W4", 3, processInstance);

		// 4 evaluations to be done...
		List<Task> evaluationTasks = ensureTaskCountPresent(DepartTrainProcessConstants.TASK_EVALUATE_WAGGON,
				processInstance.getId(), DepartTrainProcessConstants.ROLE_SUPERVISOR, 4);

		// 4 facility processes are waiting at 'CATCH_MSG_START_REPAIR'...
		List<ProcessInstance> facilityProcessesInstances = getProcessesInstances(
				DepartTrainProcessConstants.PROCESS_REPAIR_FACILITY);
		assertEquals(4, facilityProcessesInstances.size());
		assertThat(facilityProcessesInstances.get(0)).isWaitingAt(DepartTrainProcessConstants.GW_START_OR_ABORT_REPAIR);
		assertThat(facilityProcessesInstances.get(1)).isWaitingAt(DepartTrainProcessConstants.GW_START_OR_ABORT_REPAIR);
		assertThat(facilityProcessesInstances.get(2)).isWaitingAt(DepartTrainProcessConstants.GW_START_OR_ABORT_REPAIR);
		assertThat(facilityProcessesInstances.get(3)).isWaitingAt(DepartTrainProcessConstants.GW_START_OR_ABORT_REPAIR);

		HashMap<String, String> evaluationTaskMapppings = getWaggonNumberToTaskIdMapping(evaluationTasks,
				DepartTrainProcessConstants.VAR_ASSUMED_WAGGON, processEngine);

		processWaggonEvaluation("W1", evaluationTaskMapppings, RepairEvaluationResult.REPAIR_WAGGON);
		processWaggonEvaluation("W2", evaluationTaskMapppings, RepairEvaluationResult.REPAIR_WAGGON);
		processWaggonEvaluation("W3", evaluationTaskMapppings, RepairEvaluationResult.REPLACE_WAGGON);
		processWaggonEvaluation("W4", evaluationTaskMapppings, RepairEvaluationResult.REPLACE_WAGGON);

		assertThat(processInstance).isWaitingAt(DepartTrainProcessConstants.TASK_PROMPT_WAGGON_REPAIR,
				DepartTrainProcessConstants.TASK_PROMPT_WAGGON_REPLACEMENT);

		// we have 2 prompt repair task...
		List<Task> promptRepairTasks = ensureTaskCountPresent(DepartTrainProcessConstants.TASK_PROMPT_WAGGON_REPAIR,
				processInstance.getId(), DepartTrainProcessConstants.ROLE_DISPONENT, 2);

		// ...and 2 prompt replacement task
		ensureTaskCountPresent(DepartTrainProcessConstants.TASK_PROMPT_WAGGON_REPLACEMENT, processInstance.getId(),
				DepartTrainProcessConstants.ROLE_DISPONENT, 2);

		// only 2 facility processes are waiting at 'CATCH_MSG_START_REPAIR' (those of
		// waggons
		// to be replaced have been canceled)
		List<ProcessInstance> facilityProcessList = processEngine.getRuntimeService().createProcessInstanceQuery()
				.processDefinitionKey(DepartTrainProcessConstants.PROCESS_REPAIR_FACILITY).list();
		assertEquals(2, facilityProcessList.size());
		for (ProcessInstance facilityProcessInstance : facilityProcessList) {
			assertThat(facilityProcessInstance).isWaitingAt(DepartTrainProcessConstants.GW_START_OR_ABORT_REPAIR);
		}

		// prompt repair (correlates message 'MSG_START_REPAIR') --> before, facility
		// process is waiting at 'CATCH_MSG_START_REPAIR'...
		processPromptRepair(promptRepairTasks.get(0));
		processPromptRepair(promptRepairTasks.get(1));

		// we have 2 task of 'TASK_REPAIR_WAGGON' (NOT of this process instance, as we
		// are the 'master')...
		ensureTaskCountPresent(DepartTrainProcessConstants.TASK_REPAIR_WAGGON, null, DepartTrainProcessConstants.ROLE_REPAIR_DUDE,
				2);

		// we prompt 2 new waggons..
		List<Task> promptReplacementTasks = ensureTaskCountPresent(DepartTrainProcessConstants.TASK_PROMPT_WAGGON_REPLACEMENT,
				processInstance.getId(), DepartTrainProcessConstants.ROLE_DISPONENT, 2);
		processPromptReplacement(promptReplacementTasks.get(0));
		processPromptReplacement(promptReplacementTasks.get(1));

		// waiting for replacement
		assertThat(processInstance).isWaitingAt(DepartTrainProcessConstants.CATCH_MSG_REP_WAGG_ARRIVED);

		// 2 replacements to be be processed...
		processDeliverReplacement(processInstance.getBusinessKey(), "W888", "W999");

		Task chooseReplacementTrackTask = ensureSingleTaskPresent(DepartTrainProcessConstants.TASK_CHOOSE_REPLACEMENT_TRACK,
				DepartTrainProcessConstants.ROLE_DISPONENT, false, null);
		processChooseReplacementTrack(chooseReplacementTrackTask, "TrackReplacement");

		// we must have 2 more waggons (now 5+2=7) in the system...
		assertEquals(7, RailwayStationBusinessLogic.getInstance().countWaggons());

		// check replacement track...
		assertTrue(RailwayStationBusinessLogic.getInstance().checkTrackWaggons("TrackReplacement", "W888", "W999"));

		// all prompted --> wait for repairs...
		assertThat(processInstance).isWaitingAt(DepartTrainProcessConstants.CATCH_MSG_WAGGON_REPAIRED);

		// we have 2 waggons to repair (W1, W2)
		/*
		 * List<WaggonRepairInfo> repairInfos = (List<WaggonRepairInfo>)
		 * processEngine.getRuntimeService() .getVariable(processInstance.getId(),
		 * DepartTrainProcessConstants.VAR_PROMPT_REPAIR_WAGGONS_LIST);
		 */

		// assertEquals(2, repairInfos.size());
		assertEquals(2,
				((WaggonList) processEngine.getRuntimeService().getVariable(processInstance.getId(),
						DepartTrainProcessConstants.VAR_WAGGON_LIST))
								.getWaggonsByEvaluationResult(RepairEvaluationResult.REPAIR_WAGGON).size());

		processWaggonRepair("W1", processInstance);
		// we have 1 repaired waggon...
		assertEquals(1, getRepairedWaggonCount(processInstance));

		processWaggonRepair("W2", processInstance);
		// we have 2 repaired waggons...
		assertEquals(2, getRepairedWaggonCount(processInstance));

		// all waggons repaired, so...
		processExitTrack(processInstance, "TrackExit");

		// we have waggon runnabilities to check...
		List<Task> checkRunnabilityTasks = processEngine.getTaskService().createTaskQuery()
				.taskDefinitionKey(DepartTrainProcessConstants.TASK_CHECK_WAGGON_RUNNABILITY).list();
		assertEquals(4, checkRunnabilityTasks.size());

		// TODO
		HashMap<String, String> checkRunnabilityTaskMapppings = getWaggonNumberToTaskIdMapping(checkRunnabilityTasks,
				"VAR_PLANNED_WAGGON", processEngine);

		processRunnabilityCheck("W1", checkRunnabilityTaskMapppings, true);
		processRunnabilityCheck("W2", checkRunnabilityTaskMapppings, true);
		processRunnabilityCheck("W3", checkRunnabilityTaskMapppings, true);
		processRunnabilityCheck("W4", checkRunnabilityTaskMapppings, true);

		assertThat(processInstance).isWaitingAt(DepartTrainProcessConstants.TASK_CONFIRM_ROLLOUT);

		RailwayStationBusinessLogic.getInstance().print("Before rollout", false);

		// confirm roll out
		processRollout(processInstance, true);

		RailwayStationBusinessLogic.getInstance().print("After rollout", false);

		// 4 waggons ware gone...
		assertEquals(3, RailwayStationBusinessLogic.getInstance().countWaggons());

		// TODO ALL processes must be gone in the end
		assertEquals(0, processEngine.getRuntimeService().createProcessInstanceQuery().list().size());
	}

	@Test
	@Deployment(resources = { "departTrainProcess.bpmn" })
	public void testRepairProcessesForCriticalErrors() {

		RailwayStationBusinessLogic.getInstance().reset();

		// prepare test data
		RailwayStationBusinessLogic.getInstance().withTracks("Track1", "TrackExit").withWaggons("Track1", "W1@C1", "W2@C1",
				"W3@N1", "W4", "W5");

		// start process A
		startDepartureProcess(getDefaultPlannedDepartureTime(), "W1", "W2", "W3", "W4", "W5");

		assertEquals(2, ensureProcessInstanceCount(DepartTrainProcessConstants.PROCESS_REPAIR_FACILITY));
	}

	@SuppressWarnings("unchecked")
	@Test
	@Deployment(resources = { "departTrainProcess.bpmn" })
	public void testProcessMultipleCriticalWaggons() {

		RailwayStationBusinessLogic.getInstance().reset();

		// prepare test data
		RailwayStationBusinessLogic.getInstance().withTracks("Track1@true", "TrackExit@true").withWaggons("Track1", "W1@C1",
				"W2@C1#N1", "W3@C1", "W4", "W5@C1");

		RailwayStationBusinessLogic.getInstance().print(null, true);

		assertEquals(5, RailwayStationBusinessLogic.getInstance().countWaggons());

		// 3 critical, 1 non critical waggons
		ProcessInstance processInstance = startDepartureProcess(getDefaultPlannedDepartureTime(), "W1@C1", "W2@C1", "W3@C1",
				"W4");

		// we must have 3 repair processes...
		assertEquals(3, ensureProcessInstanceCount(DepartTrainProcessConstants.PROCESS_REPAIR_FACILITY));

		// 3 waggons must be marked as to be repaired...
		List<String> waggonsToRepair = (List<String>) processEngine.getRuntimeService().getVariable(processInstance.getId(),
				DepartTrainProcessConstants.VAR_WAGGONS_TO_ASSUME);
		assertEquals(3, waggonsToRepair.size());

		// master process is waiting at message catch...
		assertThat(processInstance).isWaitingAt(DepartTrainProcessConstants.CATCH_MSG_WG_REPAIRED);

		// 3 tasks 'TASK_ASSUME_REPAIR_TIME'...
		List<Task> repairTasks = processEngine.getTaskService().createTaskQuery()
				.taskDefinitionKey(DepartTrainProcessConstants.TASK_ASSUME_REPAIR_TIME).list();
		assertEquals(3, repairTasks.size());

		// assume waggon 1 of 3 --> not done (back to message catch)
		assertEquals(1, processEngine.getRuntimeService().createEventSubscriptionQuery()
				.eventName(DepartTrainProcessConstants.MSG_REPAIR_ASSUMED).list().size());
		processWaggonRepairAssumement("W1", 2, processInstance);
		assertThat(processInstance).isWaitingAt(DepartTrainProcessConstants.CATCH_MSG_WG_REPAIRED);

		// assume waggon 2 of 3 --> not done (back to message catch)
		assertEquals(1, processEngine.getRuntimeService().createEventSubscriptionQuery()
				.eventName(DepartTrainProcessConstants.MSG_REPAIR_ASSUMED).list().size());
		processWaggonRepairAssumement("W2", 2, processInstance);
		assertThat(processInstance).isWaitingAt(DepartTrainProcessConstants.CATCH_MSG_WG_REPAIRED);

		// assume waggon 3 of 3 --> done!!!
		assertEquals(1, processEngine.getRuntimeService().createEventSubscriptionQuery()
				.eventName(DepartTrainProcessConstants.MSG_REPAIR_ASSUMED).list().size());
		processWaggonRepairAssumement("W3", 2, processInstance);

		// ...
		// assertThat(processInstance).isWaitingAt(DepartTrainProcessConstants.TASK_EVALUATE_REPAIR);

		/*
		 * // this is an error --> loop processExitTrack(processInstance, "UNKNOWN");
		 * assertThat(processInstance).isWaitingAt(DepartTrainProcessConstants.
		 * TASK_CHOOSE_EXIT_TRACK);
		 * 
		 * // again processExitTrack(processInstance, "TrackExit");
		 * 
		 * // wait for shunting response
		 * assertThat(processInstance).isWaitingAt(DepartTrainProcessConstants.
		 * CATCH_MSG_SH_DONE);
		 * 
		 * processShunting(processInstance);
		 * 
		 * processRollout(processInstance, true);
		 * 
		 * // process finished assertThat(processInstance).isEnded();
		 */
	}

	@Test
	@Deployment(resources = { "departTrainProcess.bpmn" })
	public void testConcurrentDeparture() {

		RailwayStationBusinessLogic.getInstance().reset();

		// prepare test data
		RailwayStationBusinessLogic.getInstance().withTracks("Track1@true", "TrackExit").withWaggons("Track1", "W1@C1", "W2");

		RailwayStationBusinessLogic.getInstance().print(null, true);

		assertEquals(2, RailwayStationBusinessLogic.getInstance().countWaggons());

		// start process A
		ProcessInstance instanceA = startDepartureProcess(getDefaultPlannedDepartureTime(), "W1", "W2");

		// start process B
		ProcessInstance instanceB = startDepartureProcess(getDefaultPlannedDepartureTime(), "W1", "W2");

		// process B
		assertThat(instanceB).isWaitingAt(DepartTrainProcessConstants.SIG_RO_CANC);

		// process A
		assertThat(instanceA).isWaitingAt(DepartTrainProcessConstants.CATCH_MSG_WG_REPAIRED);

		// process repair assume for instance A
		List<Task> assumeListA = processEngine.getTaskService().createTaskQuery()
				.taskDefinitionKey(DepartTrainProcessConstants.TASK_ASSUME_REPAIR_TIME).list();
		assertEquals(1, assumeListA.size());

		processWaggonRepairAssumement("W1", 12, instanceA);

		// ...
		// assertThat(instanceA).isWaitingAt(DepartTrainProcessConstants.TASK_EVALUATE_REPAIR);

		/*
		 * // finish track choosing for A processExitTrack(instanceA, "Track1");
		 * 
		 * // shunt A... processShunting(instanceA);
		 * 
		 * // finish roll out processRollout(instanceA, false);
		 * assertThat(instanceA).isEnded();
		 * 
		 * // B caught signal and must now check its own waggons... //
		 * assertThat(instanceB).isWaitingAt(DepartTrainProcessConstants.
		 * TASK_CHECK_WAGGONS);
		 * 
		 * // process assume for instance B List<Task> assumeListB =
		 * processEngine.getTaskService().createTaskQuery()
		 * .taskDefinitionKey(DepartTrainProcessConstants.TASK_ASSUME_REPAIR_TIME).list(
		 * ); assertEquals(1, assumeListB.size());
		 * processEngine.getTaskService().complete(assumeListB.get(0).getId());
		 * 
		 * // B waiting for exit track processExitTrack(instanceB, "Track1");
		 * 
		 * processShunting(instanceB);
		 * 
		 * // B waiting for exit track
		 * assertThat(instanceB).isWaitingAt(DepartTrainProcessConstants.
		 * TASK_CONFIRM_ROLLOUT);
		 * 
		 * processRollout(instanceB, true);
		 * 
		 * // B is gone... assertThat(instanceB).isEnded();
		 * 
		 * // waggons must have left the station... assertEquals(0,
		 * RailwayStationBusinessLogic.getInstance().countWaggons());
		 */
	}

	private void processWaggonRepair(String waggonNumber, ProcessInstance parentInstance) {
		Task processRepairTask = getRepairFacilityProcessTask(waggonNumber, DepartTrainProcessConstants.TASK_REPAIR_WAGGON,
				parentInstance);
		processEngine.getTaskService().complete(processRepairTask.getId());
	}

	private void processWaggonRepairAssumement(String waggonNumber, int hours, ProcessInstance parentInstance) {
		Task assumeRepairTimeTask = getRepairFacilityProcessTask(waggonNumber,
				DepartTrainProcessConstants.TASK_ASSUME_REPAIR_TIME, parentInstance);
		processEngine.getTaskService().complete(assumeRepairTimeTask.getId(),
				HashMapBuilder.create().withValuePair(DepartTrainProcessConstants.VAR_ASSUMED_TIME, hours).build());

	}

	private void processExitTrack(ProcessInstance processInstance, String trackNumber) {
		processEngine.getTaskService().complete(
				processEngine.getTaskService().createTaskQuery().processInstanceBusinessKey(processInstance.getBusinessKey())
						.taskDefinitionKey(DepartTrainProcessConstants.TASK_CHOOSE_EXIT_TRACK).list().get(0).getId(),
				HashMapBuilder.create().withValuePair(DepartTrainProcessConstants.VAR_EXIT_TRACK, trackNumber).build());
	}

	private void processShunting(ProcessInstance processInstance) {
		ensureSingleTaskPresent(DepartTrainProcessConstants.TASK_SHUNT_WAGGONS, null, true, null);
	}

	private void processRollout(ProcessInstance processInstance, boolean doRollOut) {
		processEngine.getTaskService().complete(
				ensureSingleTaskPresent(DepartTrainProcessConstants.TASK_CONFIRM_ROLLOUT,
						DepartTrainProcessConstants.ROLE_DISPONENT, false, null).getId(),
				HashMapBuilder.create().withValuePair(DepartTrainProcessConstants.VAR_ROLLOUT_CONFIRMED, doRollOut).build());
	}

	private void processPromptReplacement(Task task) {
		processEngine.getTaskService().complete(task.getId());
	}

	private void processPromptRepair(Task task) {
		processEngine.getTaskService().complete(task.getId());
	}

	private void processDeliverReplacement(String businessKey, String... waggonNumbers) {
		processEngine.getRuntimeService().correlateMessage(DepartTrainProcessConstants.MSG_REPL_WAGG_ARRIVED, businessKey,
				HashMapBuilder.create().withValuePair(DepartTrainProcessConstants.VAR_DELIVERED_REPLACMENT_WAGGONS, waggonNumbers)
						.build());
	}

	private void processChooseReplacementTrack(Task task, String replacementTrack) {
		processEngine.getTaskService().complete(task.getId(), HashMapBuilder.create()
				.withValuePair(DepartTrainProcessConstants.VAR_REPLACE_WAGGON_TARGET_TRACK, replacementTrack).build());
	}

	private void processRunnabilityCheck(String waggonNumber, HashMap<String, String> waggonNumberToTaskIdmapping,
			boolean runnable) {
		processEngine.getTaskService().complete(waggonNumberToTaskIdmapping.get(waggonNumber),
				HashMapBuilder.create().withValuePair(DepartTrainProcessConstants.VAR_SINGLE_WAGGON_RUNNABLE, runnable).build());
	}

	private void processWaggonEvaluation(String waggonNumber, HashMap<String, String> waggonNumberToTaskIdmapping,
			RepairEvaluationResult repairEvaluationResult) {
		TaskService taskService = processEngine.getTaskService();
		String taskId = waggonNumberToTaskIdmapping.get(waggonNumber);
		taskService.complete(taskId, HashMapBuilder.create()
				.withValuePair(DepartTrainProcessConstants.VAR_WAGGON_EVALUATION_RESULT, repairEvaluationResult).build());
	}

	@JsonIgnore
	private int getRepairedWaggonCount(ProcessInstance processInstance) {
		WaggonList waggonList = (WaggonList) processEngine.getRuntimeService().getVariable(processInstance.getId(),
				DepartTrainProcessConstants.VAR_WAGGON_LIST);
		return waggonList.getRepairedWaggonCount();
	}

	private LocalDateTime getDefaultPlannedDepartureTime() {
		return LocalDateTime.now().plusHours(24);
	}

	private ProcessInstance startDepartureProcess(LocalDateTime plannedDepartureTime, String... waggonNumbers) {
		List<String> extractedWaggonNumbers = Waggon.getWaggonNumbers(waggonNumbers);
		String generatedBusinessKey = RailwayStationBusinessLogic.getInstance()
				.generateBusinessKey(DepartTrainProcessConstants.PROCESS_DEPART_TRAIN, HashMapBuilder.create().build(), null);
		WaggonList waggonList = WaggonList.fromWaggonNumbers(extractedWaggonNumbers);
		ProcessInstance instance = processEngine.getRuntimeService()
				.startProcessInstanceByMessage(DepartTrainProcessConstants.MSG_DEPARTURE_PLANNED, generatedBusinessKey,
						HashMapBuilder.create().withValuePair(DepartTrainProcessConstants.VAR_WAGGON_LIST, waggonList)
								.withValuePair(DepartTrainProcessConstants.VAR_PLANNED_DEPARTMENT_DATE, plannedDepartureTime)
								.build());
		return instance;
	}

	private Task getRepairFacilityProcessTask(String waggonNumber, String taskDefinitionKey, ProcessInstance parentInstance) {
		ProcessInstance instance = resolveRepairFacilityProcessForWaggonNumber(waggonNumber, parentInstance);
		List<Task> tasksAssumeRepairTime = processEngine.getTaskService().createTaskQuery().taskDefinitionKey(taskDefinitionKey)
				.processInstanceBusinessKey(instance.getBusinessKey()).list();
		assertEquals(1, tasksAssumeRepairTime.size());
		return tasksAssumeRepairTime.get(0);
	}

	private ProcessInstance resolveRepairFacilityProcessForWaggonNumber(String waggonNumber, ProcessInstance parentInstance) {
		ProcessInstance instance = RailwayStationBusinessLogic.getInstance().resolveProcessInstance(getProcessInstances(),
				DepartTrainProcessConstants.PROCESS_REPAIR_FACILITY, waggonNumber, parentInstance);
		return instance;
	}

	private List<ProcessInstance> getProcessInstances() {
		return processEngine.getRuntimeService().createProcessInstanceQuery().list();
	}
}