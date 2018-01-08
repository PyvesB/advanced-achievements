package com.hm.achievement.lang;

import com.hm.achievement.AdvancedAchievements;
import org.apache.commons.lang3.StringUtils;

public interface Lang {

	/**
	 * Converts to the key in the language file used in the list command.
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

	static String get(Lang lang, AdvancedAchievements plugin) {
		return plugin.getPluginLang().getMsg(lang);
	}

	static String getWithChatHeader(Lang lang, AdvancedAchievements plugin) {
		return plugin.getChatHeader() + get(lang, plugin);
	}

	static String getReplacedOnce(Lang lang, String replace, String with, AdvancedAchievements plugin) {
		return StringUtils.replaceOnce(get(lang, plugin), replace, with);
	}

	static String getEachReplaced(Lang lang, AdvancedAchievements plugin, String[] replace, String[] with) {
		return StringUtils.replaceEach(get(lang, plugin), replace, with);
	}

	static String toPath(String enumName) {
		return StringUtils.replace(enumName.toLowerCase(), "_", "-");
	}
}
