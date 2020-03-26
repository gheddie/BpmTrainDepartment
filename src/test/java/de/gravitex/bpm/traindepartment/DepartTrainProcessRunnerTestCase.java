package de.gravitex.bpm.traindepartment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import java.time.LocalDateTime;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;
import de.gravitex.bpm.traindepartment.logic.facade.FacilityFacade;
import de.gravitex.bpm.traindepartment.logic.facade.IFacilityFacade;
import de.gravitex.bpm.traindepartment.runner.base.DepartmentProcessRunner;

public class DepartTrainProcessRunnerTestCase extends BpmTestCase {

	@Rule
	public ProcessEngineRule processEngine = new ProcessEngineRule();

	private static final IFacilityFacade facilityFacade = new FacilityFacade();

	@Test
	@Deployment(resources = { "departTrainProcess.bpmn" })
	public void testProcessRunner() {

		DepartmentProcessRunner processRunner = new DepartmentProcessRunner(processEngine);

		processRunner.withTracks("Track1@true", "TrackExit@true", "TrackReplacement").withWaggons("Track1", "W1@C1#N1",
				"W2@C1", "W3@C1", "W4@C1", "W5");

		ProcessInstance processInstance = processRunner.startDepartureProcess(LocalDateTime.now(),
				new String[] { "W1", "W2" });

		assertWaitStates(processInstance, DtpConstants.NotQualified.CATCH.CATCH_MSG_WG_ASSUMED);

		// assume repairs
		processRunner.assumeWaggonRepairs(processInstance, 12, "W1");
		processRunner.assumeWaggonRepairs(processInstance, 24, "W2");

		ensureTaskCountPresent(processInstance, DtpConstants.DepartTrain.TASK.TASK_EVALUATE_WAGGON,
				DtpConstants.NotQualified.ROLE.ROLE_SUPERVISOR, 2);

		// evaluate repairs
		processRunner.evaluateWaggonRepairs(processInstance, WaggonState.REPAIR_WAGGON, "W1", "W2");

		assertWaitStates(processInstance, DtpConstants.DepartTrain.TASK.TASK_PROMPT_WAGGON_REPAIR);

		// prompt repairs
		processRunner.promptWaggonRepairs(processInstance, "W1", "W2");

		assertWaitStates(processInstance, DtpConstants.DepartTrain.GATEWAY.GW_AWAIT_REPAIR_OUTCOME);

		// finish WaggonRepairs
		processRunner.finishWaggonRepair(processInstance, "W1");
		processRunner.timeoutWaggonRepairs(processInstance, "W2");

		assertWaitStates(processInstance, DtpConstants.NotQualified.TASK.TASK_PROMPT_REPAIR_WAGGON_REPLACEMENT);

		// request a replacement waggon for 'W2' (for repair has timed out)...
		processRunner.promptRepairWaggonReplacements(processInstance, "W2");

		assertWaitStates(processInstance, DtpConstants.NotQualified.CATCH.CATCH_MSG_REP_REPLACE_ARR);

		// repair replacement waggon arrives...
		processRunner.deliverRepairReplacementWaggons(processInstance, WaggonProcessInfo.fromValues("W1000"));

		// ready to choose an exit track...
		// assertWaitStates(processInstance,
		// DepartTrainProcessConstants.TASK_CHOOSE_EXIT_TRACK);
	}

