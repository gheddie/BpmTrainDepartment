package de.gravitex.bpm.traindepartment.listener.complete;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.Date;

import org.apache.log4j.Logger;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import de.gravitex.bpm.traindepartment.delegate.AllRepairsDoneDelegate;
import de.gravitex.bpm.traindepartment.listener.base.TrainDepartmentTaskListener;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;
import de.gravitex.bpm.traindepartment.util.HashMapBuilder;

public class RepairAssumenentComplementListener extends TrainDepartmentTaskListener {
	
	public static final Logger logger = Logger.getLogger(TrainDepartmentTaskListener.class);

	@SuppressWarnings("unchecked")
	@Override
	public void notify(DelegateTask delegateTask) {
		RuntimeService runtimeService = delegateTask.getProcessEngine().getRuntimeService();
		String assumedWaggon = (String) runtimeService
				.getVariable(delegateTask.getExecutionId(), DepartTrainProcessConstants.VAR_SINGLE_FACILITY_PROCESS_WAGGON);
		logger.info("calling back waggon assumement: " + assumedWaggon);
		int singleAssumedTime = (int) runtimeService
				.getVariable(delegateTask.getExecutionId(), DepartTrainProcessConstants.VAR_ASSUMED_TIME);
		WaggonProcessInfo callback = WaggonProcessInfo.fromValues(assumedWaggon, singleAssumedTime,
				delegateTask.getExecution().getBusinessKey());
		runtimeService.correlateMessage(DepartTrainProcessConstants.MSG_REPAIR_ASSUMED,
				(String) runtimeService.getVariable(delegateTask.getExecutionId(),
						DepartTrainProcessConstants.VAR_DEP_PROC_BK),
				HashMapBuilder.create().withValuePair(DepartTrainProcessConstants.VAR_SINGLE_FACILITY_PROCESS_WAGGON, callback)
						.build());

		// set repair dead line timer (variable)
		new Date(Timestamp.valueOf(LocalDateTime.now().plusHours(singleAssumedTime)).getTime());
		Date repairDeadline = new Date(Timestamp.valueOf(LocalDateTime.now().plusHours(singleAssumedTime)).getTime());
		runtimeService.setVariable(delegateTask.getExecution().getId(),
				DepartTrainProcessConstants.VAR_TIMER_EXCEEDED_REPAIR_TIME,
				repairDeadline);
	}
}