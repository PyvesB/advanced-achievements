package com.hm.achievement.lifecycle;

import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

/**
 * Interface used for classes that contain player specific data structures, and that should clean up when a player
 * disconnects. This avoids Maps and Sets from growing unboundedly until the server is restarted. Implementing classes
 * cooperate with QuitListener using the Observer pattern.
 *
 * @author Pyves
 */
public interface Cleanable extends Observer {

	@Override
	default void update(Observable o, Object arg) {
		cleanPlayerData((UUID) arg);
	}

	/**
	 * Cleans data for a specific player that has disconnected recently.
	 *
	 * @param uuid
	 */
	void cleanPlayerData(UUID uuid);

}
