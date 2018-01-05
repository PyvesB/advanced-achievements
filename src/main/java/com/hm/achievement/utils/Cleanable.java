package com.hm.achievement.utils;

import java.util.UUID;

/**
 * Interface used for classes that contain player specific data structures, and that should clean up when a player
 * disconnects. This avoids Maps and Sets from growing unboundedly until the server is restarted. Implementing classes
 * cooperate with QuitListener using the Observer pattern.
 *
 * @author Pyves
 */
public interface Cleanable {

	/**
	 * Cleans data for a specific player that has disconnected recently.
	 *
	 * @param uuid
	 */
	void cleanPlayerData(UUID uuid);

}
