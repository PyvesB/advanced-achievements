package com.hm.achievement.command;

import java.util.Calendar;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.hm.achievement.db.AbstractSQLDatabaseManager;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of handling the /aach week command, which displays weekly rankings.
 *
 * @author Pyves
 */
@Singleton
public class WeekCommand extends AbstractRankingCommand {

	@Inject
	public WeekCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, ReloadCommand reloadCommand,
			Logger logger, int serverVersion, AbstractSQLDatabaseManager sqlDatabaseManager) {
		super(mainConfig, langConfig, pluginHeader, reloadCommand, logger, serverVersion, CmdLang.WEEK_ACHIEVEMENT,
				sqlDatabaseManager);
	}

	@Override
	long getRankingStartTime() {
		Calendar c = Calendar.getInstance();
		// Set calendar to the first day of the week.
		c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
		return c.getTimeInMillis();
	}
}
