package de.gravitex.bpm.traindepartment.delegate;

import org.apache.log4j.Logger;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import de.gravitex.bpm.traindepartment.delegate.base.TrainDepartmentJavaDelegate;
import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;
import de.gravitex.bpm.traindepartment.logic.businesskey.RepairFacilityBusinessKeyCreator;
import de.gravitex.bpm.traindepartment.util.HashMapBuilder;

public class ProcessCriticalErrorsDelegate extends TrainDepartmentJavaDelegate {
	
	public static final Logger logger = Logger.getLogger(ProcessCriticalErrorsDelegate.class);

	@SuppressWarnings("unchecked")
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		String facilityProcessBusinessKey = null;
		for (WaggonProcessInfo plannedWaggon : getWaggons(execution)) {
			if (RailwayStationBusinessLogic.getInstance().isWaggonCritical(plannedWaggon.getWaggonNumber())) {
				facilityProcessBusinessKey = RailwayStationBusinessLogic.getInstance()
						.generateBusinessKey(DtpConstants.Facility.DEFINITION.PROCESS_REPAIR_FACILITY, HashMapBuilder.create()
								.withValuePair(RepairFacilityBusinessKeyCreator.AV_WAGGON_NUMBER, plannedWaggon.getWaggonNumber())
								.build(), execution.getBusinessKey());
				plannedWaggon.setFacilityProcessBusinessKey(facilityProcessBusinessKey);
				plannedWaggon.setWaggonState(WaggonState.TO_BE_ASSUMED);
				// pass master process business key to call back...
				execution.getProcessEngine().getRuntimeService().startProcessInstanceByMessage(
						DtpConstants.Facility.MESSAGE.MSG_INVOKE_WAG_ASSUMEMENT, facilityProcessBusinessKey,
						HashMapBuilder.create()
								.withValuePair(DtpConstants.NotQualified.VAR.VAR_DEP_PROC_BK, execution.getBusinessKey())
								.withValuePair(DtpConstants.Facility.VAR.VAR_SINGLE_FACILITY_PROCESS_WAGGON, plannedWaggon)
								.build());
				logger.info("started facility process for waggon: " + plannedWaggon.getWaggonNumber());
			}
		}
	}
}