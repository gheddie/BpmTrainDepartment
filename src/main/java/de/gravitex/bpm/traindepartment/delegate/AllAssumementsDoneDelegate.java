package de.gravitex.bpm.traindepartment.delegate;

import org.apache.log4j.Logger;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DepartmentProcessData;
import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public class AllAssumementsDoneDelegate implements JavaDelegate {

	public static final Logger logger = Logger.getLogger(AllAssumementsDoneDelegate.class);

	@Override
	public void execute(DelegateExecution execution) throws Exception {

		WaggonProcessInfo actuallyAssumed = (WaggonProcessInfo) execution
				.getVariable(DtpConstants.Facility.VAR.VAR_SINGLE_FACILITY_PROCESS_WAGGON);
		logger.info("received repair assumement : " + actuallyAssumed);
		DepartmentProcessData departmentProcessData = (DepartmentProcessData) execution
				.getVariable(DtpConstants.DepartTrain.VAR.VAR_DEPARTMENT_PROCESS_DATA);
		departmentProcessData.processRepairAssumption(actuallyAssumed.getWaggonNumber(),
				actuallyAssumed.getAssumedRepairDuration(), actuallyAssumed.getFacilityProcessBusinessKey());
	}
}