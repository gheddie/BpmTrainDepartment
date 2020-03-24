package de.gravitex.bpm.traindepartment;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.assertj.core.util.Arrays;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.gravitex.bpm.traindepartment.entity.Waggon;
import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.DepartmentProcessData;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogicException;
import de.gravitex.bpm.traindepartment.util.HashMapBuilder;
import de.gravitex.bpm.traindepartment.util.RailUtil;

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
		// ..
	}

	@Test
	@Deployment(resources = { "departTrainProcess.bpmn" })
	public void testStraightAssumement() {

		RailwayStationBusinessLogic.getInstance().reset();

		// prepare test data...
		RailwayStationBusinessLogic.getInstance().withTracks("Track1@true", "TrackExit@true", "TrackReplacement")
				.withWaggons("Track1", "W1@C1#N1", "W2@C1", "W3@C1", "W4@C1", "W5")
				.withRoles(DtpConstants.NotQualified.ROLE.ROLE_DISPONENT, DtpConstants.NotQualified.ROLE.ROLE_SHUNTER,
						DtpConstants.NotQualified.ROLE.ROLE_REPAIR_DUDE,
						DtpConstants.NotQualified.ROLE.ROLE_WAGGON_MASTER);

		RailwayStationBusinessLogic.getInstance().print("In the beginning", false);

		assertTrackOccupancies(true, "Track1:W1,W2,W3,W4,W5", "TrackExit", "TrackReplacement");

		ProcessInstance processInstance = startDepartureProcess(getDefaultPlannedDepartureTime(), "W1", "W2", "W3", "W4");

		// we have 4 facility processes, so 4 assumement tasks..
		assertEquals(4, ensureProcessInstanceCount(DtpConstants.NotQualified.DEFINITION.PROCESS_REPAIR_FACILITY));
		
		ensureTaskCountPresent(null, DtpConstants.Facility.TASK.TASK_ASSUME_REPAIR_TIME,
				DtpConstants.NotQualified.ROLE.ROLE_REPAIR_DUDE, 4);

		// assume a waggon (for all 4 waggons)
		assumeWaggonRepair(processInstance, "W1", 11);
		assumeWaggonRepair(processInstance, "W2", 4);
		assumeWaggonRepair(processInstance, "W3", 3);
		assumeWaggonRepair(processInstance, "W4", 3);

		// 4 evaluations to be done...
		List<Task> evaluationTasks = ensureTaskCountPresent(processInstance,
				DtpConstants.Main.TASK.TASK_EVALUATE_WAGGON, DtpConstants.NotQualified.ROLE.ROLE_SUPERVISOR, 4);

		// 4 facility processes are waiting at 'CATCH_MSG_START_REPAIR'...
		List<ProcessInstance> facilityProcessesInstances = getProcessesInstances(
				DtpConstants.NotQualified.DEFINITION.PROCESS_REPAIR_FACILITY);
		assertEquals(4, facilityProcessesInstances.size());
		assertThat(facilityProcessesInstances.get(0)).isWaitingAt(DtpConstants.NotQualified.GATEWAY.GW_START_OR_ABORT_REPAIR);
		assertThat(facilityProcessesInstances.get(1)).isWaitingAt(DtpConstants.NotQualified.GATEWAY.GW_START_OR_ABORT_REPAIR);
		assertThat(facilityProcessesInstances.get(2)).isWaitingAt(DtpConstants.NotQualified.GATEWAY.GW_START_OR_ABORT_REPAIR);
		assertThat(facilityProcessesInstances.get(3)).isWaitingAt(DtpConstants.NotQualified.GATEWAY.GW_START_OR_ABORT_REPAIR);

		HashMap<String, String> evaluationTaskMapppings = getWaggonNumberToTaskIdMapping(evaluationTasks,
				DtpConstants.NotQualified.VAR.VAR_ASSUMED_WAGGON, processEngine);

		evaluateWaggonRepair("W1", evaluationTaskMapppings, WaggonState.REPAIR_WAGGON);
		evaluateWaggonRepair("W2", evaluationTaskMapppings, WaggonState.REPAIR_WAGGON);
		evaluateWaggonRepair("W3", evaluationTaskMapppings, WaggonState.REPLACE_WAGGON);
		evaluateWaggonRepair("W4", evaluationTaskMapppings, WaggonState.REPLACE_WAGGON);

		// (W3+W4) must have been removed from the data model...
		/*
		assertEquals(Arrays.asList(new String[] { "W1", "W2" }),
				getProcessData(processEngine, processInstance).getUsableWaggonNumbers());
				*/

		assertThat(processInstance).isWaitingAt(DtpConstants.NotQualified.TASK.TASK_PROMPT_WAGGON_REPAIR,
				DtpConstants.NotQualified.TASK.TASK_PROMPT_WAGGON_REPLACEMENT);

		// we have 2 prompt repair task...
		List<Task> promptRepairTasks = ensureTaskCountPresent(processInstance,
				DtpConstants.NotQualified.TASK.TASK_PROMPT_WAGGON_REPAIR, DtpConstants.NotQualified.ROLE.ROLE_DISPONENT, 2);

		// ...and 2 prompt replacement task
		ensureTaskCountPresent(processInstance, DtpConstants.NotQualified.TASK.TASK_PROMPT_WAGGON_REPLACEMENT,
				DtpConstants.NotQualified.ROLE.ROLE_DISPONENT, 2);

		// only 2 facility processes are waiting at 'CATCH_MSG_START_REPAIR' (those of
		// waggons
		// to be replaced have been canceled)
		List<ProcessInstance> facilityProcessList = processEngine.getRuntimeService().createProcessInstanceQuery()
				.processDefinitionKey(DtpConstants.NotQualified.DEFINITION.PROCESS_REPAIR_FACILITY).list();
		assertEquals(2, facilityProcessList.size());
		for (ProcessInstance facilityProcessInstance : facilityProcessList) {
			assertThat(facilityProcessInstance).isWaitingAt(DtpConstants.NotQualified.GATEWAY.GW_START_OR_ABORT_REPAIR);
		}

		// prompt repair (correlates message 'MSG_START_REPAIR') --> before, facility
		// process is waiting at 'CATCH_MSG_START_REPAIR'...

		HashMap<String, String> promptRepairMappings = getWaggonNumberToTaskIdMapping(promptRepairTasks,
				DtpConstants.NotQualified.VAR.VAR_PROMPT_REPAIR_WAGGON, processEngine);

		promptWaggonRepair("W1", promptRepairMappings);
		promptWaggonRepair("W2", promptRepairMappings);

		// we have 2 task of 'TASK_REPAIR_WAGGON' (NOT of this process instance, as we
		// are the 'master')...
		ensureTaskCountPresent(null, DtpConstants.NotQualified.TASK.TASK_REPAIR_WAGGON, DtpConstants.NotQualified.ROLE.ROLE_REPAIR_DUDE,
				2);

		// we prompt replacement for 2 new waggons (W3+W4)..
		List<Task> promptReplacementTasks = ensureTaskCountPresent(processInstance,
				DtpConstants.NotQualified.TASK.TASK_PROMPT_WAGGON_REPLACEMENT, DtpConstants.NotQualified.ROLE.ROLE_DISPONENT, 2);

		HashMap<String, String> promptReplacementMappings = getWaggonNumberToTaskIdMapping(promptReplacementTasks,
				DtpConstants.NotQualified.VAR.VAR_PROMPT_REPLACE_WAGGON, processEngine);

		promptWaggonReplacement("W3", promptReplacementMappings);
		promptWaggonReplacement("W4", promptReplacementMappings);

		// waiting for replacement
		assertThat(processInstance).isWaitingAt(DtpConstants.NotQualified.CATCH.CATCH_MSG_REP_WAGG_ARRIVED);

		// 2 replacements to be be processed...
		processDeliverReplacement(processInstance.getBusinessKey(), "W888", "W999");

		Task chooseReplacementTrackTask = ensureSingleTaskPresent(DtpConstants.NotQualified.TASK.TASK_CHOOSE_REPLACEMENT_TRACK,
				DtpConstants.NotQualified.ROLE.ROLE_DISPONENT, false, null);
		processChooseReplacementTrack(chooseReplacementTrackTask, "TrackReplacement");
		
		/*
		// we must have the replaced waggons as usable waggons...
		List<String> deliveredWaggons = new ArrayList<String>();
		deliveredWaggons.add("W1");
		deliveredWaggons.add("W2");
		deliveredWaggons.add("W888");
		deliveredWaggons.add("W999");
		assertTrue(RailUtil.areListsEqual(deliveredWaggons,
				getProcessData(processEngine, processInstance).getUsableWaggonNumbers()));
				*/

		// replacement waggons were put to 'TrackReplacement'...
		assertTrackOccupancies(true, "Track1:W1,W2,W3,W4,W5", "TrackReplacement:W888,W999");

		// we must have 2 more waggons (now 5+2=7) in the system...
		assertEquals(7, RailwayStationBusinessLogic.getInstance().countWaggons());

		// check replacement track...
		assertTrue(RailwayStationBusinessLogic.getInstance().checkTrackWaggons("TrackReplacement", "W888", "W999"));

		// all prompted --> wait for repairs (or exceedements)...
		assertThat(processInstance).isWaitingAt(DtpConstants.NotQualified.GATEWAY.GW_AWAIT_REPAIR_OUTCOME);

		// assertEquals(2, repairInfos.size());
		assertEquals(2,
				((DepartmentProcessData) processEngine.getRuntimeService().getVariable(processInstance.getId(),
						DtpConstants.NotQualified.VAR.VAR_DEPARTMENT_PROCESS_DATA))
								.getWaggonsByWaggonState(WaggonState.REPAIR_WAGGON).size());

		finishWaggonRepair("W1", processInstance);
		// we have 1 repaired waggon...
		assertEquals(1, getRepairedWaggonCount(processInstance));

		finishWaggonRepair("W2", processInstance);
		// we have 2 repaired waggons...
		assertEquals(2, getRepairedWaggonCount(processInstance));

		// all waggons repaired, so...
		processChooseExitTrack(processInstance, "TrackExit");

		// we have waggon runnabilities to check...
		List<Task> checkRunnabilityTasks = processEngine.getTaskService().createTaskQuery()
				.taskDefinitionKey(DtpConstants.NotQualified.TASK.TASK_CHECK_WAGGON_RUNNABILITY).list();
		assertEquals(4, checkRunnabilityTasks.size());

		// TODO
		HashMap<String, String> checkRunnabilityTaskMapppings = getWaggonNumberToTaskIdMapping(checkRunnabilityTasks,
				DtpConstants.NotQualified.VAR.VAR_PLANNED_WAGGON, processEngine);

		processRunnabilityCheck("W1", checkRunnabilityTaskMapppings, true);
		processRunnabilityCheck("W2", checkRunnabilityTaskMapppings, true);
		processRunnabilityCheck("W888", checkRunnabilityTaskMapppings, true);
		processRunnabilityCheck("W999", checkRunnabilityTaskMapppings, true);

		assertThat(processInstance).isWaitingAt(DtpConstants.NotQualified.TASK.TASK_CONFIRM_ROLLOUT);

		RailwayStationBusinessLogic.getInstance().print("Before rollout", false);

		// confirm roll out
		processRollout(processInstance, true);

		RailwayStationBusinessLogic.getInstance().print("After rollout", false);

		// 4 waggons are gone...
		// TODO really only 1 waggon left? Where are replaced waggons now?
		assertEquals(1, RailwayStationBusinessLogic.getInstance().countWaggons());

		// ALL processes must be gone in the end...
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

		assertEquals(2, ensureProcessInstanceCount(DtpConstants.NotQualified.DEFINITION.PROCESS_REPAIR_FACILITY));
	}

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
		assertEquals(3, ensureProcessInstanceCount(DtpConstants.NotQualified.DEFINITION.PROCESS_REPAIR_FACILITY));

		// master process is waiting at message catch...
		assertThat(processInstance).isWaitingAt(DtpConstants.NotQualified.CATCH.CATCH_MSG_WG_ASSUMED);

		// 3 tasks 'TASK_ASSUME_REPAIR_TIME'...
		List<Task> repairTasks = processEngine.getTaskService().createTaskQuery()
				.taskDefinitionKey(DtpConstants.Facility.TASK.TASK_ASSUME_REPAIR_TIME).list();
		assertEquals(3, repairTasks.size());

		// assume waggon 1 of 3 --> not done (back to message catch)
		assertEquals(1, processEngine.getRuntimeService().createEventSubscriptionQuery()
				.eventName(DtpConstants.NotQualified.MESSAGE.MSG_REPAIR_ASSUMED).list().size());
		assumeWaggonRepair(processInstance, "W1", 2);
		assertThat(processInstance).isWaitingAt(DtpConstants.NotQualified.CATCH.CATCH_MSG_WG_ASSUMED);

		// assume waggon 2 of 3 --> not done (back to message catch)
		assertEquals(1, processEngine.getRuntimeService().createEventSubscriptionQuery()
				.eventName(DtpConstants.NotQualified.MESSAGE.MSG_REPAIR_ASSUMED).list().size());
		assumeWaggonRepair(processInstance, "W2", 2);
		assertThat(processInstance).isWaitingAt(DtpConstants.NotQualified.CATCH.CATCH_MSG_WG_ASSUMED);

		// assume waggon 3 of 3 --> done!!!
		assertEquals(1, processEngine.getRuntimeService().createEventSubscriptionQuery()
				.eventName(DtpConstants.NotQualified.MESSAGE.MSG_REPAIR_ASSUMED).list().size());
		assumeWaggonRepair(processInstance, "W3", 2);

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
		assertThat(instanceB).isWaitingAt(DtpConstants.NotQualified.SIGNAL.SIG_RO_CANC);

		// process A
		assertThat(instanceA).isWaitingAt(DtpConstants.NotQualified.CATCH.CATCH_MSG_WG_ASSUMED);

		// process repair assume for instance A
		List<Task> assumeListA = processEngine.getTaskService().createTaskQuery()
				.taskDefinitionKey(DtpConstants.Facility.TASK.TASK_ASSUME_REPAIR_TIME).list();
		assertEquals(1, assumeListA.size());

		assumeWaggonRepair(instanceA, "W1", 12);

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
	
	private void finishWaggonRepair(String waggonNumber, ProcessInstance parentInstance) {
		Task processRepairTask = getRepairFacilityProcessTask(waggonNumber, DtpConstants.NotQualified.TASK.TASK_REPAIR_WAGGON,
				parentInstance);
		processEngine.getTaskService().complete(processRepairTask.getId());
	}

	@SuppressWarnings("unchecked")
	private void assumeWaggonRepair(ProcessInstance processInstance, String waggonNumber, int hours) {
		Task assumeRepairTimeTask = getRepairFacilityProcessTask(waggonNumber,
				DtpConstants.Facility.TASK.TASK_ASSUME_REPAIR_TIME, processInstance);
		processEngine.getTaskService().complete(assumeRepairTimeTask.getId(),
				HashMapBuilder.create().withValuePair(DtpConstants.NotQualified.VAR.VAR_ASSUMED_TIME, hours).build());

	}

	@SuppressWarnings("unchecked")
	private void processChooseExitTrack(ProcessInstance processInstance, String trackNumber) {
		processEngine.getTaskService().complete(
				processEngine.getTaskService().createTaskQuery().processInstanceBusinessKey(processInstance.getBusinessKey())
						.taskDefinitionKey(DtpConstants.NotQualified.TASK.TASK_CHOOSE_EXIT_TRACK).list().get(0).getId(),
				HashMapBuilder.create().withValuePair(DtpConstants.NotQualified.VAR.VAR_EXIT_TRACK, trackNumber).build());
	}

	@SuppressWarnings("unchecked")
	private void processRollout(ProcessInstance processInstance, boolean doRollOut) {
		processEngine.getTaskService().complete(
				ensureSingleTaskPresent(DtpConstants.NotQualified.TASK.TASK_CONFIRM_ROLLOUT,
						DtpConstants.NotQualified.ROLE.ROLE_DISPONENT, false, null).getId(),
				HashMapBuilder.create().withValuePair(DtpConstants.NotQualified.VAR.VAR_ROLLOUT_CONFIRMED, doRollOut).build());
	}

	private void promptWaggonReplacement(String waggonNumber, HashMap<String, String> promptReplacementMappings) {
		processEngine.getTaskService().complete(promptReplacementMappings.get(waggonNumber));
	}

	private void promptWaggonRepair(String waggonNumber, HashMap<String, String> promptRepairMappings) {
		processEngine.getTaskService().complete(promptRepairMappings.get(waggonNumber));
	}

	@SuppressWarnings("unchecked")
	private void processDeliverReplacement(String businessKey, String... waggonNumbers) {
		processEngine.getRuntimeService().correlateMessage(DtpConstants.NotQualified.MESSAGE.MSG_REPL_WAGG_ARRIVED, businessKey,
				HashMapBuilder.create().withValuePair(DtpConstants.NotQualified.VAR.VAR_DELIVERED_REPLACMENT_WAGGONS, waggonNumbers)
						.build());
	}

	@SuppressWarnings("unchecked")
	private void processChooseReplacementTrack(Task task, String replacementTrack) {
		processEngine.getTaskService().complete(task.getId(), HashMapBuilder.create()
				.withValuePair(DtpConstants.NotQualified.VAR.VAR_REPLACE_WAGGON_TARGET_TRACK, replacementTrack).build());
	}

	@SuppressWarnings("unchecked")
	private void processRunnabilityCheck(String waggonNumber, HashMap<String, String> waggonNumberToTaskIdmapping,
			boolean runnable) {
		processEngine.getTaskService().complete(waggonNumberToTaskIdmapping.get(waggonNumber),
				HashMapBuilder.create().withValuePair(DtpConstants.NotQualified.VAR.VAR_SINGLE_WAGGON_RUNNABLE, runnable).build());
	}

	@SuppressWarnings("unchecked")
	private void evaluateWaggonRepair(String waggonNumber, HashMap<String, String> waggonNumberToTaskIdmapping,
			WaggonState waggonState) {
		TaskService taskService = processEngine.getTaskService();
		String taskId = waggonNumberToTaskIdmapping.get(waggonNumber);
		taskService.complete(taskId, HashMapBuilder.create()
				.withValuePair(DtpConstants.NotQualified.VAR.VAR_WAGGON_EVALUATION_RESULT, waggonState).build());
	}

	@JsonIgnore
	private int getRepairedWaggonCount(ProcessInstance processInstance) {
		DepartmentProcessData departmentProcessData = (DepartmentProcessData) processEngine.getRuntimeService()
				.getVariable(processInstance.getId(), DtpConstants.NotQualified.VAR.VAR_DEPARTMENT_PROCESS_DATA);
		return departmentProcessData.getRepairedWaggonCount();
	}

	private LocalDateTime getDefaultPlannedDepartureTime() {
		return LocalDateTime.now().plusHours(24);
	}

	@SuppressWarnings("unchecked")
	private ProcessInstance startDepartureProcess(LocalDateTime plannedDepartureTime, String... waggonNumbers) {
		List<String> extractedWaggonNumbers = Waggon.getWaggonNumbers(waggonNumbers);
		String generatedBusinessKey = RailwayStationBusinessLogic.getInstance()
				.generateBusinessKey(DtpConstants.NotQualified.DEFINITION.PROCESS_DEPART_TRAIN, HashMapBuilder.create().build(), null);
		DepartmentProcessData departmentProcessData = DepartmentProcessData.fromWaggonNumbers(extractedWaggonNumbers);
		ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByMessage(
				DtpConstants.Main.MESSAGE.MSG_DEPARTURE_PLANNED, generatedBusinessKey,
				HashMapBuilder.create()
						.withValuePair(DtpConstants.NotQualified.VAR.VAR_DEPARTMENT_PROCESS_DATA, departmentProcessData)
						.withValuePair(DtpConstants.NotQualified.VAR.VAR_PLANNED_DEPARTMENT_DATE, plannedDepartureTime).build());
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
				DtpConstants.NotQualified.DEFINITION.PROCESS_REPAIR_FACILITY, waggonNumber, parentInstance);
		return instance;
	}

	private List<ProcessInstance> getProcessInstances() {
		return processEngine.getRuntimeService().createProcessInstanceQuery().list();
	}
}