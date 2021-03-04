package com.hm.achievement.db;

/**
 * Class used to provide a cache wrapper for a database statistic.
 * 
 * @author Pyves
 *
 */
public class CachedStatistic {

	// Value of the statistic. Can only be modified by the main server thread.
	private volatile long value;
	// Indicates whether this in-memory value was written to or is about to be written to the database. Can be modified
	// concurrently by either the main server thread or the AsyncCachedRequestsSender thread.
	private volatile boolean databaseConsistent;
	// Indicates whether the player linked to this statistic has recently disconnected. Can only be modified by the main
	// server thread.
	private volatile boolean disconnection;

	public CachedStatistic(long value, boolean databaseConsistent) {
		this.value = value;
		this.databaseConsistent = databaseConsistent;
		disconnection = false;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
		databaseConsistent = false;
	}

	public boolean isDatabaseConsistent() {
		return databaseConsistent;
	}

	public void prepareDatabaseWrite() {
		databaseConsistent = true;
	}

	public boolean didPlayerDisconnect() {
		return disconnection;
	}

	public void signalPlayerDisconnection() {
		disconnection = true;
	}

	public void resetDisconnection() {
		disconnection = false;
	}
}