	/**
	 * ...
	 */
	@Test
	@Deployment(resources = { "departTrainProcess.bpmn" })
	public void testSingleProcessWithMultipleRepairTimeouts() {

		DepartmentProcessRunner processRunner = new DepartmentProcessRunner(processEngine);

		processRunner.withTracks("Track1@true", "TrackExit@true", "TrackReplacement").withWaggons("Track1", "W1@C1#N1",
				"W2@C1", "W3@C1", "W4@C1", "W5");

		ProcessInstance processInstance = processRunner.startDepartureProcess(LocalDateTime.now(),
				new String[] { "W1", "W2", "W3" });

		// assume repairs
		processRunner.assumeWaggonRepairs(processInstance, 12, "W1", "W2", "W3");

		// evaluate repairs
		processRunner.evaluateWaggonRepairs(processInstance, WaggonState.REPAIR_WAGGON, "W1", "W2", "W3");

		// we have 3 prompt waggon repair tasks...
		assertEquals(3, processEngine.getTaskService().createTaskQuery().processInstanceId(processInstance.getId())
				.taskDefinitionKey(DtpConstants.DepartTrain.TASK.TASK_PROMPT_WAGGON_REPAIR).list().size());

		// prompt all to repair...
		processRunner.promptWaggonRepairs(processInstance, "W1", "W2", "W3");

		// we have 3 waggon repair tasks...
		assertEquals(3, processEngine.getTaskService().createTaskQuery()
				.taskDefinitionKey(DtpConstants.NotQualified.TASK.TASK_REPAIR_WAGGON).list().size());

		// ---------------------------------------------------------------------------------------------------

		processRunner.finishWaggonRepair(processInstance, "W3");

		processRunner.timeoutWaggonRepairs(processInstance, "W1");
		assertEquals(1, processRunner.getWaggonsToBePromptedOnRepairTimeout(processInstance).size());
		processRunner.timeoutWaggonRepairs(processInstance, "W2");
		assertEquals(2, processRunner.getWaggonsToBePromptedOnRepairTimeout(processInstance).size());

		processRunner.promptRepairWaggonReplacements(processInstance, "W1");
		processRunner.promptRepairWaggonReplacements(processInstance, "W2");

		// ---------------------------------------------------------------------------------------------------

		/*
		 * // time out and prompt repair waggon replacement for W1...
		 * processRunner.timeoutWaggonRepair(processInstance, "W1");
		 * processRunner.promptRepairWaggonReplacement(processInstance, "W1");
		 * 
		 * // time out and prompt repair waggon replacement for W2...
		 * processRunner.timeoutWaggonRepair(processInstance, "W2");
		 * processRunner.promptRepairWaggonReplacement(processInstance, "W2");
		 */

		// ---------------------------------------------------------------------------------------------------

		// repair W3...
		// processRunner.finishWaggonRepair(processInstance, "W3");

		// assertThat(processInstance).isWaitingAt(DepartTrainProcessConstants.TASK_CHOOSE_EXIT_TRACK);

		// 2 repair waggons to be replaced...
		/*
		 * List<Task> promptRepairWaggonReplacementTaskList =
		 * processEngine.getTaskService().createTaskQuery().processInstanceId(
		 * processInstance.getId()) .taskDefinitionKey(DepartTrainProcessConstants.
		 * TASK_PROMPT_REPAIR_WAGGON_REPLACEMENT).taskAssignee(
		 * DepartTrainProcessConstants.ROLE_DISPONENT).list(); assertEquals(2,
		 * promptRepairWaggonReplacementTaskList.size());
		 */
	}

