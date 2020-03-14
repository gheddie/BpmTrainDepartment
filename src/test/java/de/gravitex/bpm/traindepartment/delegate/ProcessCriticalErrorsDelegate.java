package de.gravitex.bpm.traindepartment.delegate;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;
import de.gravitex.bpm.traindepartment.logic.businesskey.RepairFacilityBusinessKeyCreator;
import de.gravitex.bpm.traindepartment.util.HashMapBuilder;

public class ProcessCriticalErrorsDelegate implements JavaDelegate {

	@SuppressWarnings("unchecked")
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		List<String> plannedWaggons = (List<String>) execution.getVariable(DepartTrainProcessConstants.VAR_PLANNED_WAGGONS);
		List<String> waggonsToAssume = new ArrayList<String>();
		String subProcessBusinessKey = null;
		for (String plannedWaggon : plannedWaggons) {
			if (RailwayStationBusinessLogic.getInstance().isWaggonCritical(plannedWaggon)) {
				subProcessBusinessKey = RailwayStationBusinessLogic.getInstance()
						.generateBusinessKey(DepartTrainProcessConstants.PROCESS_REPAIR_FACILITY, HashMapBuilder.create()
								.withValuePair(RepairFacilityBusinessKeyCreator.AV_WAGGON_NUMBER, plannedWaggon).build(), execution.getBusinessKey());
				// pass master process business key to call back...
				execution.getProcessEngine().getRuntimeService().startProcessInstanceByMessage(
						DepartTrainProcessConstants.MSG_INVOKE_WAG_ASSUMEMENT, subProcessBusinessKey,
						HashMapBuilder.create()
								.withValuePair(DepartTrainProcessConstants.VAR_DEP_PROC_BK, execution.getBusinessKey())
								.withValuePair(DepartTrainProcessConstants.VAR_SINGLE_WAGGON_TO_ASSUME, plannedWaggon).build());
				// store waggons to repair in 'VAR_WAGGONS_TO_REPAIR'
				waggonsToAssume.add(plannedWaggon);
			}
		}
		execution.setVariable(DepartTrainProcessConstants.VAR_WAGGONS_TO_ASSUME, waggonsToAssume);
	}
}