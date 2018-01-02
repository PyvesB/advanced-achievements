package com.hm.achievement.utils;

/**
 * Interface used for classes that rely on configuration files, and that should refresh their state when the plugin is
 * reloaded. Implementing classes cooperate with ReloadCommand using the Observer pattern.
 * 
 * @author Pyves
 */
public interface Reloadable {

	/**
	 * Extracts and parses configuration parameters.
	 */
    void extractConfigurationParameters();

}
