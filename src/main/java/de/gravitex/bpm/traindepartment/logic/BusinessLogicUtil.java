package de.gravitex.bpm.traindepartment.logic;

import java.util.List;

public class BusinessLogicUtil {

	public static String formatStringList(List<String> strings) {
		String result = "";
		for (String s : strings) {
			result += "[" + s + "]";
		}
		return result;
	}
}