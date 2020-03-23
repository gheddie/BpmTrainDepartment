package de.gravitex.bpm.traindepartment.listener.start;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public class CatchMsgRepairTimeExceededStartListener implements ExecutionListener {

	@SuppressWarnings("unchecked")
	@Override
	public void notify(DelegateExecution execution) throws Exception {
		WaggonProcessInfo exceededWaggon = (WaggonProcessInfo) execution.getVariable(DepartTrainProcessConstants.VAR_WAGGON_REPAIR_TIMEOUT);
		List<WaggonProcessInfo> exceededWaggons =  (List<WaggonProcessInfo>) execution.getVariable(DepartTrainProcessConstants.VAR_WAGGONS_REPAIR_TIME_EXCEEDED_LIST);
		if (exceededWaggons == null) {
			execution.setVariable(DepartTrainProcessConstants.VAR_WAGGONS_REPAIR_TIME_EXCEEDED_LIST, new ArrayList<WaggonProcessInfo>());
		}
		((List<WaggonProcessInfo>) execution.getVariable(DepartTrainProcessConstants.VAR_WAGGONS_REPAIR_TIME_EXCEEDED_LIST)).add(exceededWaggon);
	}
}