package de.gravitex.bpm.traindepartment.logic.businesskey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.camunda.bpm.engine.runtime.ProcessInstance;

import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogicException;
import lombok.Data;

@Data
public abstract class BusinessKeyCreator {

	protected static final Random random = new Random();

	protected static final String DIVIDER = "@";

	private static final String SUB_DIVIDER = "#";

	private String processDefinitionKey;

	private HashMap<String, Object> additionalValues;

	public BusinessKeyCreator(String aProcessDefinitionKey) {
		super();
		this.processDefinitionKey = aProcessDefinitionKey;
	}

	public String generate(HashMap<String, Object> additionalValues, String parentBusinessKey) {
		this.additionalValues = additionalValues;
		String businessKey = processDefinitionKey + DIVIDER + String.valueOf(System.currentTimeMillis()) + DIVIDER
				+ String.valueOf(random.nextInt(1000)) + DIVIDER
				+ (parentBusinessKey != null ? parentBusinessKey.replaceAll(DIVIDER, SUB_DIVIDER) : "0");
		return businessKey;
	}

	protected Object getAdditionalValue(String key) {
		return additionalValues.get(key);
	}

	public static String getDefinitionKey(String businessKey) {
		return businessKey.split(BusinessKeyCreator.DIVIDER)[0];
	}

	public static String getParentBusinessKey(String businessKey) {
		return businessKey.split(BusinessKeyCreator.DIVIDER)[3];
	}

	public static String getAdditionalKey(String businessKey) {
		return businessKey.split(BusinessKeyCreator.DIVIDER)[4];
	}

	public static ProcessInstance resolveProcessInstance(List<ProcessInstance> processInstances, String aProcessDefinitionKey,
			Object value, ProcessInstance parentInstance) {

		HashMap<String, List<ProcessInstance>> hashedInstances = new HashMap<String, List<ProcessInstance>>();
		for (ProcessInstance processInstance : processInstances) {
			String definitionKey = BusinessKeyCreator.getDefinitionKey(processInstance.getBusinessKey());
			if (hashedInstances.get(definitionKey) == null) {
				hashedInstances.put(definitionKey, new ArrayList<ProcessInstance>());
			}
			hashedInstances.get(definitionKey).add(processInstance);
		}
		List<ProcessInstance> potentialInstances = hashedInstances.get(aProcessDefinitionKey);
		List<ProcessInstance> matches = new ArrayList<ProcessInstance>();
		boolean additionalValueEquals = false;
		boolean parentInstanceEquals = false;
		for (ProcessInstance processInstance : potentialInstances) {
			String instanceParentBusinessKey = BusinessKeyCreator.getParentBusinessKey(processInstance.getBusinessKey())
					.replaceAll(BusinessKeyCreator.SUB_DIVIDER, BusinessKeyCreator.DIVIDER);
			additionalValueEquals = BusinessKeyCreator.getAdditionalKey(processInstance.getBusinessKey()).equals(value);
			parentInstanceEquals = instanceParentBusinessKey.equals(parentInstance.getBusinessKey());
			if (additionalValueEquals && parentInstanceEquals) {
				matches.add(processInstance);
			}
		}
		if (matches.size() == 0) {
			throw new RailwayStationBusinessLogicException("[aProcessDefinitionKey:" + aProcessDefinitionKey + "|value:" + value
					+ "] matching to no process instance!!");
		}
		if (matches.size() > 1) {
			throw new RailwayStationBusinessLogicException("[aProcessDefinitionKey:" + aProcessDefinitionKey + "|value:" + value
					+ "] matching to more than one process instance!!");
		}
		return matches.get(0);
	}
}