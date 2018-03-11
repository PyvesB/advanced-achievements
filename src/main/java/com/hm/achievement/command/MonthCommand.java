package com.hm.achievement.command;

import java.util.Calendar;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of handling the /aach month command, which displays monthly rankings.
 *
 * @author Pyves
 */
@Singleton
public class MonthCommand extends AbstractRankingCommand {

	@Inject
	public MonthCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, ReloadCommand reloadCommand,
			Logger logger, int serverVersion, AbstractDatabaseManager sqlDatabaseManager) {
		super(mainConfig, langConfig, pluginHeader, reloadCommand, logger, serverVersion, CmdLang.MONTH_ACHIEVEMENT,
				sqlDatabaseManager);
	}

	@Override
	long getRankingStartTime() {
		Calendar c = Calendar.getInstance();
		// Set calendar to the first day of the month.
		c.set(Calendar.DAY_OF_MONTH, 1);
		return c.getTimeInMillis();
	}
}
