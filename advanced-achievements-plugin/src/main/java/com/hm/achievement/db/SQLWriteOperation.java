package com.hm.achievement.db;

import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class used to perform write operations to the database and automatically retry if a SQLException is thrown.
 *
 * @author Pyves
 */
@FunctionalInterface
public interface SQLWriteOperation {

	int MAX_ATTEMPTS = 5;

	/**
	 * Performs a single write operation to the database.
	 *
	 * @throws SQLException
	 */
	void performWrite() throws SQLException;

	/**
	 * Performs the write operation with an Executor.
	 *
	 * @param executor
	 * @param logger
	 * @param operationMessage
	 */
	default void executeOperation(Executor executor, Logger logger, String operationMessage) {
		executor.execute(() -> attemptWrites(logger, operationMessage));
	}

	/**
	 * Calls {@code performWrite} repeatedly until the write succeeds or {@code MAX_ATTEMPTS} is reached.
	 *
	 * @param logger
	 * @param operationMessage
	 */
	default void attemptWrites(Logger logger, String operationMessage) {
		for (int attempt = 1; attempt <= MAX_ATTEMPTS; ++attempt) {
			try {
				performWrite();
				// Operation succeeded: return immediately.
				return;
			} catch (SQLException e) {
				if (attempt == MAX_ATTEMPTS) {
					// Final attempt: log error.
					logger.log(Level.SEVERE, "Database write error while " + operationMessage + ":", e);
				} else {
					// Sleep before next attempt.
					sleepOneSecond(logger);
				}
			}
		}
	}

	/**
	 * Sleeps during one second.
	 *
	 * @param logger
	 */
	default void sleepOneSecond(Logger logger) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Thead interrupted while sleeping:", e);
			Thread.currentThread().interrupt();
		}
	}
}
