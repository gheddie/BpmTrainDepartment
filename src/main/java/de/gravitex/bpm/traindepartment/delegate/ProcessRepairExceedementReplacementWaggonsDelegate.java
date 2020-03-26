package de.gravitex.bpm.traindepartment.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import de.gravitex.bpm.traindepartment.delegate.base.ReplaceWaggonsJavaDelegate;
import de.gravitex.bpm.traindepartment.logic.DepartmentProcessData;
import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public class ProcessRepairExceedementReplacementWaggonsDelegate extends ReplaceWaggonsJavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		DepartmentProcessData processData = getProcessData(execution);
		
		WaggonProcessInfo[] deliveredWaggons = (WaggonProcessInfo[]) execution
				.getVariable(DtpConstants.DepartTrain.VAR.VAR_DELIVERED_REP_TIMEOUT_REPLACMENT_WAGGONS);
		
		replaceWaggons(processData, deliveredWaggons);
	}
}