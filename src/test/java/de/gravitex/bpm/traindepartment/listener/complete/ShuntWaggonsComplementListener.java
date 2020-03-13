package de.gravitex.bpm.traindepartment.listener.complete;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class ShuntWaggonsComplementListener implements TaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		runtimeService().correlateMessage("MSG_SH_DONE");
	}
}