	/**
	 * W1-W7
	 * 
	 * W1 [Track1] --> OK throughout the whole process
	 * W2 [Track1] --> critical, evaluated to be replaced by W2000
	 * W3 [Track1] --> critical, evaluated to be replaced by W3000
	 * W4 [Track2] --> critical, evaluated to be repaired (repair finishes on time)
	 * W5 [Track2] --> OK throughout the whole process
	 * W6 [Track3] --> critical, evaluated to be repaired (exceeds repair time, replaced by W6000)
	 * W7 [Track3] --> critical, evaluated to be repaired (exceeds repair time, replaced by W7000)
	 * W8 [Track4] --> critical, evaluated to be repaired (repair finishes on time)
	 * 
	 * TrackExit --> exit track
	 * TrackReplacment --> target for delivered waggon replacements
	 */
	@Test
	@Deployment(resources = { "departTrainProcess.bpmn" })
	public void testComplete() {

		DepartmentProcessRunner processRunner = new DepartmentProcessRunner(processEngine);

		processRunner.withTracks("Track1", "Track2", "Track3", "Track4", "TrackExit@true", "TrackReplacement")
				.withWaggons("Track1", "W1", "W2@C1", "W3@C1").withWaggons("Track2", "W4@C1", "W5")
				.withWaggons("Track3", "W6@C1", "W7@C1").withWaggons("Track4", "W8@C1");

		RailwayStationBusinessLogic.getInstance().print("initial", true);

		ProcessInstance processInstance = processRunner.startDepartureProcess(LocalDateTime.now(),
				new String[] { "W1", "W2", "W3", "W4", "W5", "W6", "W7", "W8" });

		// we have a department order
		assertNotNull(RailwayStationBusinessLogic.getInstance().getDepartingOrder(processInstance.getBusinessKey()));

		assertProcessWaggonStates(processEngine, processInstance, "W1", WaggonState.OK, "W2", WaggonState.TO_BE_ASSUMED,
				"W3", WaggonState.TO_BE_ASSUMED, "W4", WaggonState.TO_BE_ASSUMED, "W5", WaggonState.OK, "W6",
				WaggonState.TO_BE_ASSUMED, "W7", WaggonState.TO_BE_ASSUMED, "W8", WaggonState.TO_BE_ASSUMED);

		// 6 waggons to be assumed...
		assertEquals(6, processEngine.getTaskService().createTaskQuery()
				.taskDefinitionKey(DtpConstants.Facility.TASK.TASK_ASSUME_REPAIR_TIME).list().size());

		processRunner.assumeWaggonRepairs(processInstance, 12, "W2", "W3", "W4", "W6", "W7", "W8");

		// all waggones were assumed now...
		assertProcessWaggonStates(processEngine, processInstance, "W1", WaggonState.OK, "W2", WaggonState.ASSUMED, "W3",
				WaggonState.ASSUMED, "W4", WaggonState.ASSUMED, "W5", WaggonState.OK, "W6", WaggonState.ASSUMED,
				"W7", WaggonState.ASSUMED, "W8", WaggonState.ASSUMED);

		// 6 waggons to be evaluated...
		assertEquals(6,
				processEngine.getTaskService().createTaskQuery()
						.taskDefinitionKey(DtpConstants.DepartTrain.TASK.TASK_EVALUATE_WAGGON)
						.processInstanceId(processInstance.getId()).list().size());

		processRunner.evaluateWaggonRepairs(processInstance, WaggonState.REPAIR_WAGGON, "W4", "W6", "W7", "W8");
		processRunner.evaluateWaggonRepairs(processInstance, WaggonState.REPLACE_WAGGON, "W2", "W3");

		// all relevant waggons evaluated...
		assertProcessWaggonStates(processEngine, processInstance, "W1", WaggonState.OK, "W2", WaggonState.REPLACE_WAGGON,
				"W3", WaggonState.REPLACE_WAGGON, "W4", WaggonState.REPAIR_WAGGON, "W5", WaggonState.OK, "W6",
				WaggonState.REPAIR_WAGGON, "W7", WaggonState.REPAIR_WAGGON, "W8", WaggonState.REPAIR_WAGGON);

		// 4 facility processes left (W2, W6, W7, W8)
		assertEquals(4, ensureProcessInstanceCount(DtpConstants.Facility.DEFINITION.PROCESS_REPAIR_FACILITY));

		// prompt repairs (W2, W6, W7, W8)
		processRunner.promptWaggonRepairs(processInstance, "W4", "W6", "W7", "W8");

		// prompt replacements (W3, W4)
		processRunner.promptWaggonReplacements(processInstance, "W2", "W3");

		assertThat(processInstance).isWaitingAt(DtpConstants.DepartTrain.CATCH.CATCH_MSG_REP_WAGG_ARRIVED);

		processRunner.deliverEvaluationReplacementWaggons(processInstance, WaggonProcessInfo.fromValues("W2000", "W2"),
				WaggonProcessInfo.fromValues("W3000", "W3"));

		// choose invalid replacement track
		processRunner.chooseEvaluationReplacementTrack(processInstance, "Track123");
		
		// process must go back to 'TASK_CHOOSE_EVALUATION_REPLACEMENT_TRACK'...
		assertThat(processInstance).isWaitingAt(DtpConstants.DepartTrain.TASK.TASK_CHOOSE_EVALUATION_REPLACEMENT_TRACK);
		
		// choose valid replacement track
		processRunner.chooseEvaluationReplacementTrack(processInstance, "TrackReplacement");
		
		// check replacement track
		assertTrue(RailwayStationBusinessLogic.getInstance().checkTrackWaggons("TrackReplacement", "W2000", "W3000"));
		
		// check process waggons (after above replacements)
		assertEquals(8, getProcessData(processEngine, processInstance).getWaggons().values().size());
		assertProcessWaggonsPresent(processEngine, processInstance, "W1", "W2000", "W3000", "W4", "W5", "W6", "W7", "W8");
		
		assertThat(processInstance).isWaitingAt(DtpConstants.DepartTrain.GATEWAY.GW_AWAIT_REPAIR_OUTCOME);
		
		// all facility still there (W6, W7, W4, W8)
		assertEquals(4, processEngine.getRuntimeService().createProcessInstanceQuery()
				.processDefinitionKey(DtpConstants.Facility.DEFINITION.PROCESS_REPAIR_FACILITY).list().size());
		
		// repairs (timeout [W6, W7] and nominal [W4, W8])
		assertEquals(4,
				processRunner.resolveRepairFacilityProcessesForWaggonNumber(processInstance, "W4", "W6", "W7", "W8").size());
		
		// time out repairs
		processRunner.timeoutWaggonRepairs(processInstance, "W6");
		assertEquals(3,
				processRunner.resolveRepairFacilityProcessesForWaggonNumber(processInstance, "W4", "W7", "W8").size());
		processRunner.timeoutWaggonRepairs(processInstance, "W7");
		assertEquals(2, processRunner.resolveRepairFacilityProcessesForWaggonNumber(processInstance, "W4", "W8").size());
		// repair nominal
		processRunner.finishWaggonRepair(processInstance, "W4");
		assertEquals(1, processRunner.resolveRepairFacilityProcessesForWaggonNumber(processInstance, "W8").size());
		processRunner.finishWaggonRepair(processInstance, "W8");
		
		// all facility process gone
		assertEquals(0, processEngine.getRuntimeService().createProcessInstanceQuery()
				.processDefinitionKey(DtpConstants.Facility.DEFINITION.PROCESS_REPAIR_FACILITY).list().size());
		
		//  prompt repair waggon replacements (W6, W7)
		processRunner.promptRepairWaggonReplacements(processInstance, "W6", "W7");
		
		processRunner.deliverRepairReplacementWaggons(processInstance, WaggonProcessInfo.fromValues("W6000", "W6"),
				WaggonProcessInfo.fromValues("W7000", "W7"));
		
		assertThat(processInstance).isWaitingAt(DtpConstants.DepartTrain.TASK.TASK_CHOOSE_EXIT_TRACK);
	}
}