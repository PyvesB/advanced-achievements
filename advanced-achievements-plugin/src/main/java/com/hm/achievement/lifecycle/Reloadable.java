package com.hm.achievement.lifecycle;

import com.hm.achievement.exception.PluginLoadError;

/**
 * Interface used for classes that rely on configuration files, and that should refresh their state when the plugin is
 * reloaded. Implementing classes cooperate with ReloadCommand using the Observer pattern.
 *
 * @author Pyves
 */
public interface Reloadable {

	/**
	 * Extracts and parses configuration parameters.
	 * 
	 * @throws PluginLoadError
	 */
	void extractConfigurationParameters() throws PluginLoadError;

}
