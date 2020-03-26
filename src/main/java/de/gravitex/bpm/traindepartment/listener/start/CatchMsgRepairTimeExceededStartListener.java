package de.gravitex.bpm.traindepartment.listener.start;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

import de.gravitex.bpm.traindepartment.logic.DepartmentProcessData;
import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public class CatchMsgRepairTimeExceededStartListener implements ExecutionListener {

	@SuppressWarnings("unchecked")
	@Override
	public void notify(DelegateExecution execution) throws Exception {
		WaggonProcessInfo exceededWaggon = (WaggonProcessInfo) execution
				.getVariable(DtpConstants.NotQualified.VAR.VAR_WAGGON_REPAIR_TIMEOUT);
		List<WaggonProcessInfo> exceededWaggons = (List<WaggonProcessInfo>) execution
				.getVariable(DtpConstants.NotQualified.VAR.VAR_WAGGONS_REPAIR_TIME_EXCEEDED_LIST);
		if (exceededWaggons == null) {
			execution.setVariable(DtpConstants.NotQualified.VAR.VAR_WAGGONS_REPAIR_TIME_EXCEEDED_LIST,
					new ArrayList<WaggonProcessInfo>());
		}
		((List<WaggonProcessInfo>) execution
				.getVariable(DtpConstants.NotQualified.VAR.VAR_WAGGONS_REPAIR_TIME_EXCEEDED_LIST)).add(exceededWaggon);
		// update process waggon
		((DepartmentProcessData) execution.getVariable(DtpConstants.DepartTrain.VAR.VAR_DEPARTMENT_PROCESS_DATA))
				.processWaggonCallback(exceededWaggon);
	}
}