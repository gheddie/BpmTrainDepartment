package de.gravitex.bpm.traindepartment.delegate;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.delegate.base.TrainDepartmentJavaDelegate;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;
import de.gravitex.bpm.traindepartment.logic.businesskey.RepairFacilityBusinessKeyCreator;
import de.gravitex.bpm.traindepartment.util.HashMapBuilder;

public class ProcessCriticalErrorsDelegate extends TrainDepartmentJavaDelegate {

	@SuppressWarnings("unchecked")
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		String facilityProcessBusinessKey = null;
		for (WaggonProcessInfo plannedWaggon : getWaggons(execution)) {
			if (RailwayStationBusinessLogic.getInstance().isWaggonCritical(plannedWaggon.getWaggonNumber())) {
				facilityProcessBusinessKey = RailwayStationBusinessLogic.getInstance()
						.generateBusinessKey(DepartTrainProcessConstants.PROCESS_REPAIR_FACILITY, HashMapBuilder.create()
								.withValuePair(RepairFacilityBusinessKeyCreator.AV_WAGGON_NUMBER, plannedWaggon.getWaggonNumber())
								.build(), execution.getBusinessKey());
				plannedWaggon.setFacilityProcessBusinessKey(facilityProcessBusinessKey);
				// pass master process business key to call back...
				execution.getProcessEngine().getRuntimeService().startProcessInstanceByMessage(
						DepartTrainProcessConstants.MSG_INVOKE_WAG_ASSUMEMENT, facilityProcessBusinessKey,
						HashMapBuilder.create()
								.withValuePair(DepartTrainProcessConstants.VAR_DEP_PROC_BK, execution.getBusinessKey())
								.withValuePair(DepartTrainProcessConstants.VAR_SINGLE_FACILITY_PROCESS_WAGGON, plannedWaggon)
								.build());
			}
		}
	}
}