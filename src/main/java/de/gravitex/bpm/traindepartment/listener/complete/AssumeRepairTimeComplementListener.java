package de.gravitex.bpm.traindepartment.listener.complete;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

import org.apache.log4j.Logger;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateTask;

import de.gravitex.bpm.traindepartment.listener.base.TrainDepartmentTaskListener;
import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;
import de.gravitex.bpm.traindepartment.util.HashMapBuilder;

public class AssumeRepairTimeComplementListener extends TrainDepartmentTaskListener {
	
	public static final Logger logger = Logger.getLogger(TrainDepartmentTaskListener.class);

	@SuppressWarnings("unchecked")
	@Override
	public void notify(DelegateTask delegateTask) {
		RuntimeService runtimeService = delegateTask.getProcessEngine().getRuntimeService();
		WaggonProcessInfo assumedWaggon = (WaggonProcessInfo) runtimeService
				.getVariable(delegateTask.getExecutionId(), DtpConstants.Facility.VAR.VAR_SINGLE_FACILITY_PROCESS_WAGGON);
		logger.info("calling back waggon assumement: " + assumedWaggon);
		int singleAssumedTime = (int) runtimeService
				.getVariable(delegateTask.getExecutionId(), DtpConstants.NotQualified.VAR.VAR_ASSUMED_TIME);
		assumedWaggon.setAssumedRepairDuration(singleAssumedTime);
		runtimeService.correlateMessage(DtpConstants.NotQualified.MESSAGE.MSG_REPAIR_ASSUMED,
				(String) runtimeService.getVariable(delegateTask.getExecutionId(),
						DtpConstants.NotQualified.VAR.VAR_DEP_PROC_BK),
				HashMapBuilder.create().withValuePair(DtpConstants.Facility.VAR.VAR_SINGLE_FACILITY_PROCESS_WAGGON, assumedWaggon)
						.build());

		// set repair dead line timer (variable)
		new Date(Timestamp.valueOf(LocalDateTime.now().plusHours(singleAssumedTime)).getTime());
		Date repairDeadline = new Date(Timestamp.valueOf(LocalDateTime.now().plusHours(singleAssumedTime)).getTime());
		runtimeService.setVariable(delegateTask.getExecution().getId(),
				DtpConstants.NotQualified.VAR.VAR_TIMER_EXCEEDED_REPAIR_TIME,
				repairDeadline);
	}
}