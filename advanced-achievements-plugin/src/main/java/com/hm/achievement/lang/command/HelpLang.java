package com.hm.achievement.lang.command;

import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.LangHelper;

/**
 * Lang implementation for translations used in HelpCommand.
 * 
 * @author Rsl1122
 * @see HelpLang.Hover
 */
public enum HelpLang implements Lang {

	LIST("Display received and missing achievements."),
	TOP("Display personal and global rankings."),
	INFO("Display information about the plugin."),
	BOOK("Receive your achievements book."),
	WEEK("Display weekly rankings."),
	STATS("Display amount of received achievements."),
	MONTH("Display monthly rankings."),
	TOGGLE("Toggle achievements of other players."),
	RELOAD("Reload the plugin's configuration."),
	GENERATE("Generate advancements."),
	GIVE("Give achievement ACH to &7NAME."),
	ADD("Increase a statistic."),
	RESET("Reset statistic for category CAT."),
	CHECK("Check if NAME has ACH."),
	DELETE("Delete ACH from NAME."),
	INSPECT("Inspect recipients of ACH.");

	private final String defaultMessage;

	HelpLang(String defaultMessage) {
		this.defaultMessage = defaultMessage;
	}

	@Override
	public String toLangKey() {
		return "aach-command-" + LangHelper.toPath(name());
	}

	@Override
	public String toLangDefault() {
		return defaultMessage;
	}

	/**
	 * Lang implementation used for hover message translations used in HelpCommand.
	 */
	public enum Hover implements Lang {

		LIST("Fancy GUI to get an overview of all achievements and your progress!"),
		TOP("Who are the server's leaders and how do you compare to them?"),
		INFO("Some extra info about the plugin and its awesome author!"),
		BOOK("RP items you can collect and exchange with others! Time-based listing."),
		WEEK("Best achievement hunters since the start of the week!"),
		STATS("Progress bar. Gotta catch 'em all!"),
		MONTH("Best achievement hunters since the start of the month!"),
		TOGGLE("Your choice is saved until next server restart!"),
		RELOAD("Reload most settings in config.yml and lang.yml files."),
		GENERATE("Potentially slow command; use with care!"),
		GIVE("Player must be online; only Commands achievements can be used."),
		ADD("Player must be online; mainly used for Customs achievements."),
		RESET("Player must be online; example: reset Places.stone DarkPyves"),
		CHECK("Use the Name parameter specified in the config."),
		DELETE("Player must be online; does not reset any associated statistics."),
		INSPECT("Lists most recent recipients of an achievement, max 1000.");

		private final String defaultMessage;

		Hover(String defaultMessage) {
			this.defaultMessage = defaultMessage;
		}

		@Override
		public String toLangKey() {
			return "aach-command-" + LangHelper.toPath(name()) + "-hover";
		}

		@Override
		public String toLangDefault() {
			return defaultMessage;
		}
	}
}
