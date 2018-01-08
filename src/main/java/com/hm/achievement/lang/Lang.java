package com.hm.achievement.lang;

import com.hm.achievement.AdvancedAchievements;
import org.apache.commons.lang3.StringUtils;

public interface Lang {

	String getPath();

	String getDefaultMessage();

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
