package de.gravitex.bpm.traindepartment.logic;

import java.io.Serializable;

import lombok.Data;

@Data
public class DollesDing implements Serializable {

	private static final long serialVersionUID = -6510441131732584389L;

	public Boolean standardOrder() {
		return new Boolean(false);
	}
}