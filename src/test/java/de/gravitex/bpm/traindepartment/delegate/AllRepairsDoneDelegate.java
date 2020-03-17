package de.gravitex.bpm.traindepartment.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class AllRepairsDoneDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		// TODO
		// execution.setVariable(DepartTrainProcessConstants.VAR_ALL_REPAIRS_DONE, false);
		// execution.setVariable("dollesDing", new DollesDing());
	}
}