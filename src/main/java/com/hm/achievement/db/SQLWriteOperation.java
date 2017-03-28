package com.hm.achievement.db;

import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class used to perform write operations to the database and automatically retry if a SQLException is thrown.
 * 
 * @author Pyves
 *
 */
public abstract class SQLWriteOperation {

	private static final int NUM_OF_ATTEMPTS = 3;

	/**
	 * Performs a single write operation to the database.
	 * 
	 * @throws SQLException
	 */
	protected abstract void performWrite() throws SQLException;

	/**
	 * Performs the write operation with an Executor.
	 * 
	 * @param executor
	 * @param logger
	 * @param exceptionMessage
	 */
	protected void executeOperation(final Executor executor, final Logger logger, final String exceptionMessage) {
		executor.execute(new Runnable() {

			@Override
			public void run() {
				attemptWrites(logger, exceptionMessage);
			}

		});
	}

	/**
	 * Calls {@code performWrite} repeatedly until the write succeeds or {@code NUM_OF_ATTEMPTS} is reached.
	 *
	 * @param logger
	 * @param exceptionMessage
	 */
	protected void attemptWrites(final Logger logger, final String exceptionMessage) {
		for (int attempt = 1; attempt <= NUM_OF_ATTEMPTS; ++attempt) {
			try {
				performWrite();
				// Operation succeeded: return immediately.
				return;
			} catch (SQLException e) {
				if (attempt == NUM_OF_ATTEMPTS) {
					// Final attempt: log error.
					logger.log(Level.SEVERE, exceptionMessage, e);

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
	private void sleepOneSecond(Logger logger) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Thead interrupted while sleeping.", e);
			Thread.currentThread().interrupt();
		}
	}
}
