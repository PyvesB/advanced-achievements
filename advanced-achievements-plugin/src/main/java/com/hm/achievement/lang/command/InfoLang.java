package com.hm.achievement.lang.command;

import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.LangHelper;

/**
 * Lang implementation for translations used in InfoCommand.
 *
 * @author Rsl1122
 */
public enum InfoLang implements Lang {

	DESCRIPTION("Description:"),
	DESCRIPTION_DETAILS("Advanced Achievements enables unique and challenging achievements. "
			+ "Try to collect as many as you can, earn rewards, climb the rankings and receive RP books!"),
	VERSION("Version:"),
	AUTHOR("Author:"),
	WEBSITE("Website:"),
	VAULT("Vault integration:"),
	PETMASTER("Pet Master integration:"),
	BTLP("BungeeTabListPlus integration:"),
	ESSENTIALS("Essentials integration:"),
	PLACEHOLDERAPI("PlaceholderAPI integration:"),
	DATABASE("Database type:");

	private final String defaultMessage;

	InfoLang(String defaultMessage) {
		this.defaultMessage = defaultMessage;
	}

	@Override
	public String toLangKey() {
		return "version-command-" + LangHelper.toPath(name());
	}

	@Override
	public String toLangDefault() {
		return defaultMessage;
	}
}
