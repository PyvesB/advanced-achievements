package com.hm.achievement.lang;

/**
 * Lang implementation for translations used in GUI classes.
 *
 * @author Rsl1122
 */
public enum GuiLang implements Lang {
	ACHIEVEMENTS_IN_CATEGORY_SINGULAR("AMOUNT achievement"),
	ACHIEVEMENTS_IN_CATEGORY_PLURAL("AMOUNT achievements"),
	COMMANDS("Other Achievements"),
	GUI_TITLE("&5&lAchievements List"),
	ACHIEVEMENT_RECEIVED("&a\u2714&f "),
	ACHIEVEMENT_NOT_RECEIVED("&4\u2718 "),
	DESCRIPTION("Description:"),
	RECEPTION("Reception date:"),
	GOAL("Goal:"),
	PROGRESS("Progress:"),
	REWARD("Reward(s):"),
	PREVIOUS_MESSAGE("&7Previous"),
	PREVIOUS_LORE(""),
	NEXT_MESSAGE("&7Next"),
	NEXT_LORE(""),
	BACK_MESSAGE("&7Back"),
	BACK_LORE(""),
	CATEGORY_NOT_UNLOCKED("You have not yet unlocked this category.");

	private final String defaultMessage;

	GuiLang(String defaultMessage) {
		this.defaultMessage = defaultMessage;
	}

	@Override
	public String toLangKey() {
		return "list-" + LangHelper.toPath(name());
	}

	@Override
	public String toLangDefault() {
		return defaultMessage;
	}
}
