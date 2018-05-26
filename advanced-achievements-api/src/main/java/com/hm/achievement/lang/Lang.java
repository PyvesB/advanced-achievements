package com.hm.achievement.lang;

/**
 * Interface used to define key-value pairs in lang.yml.
 * <p>
 * Each Lang implementation should only use keys found in language file. This means that custom language implementations
 * should add the key used to the language file.
 *
 * @author Rsl1122
 */
public interface Lang {

	/**
	 * Converts to the key in the language file.
	 *
	 * @return the language configuration key
	 */
	String toLangKey();

	/**
	 * Converts to the default message that is returned, if key is not found in the configuration.
	 *
	 * @return the language configuration default value
	 */
	String toLangDefault();

}
