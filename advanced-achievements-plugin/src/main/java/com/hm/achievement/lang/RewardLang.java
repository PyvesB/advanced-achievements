package com.hm.achievement.lang;

/**
 * Lang implementation for translations used in RewardParser.
 *
 * @author Rsl1122
 */
public enum RewardLang implements Lang {

	MONEY("receive AMOUNT"),
	ITEM("receive AMOUNT ITEM"),
	COMMAND("other"),
	EXPERIENCE("receive AMOUNT experience"),
	INCREASE_MAX_HEALTH("increase max health by AMOUNT"),
	INCREASE_MAX_OXYGEN("increase max oxygen by AMOUNT");

	private final String defaultMessage;

	RewardLang(String defaultMessage) {
		this.defaultMessage = defaultMessage;
	}

	@Override
	public String toLangKey() {
		return "list-reward-" + LangHelper.toPath(name());
	}

	@Override
	public String toLangDefault() {
		return defaultMessage;
	}
}
