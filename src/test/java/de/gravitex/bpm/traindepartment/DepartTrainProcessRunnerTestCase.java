package de.gravitex.bpm.traindepartment;

import java.time.LocalDateTime;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.runner.EvaluateAllToRepairProcessRunner;

public class DepartTrainProcessRunnerTestCase extends BpmTestCase {
	
	@Rule
	public ProcessEngineRule processEngine = new ProcessEngineRule();

	@Test
	@Deployment(resources = { "departTrainProcess.bpmn" })
	public void testProcessRunner() {

		EvaluateAllToRepairProcessRunner processRunner = new EvaluateAllToRepairProcessRunner(processEngine);
		
		processRunner.clear();
		
		processRunner.withTracks("Track1@true", "TrackExit@true", "TrackReplacement").withWaggons("Track1", "W1@C1#N1", "W2@C1",
				"W3@C1", "W4@C1", "W5");

		ProcessInstance processInstance = processRunner.startDepartureProcess(LocalDateTime.now(), new String[] { "W1", "W2" });
		
		assertWaitStates(processInstance, DepartTrainProcessConstants.CATCH_MSG_WG_ASSUMED);

		// assume repairs
		processRunner.assumeWaggonRepair(processInstance, "W1", 12);
		processRunner.assumeWaggonRepair(processInstance, "W2", 24);
		
		ensureTaskCountPresent(processInstance, DepartTrainProcessConstants.TASK_EVALUATE_WAGGON, DepartTrainProcessConstants.ROLE_SUPERVISOR, 2);
		
		// evaluate repairs
		processRunner.evaluateWaggonRepair(processInstance, "W1", WaggonState.REPAIR_WAGGON);
		processRunner.evaluateWaggonRepair(processInstance, "W2", WaggonState.REPAIR_WAGGON);
		
		assertWaitStates(processInstance, DepartTrainProcessConstants.TASK_PROMPT_WAGGON_REPAIR);
		
		// prompt repairs
		processRunner.promptWaggonRepair(processInstance, "W1");
		processRunner.promptWaggonRepair(processInstance, "W2");
		
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
}