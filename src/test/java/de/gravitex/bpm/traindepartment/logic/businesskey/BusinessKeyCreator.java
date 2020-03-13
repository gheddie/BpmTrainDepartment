package de.gravitex.bpm.traindepartment.logic.businesskey;

import java.util.HashMap;
import java.util.Random;

import lombok.Data;

@Data
public abstract class BusinessKeyCreator {
	
	private static final Random random = new Random();

	private String processDefinitionKey;

	private HashMap<String, Object> additionalValues;
	
	public BusinessKeyCreator(String aProcessDefinitionKey) {
		super();
		this.processDefinitionKey = aProcessDefinitionKey;
	}

	public String generate(HashMap<String, Object> additionalValues) {
		this.additionalValues = additionalValues;
		return processDefinitionKey + "_" + String.valueOf(System.currentTimeMillis()) + "_" + String.valueOf(random.nextInt(1000));
	}
	
	protected Object getAdditionalValue(String key) {
		return additionalValues.get(key);
	}
}