package net.erickelly.scorekeeper;

import java.util.Map;
import java.util.TreeMap;

public class PlayerManager {
	Map<Integer, Player> players = new TreeMap<Integer, Player>();
	
	private Integer getNextId() {
		int lowestId = 0;
		for (Integer i : players.keySet()) {
			if (lowestId == i) {
				lowestId++;
			} else {
				break;
			}
		}
		return lowestId;
	}

	/**
	 * Add a player
	 * @param name Name of the new player
	 * @return Id of the newly created player
	 */
	private int addPlayer(String name) {
		Integer id = getNextId();
		players.put(id, new Player(id, name));
		return id;
	}
	
	/**
	 * Add a player
	 * @return Id of the newly created player
	 */
	public int addPlayer() {
		String name = "Player " + players.size();
		return addPlayer(name);
	}
	
	/**
	 * Delete the player by id
	 * @param i
	 */
	public void deletePlayer(int i) {
		
	}
	
	public static PlayerManager getInstance() {
		if (mInstance == null) {
			mInstance = new PlayerManager();
		}
		return mInstance;
	}
	private PlayerManager() {}
	private static PlayerManager mInstance;
}
