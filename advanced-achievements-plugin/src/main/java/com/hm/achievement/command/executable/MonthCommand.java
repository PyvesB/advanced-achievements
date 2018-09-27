package com.hm.achievement.command.executable;

import java.util.Calendar;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.utils.SoundPlayer;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of handling the /aach month command, which displays monthly rankings.
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "month", permission = "month", minArgs = 1, maxArgs = 2)
public class MonthCommand extends AbstractRankingCommand {

	@Inject
	public MonthCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, Logger logger,
			int serverVersion, AbstractDatabaseManager databaseManager, SoundPlayer soundPlayer) {
		super(mainConfig, langConfig, pluginHeader, logger, serverVersion, CmdLang.MONTH_ACHIEVEMENT, databaseManager,
				soundPlayer);
	}

	@Override
	long getRankingStartTime() {
		Calendar c = Calendar.getInstance();
		// Set calendar to the first day of the month.
		c.set(Calendar.DAY_OF_MONTH, 1);
		return c.getTimeInMillis();
	}
}
