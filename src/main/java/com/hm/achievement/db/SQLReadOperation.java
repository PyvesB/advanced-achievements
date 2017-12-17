package com.hm.achievement.db;

import java.sql.SQLException;

import com.hm.achievement.exception.DatabaseReadError;

/**
 * Class used to perform read operations to the database and automatically retry if a SQLException is thrown.
 * 
 * @author Pyves
 * @param <T>
 *
 */
@FunctionalInterface
public interface SQLReadOperation<T> {

	static final int MAX_ATTEMPTS = 3;

	/**
	 * Performs a single read operation on the database.
	 * 
	 * @return result of the read operation
	 * 
	 * @throws SQLException
	 */
	T performRead() throws SQLException;

	/**
	 * Calls {@code performRead} repeatedly until the read succeeds or {@code MAX_ATTEMPTS} is reached, in which case a
	 * runtime exception is thrown.
	 *
	 * @param exceptionMessage
	 * @return the result of a successful read operation
	 */
	public default T executeOperation(String exceptionMessage) {
		SQLException cause = null;
		for (int attempt = 1; attempt <= MAX_ATTEMPTS; ++attempt) {
			try {
				return performRead();
			} catch (SQLException e) {
				cause = e;
			}
		}
		throw new DatabaseReadError(exceptionMessage, cause);
	}
}
