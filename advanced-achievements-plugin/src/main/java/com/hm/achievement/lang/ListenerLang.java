package com.hm.achievement.lang;

/**
 * Lang implementation for translations used in Listeners.
 *
 * @author Rsl1122
 */
public enum ListenerLang implements Lang {

	STATISTIC_COOLDOWN("Achievements cooldown, wait TIME seconds before this action counts again."),
	COMMAND_REWARD("Reward command carried out!"),
	ACHIEVEMENT_RECEIVED("PLAYER received the achievement:"),
	ITEM_REWARD_RECEIVED("You received an item reward:"),
	MONEY_REWARD_RECEIVED("You received: AMOUNT!"),
	EXPERIENCE_REWARD_RECEIVED("You received: AMOUNT experience!"),
	INCREASE_MAX_HEALTH_REWARD_RECEIVED("Your max health has increased by AMOUNT!"),
	INCREASE_MAX_OXYGEN_REWARD_RECEIVED("Your max oxygen has increased by AMOUNT!"),
	ACHIEVEMENT_NEW("New Achievement:"),
	CUSTOM_COMMAND_REWARD("You received your reward: MESSAGE"),
	ALL_ACHIEVEMENTS_RECEIVED("Congratulations, you have received all the achievements!");

	private final String defaultMessage;

	ListenerLang(String defaultMessage) {
		this.defaultMessage = defaultMessage;
	}

	@Override
	public String toLangKey() {
		return LangHelper.toPath(name());
	}

	@Override
	public String toLangDefault() {
		return defaultMessage;
	}
}
