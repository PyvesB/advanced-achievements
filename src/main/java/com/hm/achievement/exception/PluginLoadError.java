package com.hm.achievement.exception;

/**
 * Checked exception thrown if the plugin encounters a non recoverable error during load time.
 * 
 * @author Pyves
 *
 */
public class PluginLoadError extends Exception {

	private static final long serialVersionUID = -2223221493185030224L;

	public PluginLoadError(String message, Exception e) {
		super(message, e);
	}

	public PluginLoadError(String message) {
		super(message);
	}
}
