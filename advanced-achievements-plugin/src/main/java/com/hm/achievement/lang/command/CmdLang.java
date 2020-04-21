package com.hm.achievement.lang.command;

import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.LangHelper;

/**
 * Lang implementation for translations used in Command classes.
 *
 * @author Rsl1122
 */
public enum CmdLang implements Lang {

	NO_PERMISSIONS("You do not have the permission to do this."),
	INVALID_COMMAND("Invalid command. Please type /aach to display the command help."),
	PLAYER_OFFLINE("The player PLAYER is offline!"),
	NOT_A_PLAYER("You cannot give achievements to ENTITY, it is not a player!"),
	PLAYER_RANK("Current rank:"),
	NOT_RANKED("You are currently not ranked for this period."),
	// AddCommand
	ERROR_VALUE("The value VALUE must to be an integer!"),
	STATISTIC_INCREASED("Statistic ACH increased by AMOUNT for PLAYER!"),
	CATEGORY_DOES_NOT_EXIST("The category and/or sub-category CAT does not exist. Did you mean CLOSEST_MATCH?"),
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
	DELETE_ALL_ACHIEVEMENTS("All achievements were deleted from PLAYER."),
	// GenerateCommand
	ADVANCEMENTS_GENERATED("Advancements were successfully generated."),
	MINECRAFT_NOT_SUPPORTED("Advancements not supported in your Minecraft version. Please update to 1.12+."),
	// GiveCommand
	ACHIEVEMENT_ALREADY_RECEIVED("The player PLAYER has already received this achievement!"),
	ACHIEVEMENT_GIVEN("Achievement given!"),
	ACHIEVEMENT_NOT_FOUND("The specified achievement was not found in Commands category. Did you mean CLOSEST_MATCH?"),
	// HelpCommand, check out HelpLang for more.
	AACH_TIP("&lHINT &8You can &7&n&ohover &8or &7&n&oclick &8on the commands!"),
	CONFIGURATION_RELOAD_FAILED("Errors while reloading configuration. Please view logs for more details."),
	CONFIGURATION_SUCCESSFULLY_RELOADED("Configuration successfully reloaded."),
	// ResetCommand
	RESET_SUCCESSFUL("CAT statistics were cleared for PLAYER."),
	RESET_ALL_SUCCESSFUL("All statistics were cleared for PLAYER."),
	// StatsCommand
	NUMBER_ACHIEVEMENTS("Achievements received:"),
	// ToggleCommand
	TOGGLE_DISPLAYED("You will now be notified when other players get achievements."),
	TOGGLE_HIDDEN("You will no longer be notified when other players get achievements."),
	// AbstractRankingCommand
	WEEK_ACHIEVEMENT("Weekly achievement rankings:"),
	MONTH_ACHIEVEMENT("Monthly achievement rankings:"),
	TOP_ACHIEVEMENT("Top achievement owners:"),
	PAGINATION_HEADER("&7> &5Page PAGE/MAX"),
	PAGINATION_FOOTER("&7>"),
	// InspectCommand
	ACHIEVEMENT_NOT_RECOGNIZED("Achievement called 'NAME' was not recognized. Did you mean CLOSEST_MATCH?");

	private final String defaultMessage;

	CmdLang(String defaultMessage) {
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
