package de.gravitex.bpm.traindepartment.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;

public class OrderShuntingDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		// start shunting process
		execution.getProcessEngine().getRuntimeService().startProcessInstanceByMessage(DepartTrainProcessConstants.MSG_SH_ORD);
	}
}