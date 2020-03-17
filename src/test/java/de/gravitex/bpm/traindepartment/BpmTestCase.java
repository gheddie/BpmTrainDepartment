package de.gravitex.bpm.traindepartment;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.managementService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskService;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;

import de.gravitex.bpm.traindepartment.util.RailTestUtil;

public class BpmTestCase {

	protected Task ensureSingleTaskPresent(String taskName, String role, boolean executeTask, Map<String, Object> variables) {
		return ensureSingleTaskPresent(taskName, null, role, executeTask, variables);
	}

	protected Task ensureSingleTaskPresent(String taskName, String businessKey, String role, boolean executeTask, Map<String, Object> variables) {
		List<Task> taskList = null;
		if (businessKey != null) {
			// regard business key in addition...
			TaskQuery queryBk = taskService().createTaskQuery().taskDefinitionKey(taskName).processInstanceBusinessKey(businessKey);
			if (role != null) {
				queryBk.taskAssignee(role);
			}
			taskList = queryBk.list();
		} else {
			// do not regard business key...
			TaskQuery queryNoBk = taskService().createTaskQuery().taskDefinitionKey(taskName);
			if (role != null) {
				queryNoBk.taskAssignee(role);
			}
			taskList = queryNoBk.list();
		}
		assertEquals(1, taskList.size());
		Task task = taskList.get(0);
		if (executeTask) {
			taskService().complete(task.getId(), variables);
		}
		return task;
	}

	protected List<Task> ensureTaskCountPresent(String taskName, String processInstanceId, String role, int taskCount) {
		TaskQuery query = taskService().createTaskQuery().taskDefinitionKey(taskName);
		if (role != null) {
			query.taskAssignee(role);
		}
		if (processInstanceId != null) {
			query.processInstanceId(processInstanceId);
		}
		List<Task> taskList = query.list();
		assertEquals(taskCount, taskList.size());
		return taskList;
	}

	protected boolean ensureTaskNotPresent(String taskName) {
		return (taskService().createTaskQuery().taskDefinitionKey(taskName).list().size() == 0);
	}

	protected void ensureProcessesRunning(String processDefinitionKey, int count) {
		List<ProcessInstance> processInstancesList = runtimeService().createProcessInstanceQuery()
				.processDefinitionKey(processDefinitionKey).list();
		assertEquals(count, processInstancesList.size());
	}

	protected void ensureProcessesRunning(int count) {
		List<ProcessInstance> processInstancesList = runtimeService().createProcessInstanceQuery().list();
		assertEquals(count, processInstancesList.size());
	}

	protected void executeSingleTask(String taskDefinitionKey, String role, String businessKey) {
		List<Task> taskList = taskService().createTaskQuery().taskDefinitionKey(taskDefinitionKey).taskAssignee(role)
				.processInstanceBusinessKey(businessKey).list();
		assertEquals(1, taskList.size());
		taskService().complete(taskList.get(0).getId());
	}

	protected void ensureVariableSet(String variableName) {
		assertEquals(1, runtimeService().createVariableInstanceQuery().variableName(variableName).list().size());
	}

	protected int ensureProcessInstanceCount(String processDefinitionKey) {
		return runtimeService().createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).list().size();
	}

	protected void ensureActiveEventSubscriptionsPresent(String processDefinitionKey, String activityId, EventType eventType,
			String eventName, List<String> expectedBusinessKeys) {

		EventSubscriptionQuery query = runtimeService().createEventSubscriptionQuery().eventName(eventName);
		if (eventType != null) {
			query.eventType(eventType.toString().toLowerCase());
		}
		List<EventSubscription> list = query.list();
		List<String> processInstanceIds = new ArrayList<String>();
		List<String> actualProcessBusinessKeysFromSubscriptions = new ArrayList<String>();
		for (EventSubscription subscription : list) {
			processInstanceIds.add(subscription.getProcessInstanceId());
			for (ProcessInstance instance : runtimeService().createProcessInstanceQuery()
					.processInstanceId(subscription.getProcessInstanceId()).list()) {
				actualProcessBusinessKeysFromSubscriptions.add(instance.getBusinessKey());
			}
			;
		}
		// business keys must match here...
		assertTrue(RailTestUtil.areListsEqual(expectedBusinessKeys, actualProcessBusinessKeysFromSubscriptions));
	}

	protected Object getProcessVariableByName(String variableName) {
		List<VariableInstance> variableInstanceList = runtimeService().createVariableInstanceQuery().variableName(variableName)
				.list();
		// name is unique in process
		assertEquals(1, variableInstanceList.size());
		return variableInstanceList.get(0).getValue();
	}

	protected List<ProcessInstance> getProcessesInstances(String processDefinitionKey) {
		return runtimeService().createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).list();
	}

	protected void debugEngineState() {

		System.out.println("---------------[ENGINE STATE]------------------");

		// runtime
		System.out.println("[executions] ---> " + runtimeService().createExecutionQuery().list().size());
		System.out.println("[incidents] ---> " + runtimeService().createIncidentQuery().list().size());
		System.out.println("[process instances] ---> " + runtimeService().createProcessInstanceQuery().list().size());
		System.out.println("[variable instances] ---> " + runtimeService().createVariableInstanceQuery().list().size());

		// task
		System.out.println("[tasks] ---> " + taskService().createTaskQuery().list().size());

		// management
		System.out.println("[jobs] ---> " + managementService().createJobQuery().list().size());

		System.out.println("-----------------------------------------------");
	}

	protected void shiftMinutes(int minutes) {
		Calendar now = Calendar.getInstance();
		now.add(Calendar.MINUTE, minutes);
		ClockUtil.setCurrentTime(now.getTime());
	}

	protected void sleep(long milliSeconds) {
		try {
			Thread.sleep(milliSeconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected void restartJobExecutor(JobExecutor jobExecutor) {

		// shut down job executor...
		if (jobExecutor.isActive()) {
			jobExecutor.shutdown();
			// wait until jobExecutor is inactive
			while (jobExecutor.isActive()) {
				sleep(1000);
			}
		}
		jobExecutor.setWaitTimeInMillis(500);
		// start up job executor...
		jobExecutor.start();
		// wait until jobExecutor is active
		while (!jobExecutor.isActive()) {
			sleep(1000);
		}
		ClockUtil.reset();
	}

	// ---

	protected enum EventType {
		MESSAGE
	}
}