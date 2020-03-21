package de.gravitex.bpm.traindepartment.logic.businesskey;

import java.util.HashMap;

public class RepairFacilityBusinessKeyCreator extends BusinessKeyCreator {
	
	public static final String AV_WAGGON_NUMBER = "AV_WAGGON_NUMBER";

	public RepairFacilityBusinessKeyCreator(String aProcessDefinitionKey) {
		super(aProcessDefinitionKey);
	}
	
	@Override
	public String generate(HashMap<String, Object> additionalValues, String parentBusinessKey) {
		return super.generate(additionalValues, parentBusinessKey) + DIVIDER + getAdditionalValue(AV_WAGGON_NUMBER);
	}
}