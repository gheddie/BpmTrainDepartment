package de.gravitex.bpm.traindepartment.delegate;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import de.gravitex.bpm.traindepartment.delegate.base.FacilityProcessDelegate;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;
import de.gravitex.bpm.traindepartment.util.HashMapBuilder;

public class RepairTimeExceededDelegate extends FacilityProcessDelegate {

	@SuppressWarnings("unchecked")
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		WaggonProcessInfo facilityWaggon = getFacilityWaggon(execution);
		RuntimeService runtimeService = execution.getProcessEngine().getRuntimeService();
		runtimeService.correlateMessage(DepartTrainProcessConstants.MSG_REPAIR_TIME_EXCEEDED,
				(String) runtimeService.getVariable(execution.getId(), DepartTrainProcessConstants.VAR_DEP_PROC_BK),
				HashMapBuilder.create().withValuePair(DepartTrainProcessConstants.VAR_REPAIRED_WAGGON, facilityWaggon).build());
	}
}