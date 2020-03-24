package de.gravitex.bpm.traindepartment.delegate;

import org.apache.log4j.Logger;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import de.gravitex.bpm.traindepartment.delegate.base.FacilityProcessDelegate;
import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;
import de.gravitex.bpm.traindepartment.util.HashMapBuilder;

public class RepairTimeExceededDelegate extends FacilityProcessDelegate {

	public static final Logger logger = Logger.getLogger(RepairTimeExceededDelegate.class);

	@SuppressWarnings("unchecked")
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		WaggonProcessInfo facilityWaggon = getFacilityWaggon(execution);
		facilityWaggon.setWaggonState(WaggonState.REPAIR_EXCEEDED);
		RuntimeService runtimeService = execution.getProcessEngine().getRuntimeService();
		runtimeService.correlateMessage(DtpConstants.Main.MESSAGE.MSG_REPAIR_TIME_EXCEEDED,
				(String) runtimeService.getVariable(execution.getId(), DtpConstants.NotQualified.VAR.VAR_DEP_PROC_BK),
				HashMapBuilder.create().withValuePair(DtpConstants.NotQualified.VAR.VAR_WAGGON_REPAIR_TIMEOUT, facilityWaggon).build());
		logger.info("correlated message '" + DtpConstants.Main.MESSAGE.MSG_REPAIR_TIME_EXCEEDED + "' for waggon: "
				+ facilityWaggon.getWaggonNumber());
	}
}