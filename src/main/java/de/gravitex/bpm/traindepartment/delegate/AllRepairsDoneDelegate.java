package de.gravitex.bpm.traindepartment.delegate;

import org.apache.log4j.Logger;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.DepartmentProcessData;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public class AllRepairsDoneDelegate implements JavaDelegate {
	
	public static final Logger logger = Logger.getLogger(AllRepairsDoneDelegate.class);

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		WaggonProcessInfo waggonProcessInfo = (WaggonProcessInfo) execution.getVariable(DtpConstants.NotQualified.VAR.VAR_REPAIRED_WAGGON);
		logger.info("Received repair waggon callback : " + waggonProcessInfo.getWaggonNumber());
		((DepartmentProcessData) execution.getProcessEngine().getRuntimeService().getVariable(execution.getId(),
				DtpConstants.NotQualified.VAR.VAR_DEPARTMENT_PROCESS_DATA))
						.processRepairCallback(waggonProcessInfo);
	}
}