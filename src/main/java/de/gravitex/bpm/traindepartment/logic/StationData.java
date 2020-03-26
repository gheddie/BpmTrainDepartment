package de.gravitex.bpm.traindepartment.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.gravitex.bpm.traindepartment.entity.DepartingOrder;
import de.gravitex.bpm.traindepartment.entity.Track;
import de.gravitex.bpm.traindepartment.entity.Waggon;
import de.gravitex.bpm.traindepartment.util.RailUtil;
import lombok.Data;

@Data
public class StationData {

	// key --> business key (also from referring business process)
	private HashMap<String, DepartingOrder> departingOrders = new HashMap<String, DepartingOrder>();

	private List<Track> tracks = new ArrayList<Track>();

	public boolean allWaggonsPresent(List<String> waggonNumbers) {
		HashMap<String, Waggon> allWaggons = RailUtil.hashWaggons(getAllWaggons());
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
		HashMap<String, Track> hashedTracks = RailUtil.hashTracks(tracks);
		return hashedTracks.get(trackNumber);
	}

	public boolean isWaggonCritical(String waggonNumber) {
		return findWaggon(waggonNumber).isDefect();
	}

	public void reset() {
		tracks = new ArrayList<Track>();
		departingOrders = new HashMap<String, DepartingOrder>();
	}

	public void print(String header, boolean showWaggonDefects) {
		
		System.out.println("---------------------------------------------");
		System.out.println("--------- " + header);
		System.out.println("---------------------------------------------");
		
		if (departingOrders != null) {
			System.out.println(departingOrders.size() + " department orders:");
			for (DepartingOrder departingOrder : departingOrders.values()) {
				System.out.println(departingOrder + " (" + departingOrder.getDepartmentOrderState() + ")");
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

	public DepartingOrder createDepartmentOrder(String businessKey) {
		DepartingOrder departingOrder = new DepartingOrder();
		departingOrders.put(businessKey, departingOrder);
		return departingOrder;
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
		return RailUtil.areListsEqual(findTrack(trackNumber).getWaggonNumbers(false), Arrays.asList(waggonNumbers));
	}

	public List<Waggon> getTrackWaggons(String trackNumber) {
		return findTrack(trackNumber).getWaggons();
	}

	public List<String> getAllWaggonNumbers() {
		List<String> result = new ArrayList<String>();
		for (Waggon waggon : getAllWaggons()) {
			result.add(waggon.getWaggonNumber());
		}
		return result;
	}

	public DepartingOrder getDepartingOrder(String businessKey) {
		return departingOrders.get(businessKey);
	}

	public boolean isExitTrack(String trackNumber) {
		return findTrack(trackNumber).isExitTrack();
	}
}