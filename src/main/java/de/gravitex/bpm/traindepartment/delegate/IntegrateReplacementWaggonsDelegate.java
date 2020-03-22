package de.gravitex.bpm.traindepartment.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import de.gravitex.bpm.traindepartment.delegate.base.TrainDepartmentJavaDelegate;
import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public class IntegrateReplacementWaggonsDelegate extends TrainDepartmentJavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		String[] deliveredWaggons = (String[]) execution.getProcessEngine().getRuntimeService().getVariable(execution.getId(), DepartTrainProcessConstants.VAR_DELIVERED_REPLACMENT_WAGGONS);
		for (String deliveredWaggon : deliveredWaggons) {
			getProcessData(execution).addWaggon(WaggonProcessInfo.fromValues(deliveredWaggon, WaggonState.REPLACED));			
		}
	}
}