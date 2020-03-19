package de.gravitex.bpm.traindepartment;

import java.time.LocalDateTime;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

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
		
		assertWaitState(processInstance, DepartTrainProcessConstants.CATCH_MSG_WG_ASSUMED);
		
		processRunner.assumeWaggonRepair(processInstance, "W1", 12);
		processRunner.assumeWaggonRepair(processInstance, "W2", 24);
		
		ensureTaskCountPresent(DepartTrainProcessConstants.TASK_EVALUATE_WAGGON, processInstance.getId(), DepartTrainProcessConstants.ROLE_SUPERVISOR, 2);
	}
}