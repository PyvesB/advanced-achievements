package com.hm.achievement.lang;

import com.hm.achievement.AdvancedAchievements;

public interface Lang {

	String getPath();

	String getDefaultMessage();
	
	static String get(Lang lang, AdvancedAchievements plugin) {
		return plugin.getPluginLang().getMsg(lang);
	}
}
