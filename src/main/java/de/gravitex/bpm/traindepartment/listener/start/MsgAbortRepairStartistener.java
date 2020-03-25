package de.gravitex.bpm.traindepartment.listener.start;

import org.apache.log4j.Logger;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public class MsgAbortRepairStartistener implements ExecutionListener {

	public static final Logger logger = Logger.getLogger(MsgAbortRepairStartistener.class);

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		WaggonProcessInfo waggonNumber = (WaggonProcessInfo) execution
				.getVariable(DtpConstants.Facility.VAR.VAR_SINGLE_FACILITY_PROCESS_WAGGON);
		logger.info("faicilty process aborted [" + waggonNumber.getWaggonNumber() + "]...");
	}
}