package com.hm.achievement.command;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.lang.command.CmdLang;

import java.util.Calendar;

/**
 * Class in charge of handling the /aach week command, which displays weekly rankings.
 *
 * @author Pyves
 */
public class WeekCommand extends AbstractRankingCommand {

	public WeekCommand(AdvancedAchievements plugin) {
		super(plugin, CmdLang.WEEK_ACHIEVEMENT);
	}

	@Override
	protected long getRankingStartTime() {
		Calendar c = Calendar.getInstance();
		// Set calendar to the first day of the week.
		c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
		return c.getTimeInMillis();
	}
}
