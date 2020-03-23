package de.gravitex.bpm.traindepartment;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.runner.base.DepartmentProcessRunner;

public class DepartTrainProcessRunnerTestCase extends BpmTestCase {

	@Rule
	public ProcessEngineRule processEngine = new ProcessEngineRule();

	@Test
	@Deployment(resources = { "departTrainProcess.bpmn" })
	public void testProcessRunner() {

		DepartmentProcessRunner processRunner = new DepartmentProcessRunner(processEngine);

		processRunner.clear();

		processRunner.withTracks("Track1@true", "TrackExit@true", "TrackReplacement").withWaggons("Track1", "W1@C1#N1", "W2@C1",
				"W3@C1", "W4@C1", "W5");

		ProcessInstance processInstance = processRunner.startDepartureProcess(LocalDateTime.now(), new String[] { "W1", "W2" });

		assertWaitStates(processInstance, DepartTrainProcessConstants.CATCH_MSG_WG_ASSUMED);

		// assume repairs
		processRunner.assumeWaggonRepairs(processInstance, 12, "W1");
		processRunner.assumeWaggonRepairs(processInstance, 24, "W2");

		ensureTaskCountPresent(processInstance, DepartTrainProcessConstants.TASK_EVALUATE_WAGGON,
				DepartTrainProcessConstants.ROLE_SUPERVISOR, 2);

		// evaluate repairs
		processRunner.evaluateWaggonRepairs(processInstance, WaggonState.REPAIR_WAGGON, "W1", "W2");

		assertWaitStates(processInstance, DepartTrainProcessConstants.TASK_PROMPT_WAGGON_REPAIR);

		// prompt repairs
		processRunner.promptWaggonRepairs(processInstance, "W1", "W2");

		assertWaitStates(processInstance, DepartTrainProcessConstants.GW_AWAIT_REPAIR_OUTCOME);

		// finish WaggonRepairs
		processRunner.finishWaggonRepair(processInstance, "W1");
		processRunner.timeoutWaggonRepair(processInstance, "W2");

		assertWaitStates(processInstance, DepartTrainProcessConstants.TASK_PROMPT_REPAIR_WAGGON_REPLACEMENT);

		// request a replacement waggon for 'W2' (for repair has timed out)...
		processRunner.promptRepairWaggonReplacement(processInstance, "W2");

		assertWaitStates(processInstance, DepartTrainProcessConstants.CATCH_MSG_REP_REPLACE_ARR);

		// repair replacement waggon arrives...
		processRunner.deliverRepairReplacementWaggon(processInstance, "W1000");

		// ready to choose an exit track...
		assertWaitStates(processInstance, DepartTrainProcessConstants.TASK_CHOOSE_EXIT_TRACK);
	}

	/**
	 * ...
	 */
	@Test
	@Deployment(resources = { "departTrainProcess.bpmn" })
	public void testSingleProcessWithMultipleRepairTimeouts() {

		DepartmentProcessRunner processRunner = new DepartmentProcessRunner(processEngine);

		processRunner.clear();

		processRunner.withTracks("Track1@true", "TrackExit@true", "TrackReplacement").withWaggons("Track1", "W1@C1#N1", "W2@C1",
				"W3@C1", "W4@C1", "W5");

		ProcessInstance processInstance = processRunner.startDepartureProcess(LocalDateTime.now(), new String[] { "W1", "W2", "W3" });

		// assume repairs
		processRunner.assumeWaggonRepairs(processInstance, 12, "W1", "W2", "W3");

		// evaluate repairs
		processRunner.evaluateWaggonRepairs(processInstance, WaggonState.REPAIR_WAGGON, "W1", "W2", "W3");
		
		// we have 3 prompt waggon repair tasks...
		assertEquals(3, processEngine.getTaskService().createTaskQuery().processInstanceId(processInstance.getId())
				.taskDefinitionKey(DepartTrainProcessConstants.TASK_PROMPT_WAGGON_REPAIR).list().size());

		// prompt all to repair...
		processRunner.promptWaggonRepairs(processInstance, "W1", "W2", "W3");

		// we have 3 waggon repair tasks...
		assertEquals(3, processEngine.getTaskService().createTaskQuery()
				.taskDefinitionKey(DepartTrainProcessConstants.TASK_REPAIR_WAGGON).list().size());

		// time out W1+W2...
		processRunner.timeoutWaggonRepair(processInstance, "W1");
		processRunner.timeoutWaggonRepair(processInstance, "W2");
		
		// repair W3...
		processRunner.finishWaggonRepair(processInstance, "W3");
		
		// 2 repair waggons to be replaced...
		assertEquals(2, processEngine.getTaskService().createTaskQuery().processInstanceId(processInstance.getId())
				.taskDefinitionKey(DepartTrainProcessConstants.TASK_PROMPT_REPAIR_WAGGON_REPLACEMENT).list().size());
		
		processRunner.promptRepairWaggonReplacement(processInstance, "W1");
	}
}