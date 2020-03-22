package de.gravitex.bpm.traindepartment.listener.complete;

import org.apache.log4j.Logger;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import de.gravitex.bpm.traindepartment.delegate.AllAssumementsDoneDelegate;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public class PromptWaggonRepairComplementListener implements TaskListener {
	
	public static final Logger logger = Logger.getLogger(PromptWaggonRepairComplementListener.class);

	@Override
	public void notify(DelegateTask delegateTask) {
		RuntimeService runtimeService = delegateTask.getProcessEngine().getRuntimeService();
		// single info stored in 'VAR_PROMPT_REPAIR_WAGGON'...
		WaggonProcessInfo info = (WaggonProcessInfo) runtimeService.getVariable(delegateTask.getExecution().getId(),
				DepartTrainProcessConstants.VAR_PROMPT_REPAIR_WAGGON);
		String facilityProcessBusinessKey = info.getFacilityProcessBusinessKey();
		runtimeService.correlateMessage(DepartTrainProcessConstants.MSG_START_REPAIR, facilityProcessBusinessKey);
		logger.info("successfully correlated 'MSG_START_REPAIR' message for waggon " + info.getWaggonNumber());
	}
}