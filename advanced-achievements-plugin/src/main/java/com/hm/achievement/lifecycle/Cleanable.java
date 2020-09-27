package com.hm.achievement.lifecycle;

import java.util.UUID;

/**
 * Interface used for classes that contain player specific data structures, and that should clean up when a player
 * disconnects (QuitListener). This avoids Maps and Sets from growing unboundedly until the server is restarted.
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
