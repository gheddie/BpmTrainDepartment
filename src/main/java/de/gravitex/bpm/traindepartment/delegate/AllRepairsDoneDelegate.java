package de.gravitex.bpm.traindepartment.delegate;

import org.apache.log4j.Logger;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.DepartmentProcessData;

public class AllRepairsDoneDelegate implements JavaDelegate {
	
	public static final Logger logger = Logger.getLogger(AllRepairsDoneDelegate.class);

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		String waggonNumber = (String) execution.getVariable(DepartTrainProcessConstants.VAR_REPAIRED_WAGGON);
		logger.info("Received repair waggon callback : " + waggonNumber);
		((DepartmentProcessData) execution.getProcessEngine().getRuntimeService().getVariable(execution.getId(),
				DepartTrainProcessConstants.VAR_DEPARTMENT_PROCESS_DATA))
						.processRepairCallback(waggonNumber);
	}
}