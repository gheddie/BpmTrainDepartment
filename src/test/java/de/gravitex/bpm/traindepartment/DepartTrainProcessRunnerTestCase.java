package de.gravitex.bpm.traindepartment;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;
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

		processRunner.withTracks("Track1@true", "TrackExit@true", "TrackReplacement").withWaggons("Track1", "W1@C1#N1", "W2@C1",
				"W3@C1", "W4@C1", "W5");

		ProcessInstance processInstance = processRunner.startDepartureProcess(LocalDateTime.now(), new String[] { "W1", "W2" });

		assertWaitStates(processInstance, DtpConstants.NotQualified.CATCH.CATCH_MSG_WG_ASSUMED);

		// assume repairs
		processRunner.assumeWaggonRepairs(processInstance, 12, "W1");
		processRunner.assumeWaggonRepairs(processInstance, 24, "W2");

		ensureTaskCountPresent(processInstance, DtpConstants.Main.TASK.TASK_EVALUATE_WAGGON,
				DtpConstants.NotQualified.ROLE.ROLE_SUPERVISOR, 2);

		// evaluate repairs
		processRunner.evaluateWaggonRepairs(processInstance, WaggonState.REPAIR_WAGGON, "W1", "W2");

		assertWaitStates(processInstance, DtpConstants.NotQualified.TASK.TASK_PROMPT_WAGGON_REPAIR);

		// prompt repairs
		processRunner.promptWaggonRepairs(processInstance, "W1", "W2");

		assertWaitStates(processInstance, DtpConstants.NotQualified.GATEWAY.GW_AWAIT_REPAIR_OUTCOME);

		// finish WaggonRepairs
		processRunner.finishWaggonRepair(processInstance, "W1");
		processRunner.timeoutWaggonRepair(processInstance, "W2");

		assertWaitStates(processInstance, DtpConstants.NotQualified.TASK.TASK_PROMPT_REPAIR_WAGGON_REPLACEMENT);

		// request a replacement waggon for 'W2' (for repair has timed out)...
		processRunner.promptRepairWaggonReplacement(processInstance, "W2");

		assertWaitStates(processInstance, DtpConstants.NotQualified.CATCH.CATCH_MSG_REP_REPLACE_ARR);

		// repair replacement waggon arrives...
		processRunner.deliverRepairReplacementWaggon(processInstance, "W1000");

		// ready to choose an exit track...
		// assertWaitStates(processInstance, DepartTrainProcessConstants.TASK_CHOOSE_EXIT_TRACK);
	}

	/**
	 * ...
	 */
	@Test
	@Deployment(resources = { "departTrainProcess.bpmn" })
	public void testSingleProcessWithMultipleRepairTimeouts() {

		DepartmentProcessRunner processRunner = new DepartmentProcessRunner(processEngine);

		processRunner.withTracks("Track1@true", "TrackExit@true", "TrackReplacement").withWaggons("Track1", "W1@C1#N1", "W2@C1",
				"W3@C1", "W4@C1", "W5");

		ProcessInstance processInstance = processRunner.startDepartureProcess(LocalDateTime.now(), new String[] { "W1", "W2", "W3" });

		// assume repairs
		processRunner.assumeWaggonRepairs(processInstance, 12, "W1", "W2", "W3");

		// evaluate repairs
		processRunner.evaluateWaggonRepairs(processInstance, WaggonState.REPAIR_WAGGON, "W1", "W2", "W3");
		
		// we have 3 prompt waggon repair tasks...
		assertEquals(3, processEngine.getTaskService().createTaskQuery().processInstanceId(processInstance.getId())
				.taskDefinitionKey(DtpConstants.NotQualified.TASK.TASK_PROMPT_WAGGON_REPAIR).list().size());

		// prompt all to repair...
		processRunner.promptWaggonRepairs(processInstance, "W1", "W2", "W3");

		// we have 3 waggon repair tasks...
		assertEquals(3, processEngine.getTaskService().createTaskQuery()
				.taskDefinitionKey(DtpConstants.NotQualified.TASK.TASK_REPAIR_WAGGON).list().size());
		
		// ---------------------------------------------------------------------------------------------------
		
		processRunner.finishWaggonRepair(processInstance, "W3");
		
		processRunner.timeoutWaggonRepair(processInstance, "W1");
		assertEquals(1, processRunner.getWaggonsToBePromptedOnRepairTimeout(processInstance).size());
		processRunner.timeoutWaggonRepair(processInstance, "W2");
		assertEquals(2, processRunner.getWaggonsToBePromptedOnRepairTimeout(processInstance).size());
		
		processRunner.promptRepairWaggonReplacement(processInstance, "W1");
		processRunner.promptRepairWaggonReplacement(processInstance, "W2");
		
		// ---------------------------------------------------------------------------------------------------
		
		/*
		// time out and prompt repair waggon replacement for W1...
		processRunner.timeoutWaggonRepair(processInstance, "W1");
		processRunner.promptRepairWaggonReplacement(processInstance, "W1");

		// time out and prompt repair waggon replacement for W2...
		processRunner.timeoutWaggonRepair(processInstance, "W2");
		processRunner.promptRepairWaggonReplacement(processInstance, "W2");
		*/
		
		// ---------------------------------------------------------------------------------------------------
		
		// repair W3...
		// processRunner.finishWaggonRepair(processInstance, "W3");
		
		// assertThat(processInstance).isWaitingAt(DepartTrainProcessConstants.TASK_CHOOSE_EXIT_TRACK);
		
		// 2 repair waggons to be replaced...
		/*
		List<Task> promptRepairWaggonReplacementTaskList = processEngine.getTaskService().createTaskQuery().processInstanceId(processInstance.getId())
				.taskDefinitionKey(DepartTrainProcessConstants.TASK_PROMPT_REPAIR_WAGGON_REPLACEMENT).taskAssignee(DepartTrainProcessConstants.ROLE_DISPONENT).list();
		assertEquals(2, promptRepairWaggonReplacementTaskList.size());
		*/
	}
	
	/**
	 * W1-W7
	 * 
	 * W1 [Track1]				--> OK throughout the whole process
	 * W2 [Track1]				--> critical, evaluted to be replaced by W2000 
	 * W3 [Track1]				--> critical, evaluted to be replaced by W3000
	 * W4 [Track2]				--> critical, evaluted to be repaired (repair finishes on time)
	 * W5 [Track2]				--> OK throughout the whole process
	 * W6 [Track3]				--> critical, evaluted to be repaired (exceeds repair time, replaced by W6000)
	 * W7 [Track3]				--> critical, evaluted to be repaired (exceeds repair time, replaced by W7000)
	 * W8 [Track4]				--> critical, evaluted to be repaired (repair finishes on time)
	 * 
	 * TrackExit		--> exit track
	 * TrackReplacment	--> target for delivered waggon replacements
	 */
	@Test
	@Deployment(resources = { "departTrainProcess.bpmn" })
	public void testComplete() {
		
		DepartmentProcessRunner processRunner = new DepartmentProcessRunner(processEngine);

		processRunner.withTracks("Track1", "Track2", "Track3", "Track4", "TrackExit", "TrackReplacement")
				.withWaggons("Track1", "W1", "W2@C1", "W3@C1").withWaggons("Track2", "W4@C1", "W5")
				.withWaggons("Track3", "W6@C1", "W7@C1").withWaggons("Track4", "W8@C1");

		RailwayStationBusinessLogic.getInstance().print("initial", true);

		ProcessInstance processInstance = processRunner.startDepartureProcess(LocalDateTime.now(),
				new String[] { "W1", "W2", "W3", "W4", "W5", "W6", "W7", "W8" });
		
		// 6 waggons to be assumed...
		assertEquals(6, processEngine.getTaskService().createTaskQuery()
				.taskDefinitionKey(DtpConstants.Facility.TASK.TASK_ASSUME_REPAIR_TIME).list().size());
		
		processRunner.assumeWaggonRepairs(processInstance, 12, "W2", "W3", "W4", "W6", "W7", "W8");
		
		// 6 waggons to be evaluated...
		assertEquals(6,
				processEngine.getTaskService().createTaskQuery()
						.taskDefinitionKey(DtpConstants.Main.TASK.TASK_EVALUATE_WAGGON)
						.processInstanceId(processInstance.getId()).list().size());
	}
}