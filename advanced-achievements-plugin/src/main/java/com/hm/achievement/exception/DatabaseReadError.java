package com.hm.achievement.exception;

import java.sql.SQLException;

/**
 * Runtime exception thrown if the plugin repeatedly fails to read information from the database.
 * 
 * @author Pyves
 *
 */
public class DatabaseReadError extends RuntimeException {

	private static final long serialVersionUID = 9076250874454568667L;

	public DatabaseReadError(String context, SQLException e) {
		super(context, e);
	}

}
