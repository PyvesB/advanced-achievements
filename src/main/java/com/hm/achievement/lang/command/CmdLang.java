package com.hm.achievement.lang.command;

import com.hm.achievement.lang.Lang;

public enum CmdLang implements Lang {
	NO_PERMISSIONS("You do not have the permission to do this."),
	PLAYER_OFFLINE("The player PLAYER is offline!"),
	PLAYER_RANK("Current rank:"),
	NOT_RANKED("You are currently not ranked for this period."),
	// AddCommand
	ERROR_VALUE("The value VALUE must to be an integer!"),
	STATISTIC_INCREASED("Statistic ACH increased by AMOUNT for PLAYER!"),
	CATEGORY_DOES_NOT_EXIST("The category CAT does not exist."),
	// BookCommand
	BOOK_DELAY("You must wait TIME seconds between each book reception!"),
	BOOK_NOT_RECEIVED("You have not yet received any achievements."),
	BOOK_DATE("Book created on DATE."),
	BOOK_NAME("Achievements Book"),
	BOOK_RECEIVED("You received your achievements book!"),
	// CheckCommand
	CHECK_ACHIEVEMENT_TRUE("PLAYER has received the achievement ACH!"),
	CHECK_ACHIEVEMENTS_FALSE("PLAYER has not received the achievement ACH!"),
	// DeleteCommand
	DELETE_ACHIEVEMENTS("The achievement ACH was deleted from PLAYER."),
	// GenerateCommand
	ADVANCEMENTS_GENERATED("Advancements were successfully generated."),
	MINECRAFT_NOT_SUPPORTED("Advancements not supported in your Minecraft version. Please update to 1.12+."),
	// GiveCommand
	ACHIEVEMENT_ALREADY_RECEIVED("The player PLAYER has already received this achievement!"),
	ACHIEVEMENT_GIVEN("Achievement given!"),
	ACHIEVEMENT_NOT_FOUND("The specified achievement was not found in Commands category."),
	// HelpCommand, check out HelpLang for more.
	AACH_TIP("&lHINT &8You can &7&n&ohover &8or &7&n&oclick &8on the commands!"),
	// ReloadCommand
	SERVER_RESTART_RELOAD("DisabledCategories list was modified. " +
			"Server must be fully reloaded or restarted for your changes to take effect."),
	CONFIGURATION_RELOAD_FAILED("Errors while reloading configuration. Please view logs for more details."),
	CONFIGURATION_SUCCESSFULLY_RELOADED("Configuration successfully reloaded."),
	// ResetCommand
	RESET_SUCCESSFUL(" statistics were cleared for PLAYER."),
	// StatsCommand
	NUMBER_ACHIEVEMENTS("Achievements received:"),
	// ToggleCommand
	TOGGLE_DISPLAYED("You will now be notified when other players get achievements."),
	TOGGLE_HIDDEN("You will no longer be notified when other players get achievements.");

	private final String defaultMessage;

	CmdLang(String defaultMessage) {
		this.defaultMessage = defaultMessage;
	}

	@Override
	public String toLangKey() {
		return Lang.toPath(name());
	}

	@Override
	public String toLangDefault() {
		return defaultMessage;
	}
}
