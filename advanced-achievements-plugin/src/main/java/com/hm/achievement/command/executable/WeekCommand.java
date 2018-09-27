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
 * Class in charge of handling the /aach week command, which displays weekly rankings.
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "week", permission = "week", minArgs = 1, maxArgs = 2)
public class WeekCommand extends AbstractRankingCommand {

	@Inject
	public WeekCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, Logger logger,
			int serverVersion, AbstractDatabaseManager databaseManager, SoundPlayer soundPlayer) {
		super(mainConfig, langConfig, pluginHeader, logger, serverVersion, CmdLang.WEEK_ACHIEVEMENT, databaseManager,
				soundPlayer);
	}

	@Override
	long getRankingStartTime() {
		Calendar c = Calendar.getInstance();
		// Set calendar to the first day of the week.
		c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
		return c.getTimeInMillis();
	}
}
