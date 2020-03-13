package de.gravitex.bpm.traindepartment.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.gravitex.bpm.traindepartment.util.RailTestUtil;
import lombok.Data;

@Data
public class Track extends RailTestEntity<Track> {

	private String trackNumber;
	
	private List<Waggon> waggons = new ArrayList<Waggon>();
	
	private boolean exitTrack;
	
	public List<String> getWaggonNumbers(boolean showWaggonDefects) {
		List<String> result = new ArrayList<String>();
		if (waggons == null) {
			return result;
		}
		for (Waggon waggon : waggons) {
			result.add(waggon.getWaggonNumber());
		}
		return result;
	}

	public void removeWaggon(String waggonNumber) {
		HashMap<String, Waggon> trackWaggons = RailTestUtil.hashWaggons(waggons);
		trackWaggons.remove(waggonNumber);
		waggons = new ArrayList<Waggon>(trackWaggons.values());
	}

	public Waggon getWaggon(String waggonNumber) {
		return RailTestUtil.hashWaggons(waggons).get(waggonNumber);
	}

	@Override
	public Track fromString(String value) {
		setTrackNumber(getPrimaryValue(value));
		if (hasSecondaryValue(value)) {
			setExitTrack(Boolean.parseBoolean((String) getSecondaryValue(value)));			
		}
		return this;
	}
}