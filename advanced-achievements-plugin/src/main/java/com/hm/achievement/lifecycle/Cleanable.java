package com.hm.achievement.lifecycle;

/**
 * Interface used for classes that contain player specific data structures, and that should be cleaned up once a player
 * has disconnected or has reached the end of a cooldown period. This avoids Maps and Sets from growing unboundedly
 * until the server is restarted.
 *
 * @author Pyves
 */
public interface Cleanable {

	void cleanPlayerData();

}
