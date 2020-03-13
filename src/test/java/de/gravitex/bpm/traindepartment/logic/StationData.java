package de.gravitex.bpm.traindepartment.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.gravitex.bpm.traindepartment.entity.DepartmentOrder;
import de.gravitex.bpm.traindepartment.entity.Track;
import de.gravitex.bpm.traindepartment.entity.Waggon;
import de.gravitex.bpm.traindepartment.util.RailTestUtil;
import lombok.Data;

@Data
public class StationData {

	// key --> business key (also from referring business process)
	private HashMap<String, DepartmentOrder> departmentOrders = new HashMap<String, DepartmentOrder>();

	private List<Track> tracks = new ArrayList<Track>();

	public boolean allWaggonsPresent(List<String> waggonNumbers) {
		HashMap<String, Waggon> allWaggons = RailTestUtil.hashWaggons(getAllWaggons());
		for (String waggonNumber : waggonNumbers) {
			if (allWaggons.get(waggonNumber) == null) {
				return false;
			}
		}
		return true;
	}

	public List<Waggon> getAllWaggons() {
		List<Waggon> result = new ArrayList<Waggon>();
		for (Track track : tracks) {
			if (track.getWaggons() != null) {
				result.addAll(track.getWaggons());
			}
		}
		return result;
	}

	public void removeWaggon(String waggonNumber) {
		for (Track track : tracks) {
			track.removeWaggon(waggonNumber);
		}
	}

	private Waggon findWaggon(String waggonNumber) {
		Waggon waggon = null;
		for (Track track : tracks) {
			waggon = track.getWaggon(waggonNumber);
			if (waggon != null) {
				return waggon;
			}
		}
		return null;
	}

	public void addTrack(String trackNumber) {
		tracks.add(new Track().fromString(trackNumber));
	}

	public void addWaggons(String trackNumber, String[] waggonNumbers) {
		List<Waggon> waggons = new ArrayList<Waggon>();
		for (String waggonNumber : waggonNumbers) {
			waggons.add(new Waggon().fromString(waggonNumber));
		}
		Track findTrack = findTrack(trackNumber);
		if (findTrack == null) {
			throw new RailwayStationBusinessLogicException("track " + trackNumber + " not present!!");
		}
		findTrack.setWaggons(waggons);
	}

	public Track findTrack(String trackNumber) {
		HashMap<String, Track> hashedTracks = RailTestUtil.hashTracks(tracks);
		return hashedTracks.get(trackNumber);
	}

	public boolean isWaggonCritical(String waggonNumber) {
		return findWaggon(waggonNumber).isDefect();
	}

	public void reset() {
		tracks = new ArrayList<Track>();
		departmentOrders = new HashMap<String, DepartmentOrder>();
	}

	public void print(boolean showWaggonDefects) {
		System.out.println("---------------------------------------------");
		if (departmentOrders != null) {
			System.out.println(departmentOrders.size() + " department orders:");
			for (DepartmentOrder departmentOrder : departmentOrders.values()) {
				System.out.println(departmentOrder + " (" + departmentOrder.getDepartmentOrderState() + ")");
			}
		} else {
			System.out.println("NO department orders.");
		}
		System.out.println("---tracks an waggons:");
		for (Track track : tracks) {
			System.out.println("track[" + track.getTrackNumber() + "] ---> "
					+ BusinessLogicUtil.formatStringList(track.getWaggonNumbers(showWaggonDefects)));
		}
		System.out.println("---------------------------------------------");
	}

	public void createDepartmentOrder(String businessKey) {
		departmentOrders.put(businessKey, new DepartmentOrder());
	}

	public void addWaggonsToTrack(String trackNumber, List<String> waggonNumbers) {
		List<Waggon> newWaggons = new ArrayList<Waggon>();
		for (String waggonNumber : waggonNumbers) {
			newWaggons.add(new Waggon().fromString(waggonNumber));
		}
		Track track = findTrack(trackNumber);
		track.getWaggons().addAll(newWaggons);
	}

	public boolean checkTrackWaggons(String trackNumber, String... waggonNumbers) {
		return RailTestUtil.areListsEqual(findTrack(trackNumber).getWaggonNumbers(false), Arrays.asList(waggonNumbers));
	}
}