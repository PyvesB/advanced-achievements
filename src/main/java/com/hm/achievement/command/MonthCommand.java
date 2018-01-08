package com.hm.achievement.command;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.lang.command.CmdLang;

import java.util.Calendar;

/**
 * Class in charge of handling the /aach month command, which displays monthly rankings.
 *
 * @author Pyves
 */
public class MonthCommand extends AbstractRankingCommand {

	public MonthCommand(AdvancedAchievements plugin) {
		super(plugin, CmdLang.MONTH_ACHIEVEMENT);
	}

	@Override
	protected long getRankingStartTime() {
		Calendar c = Calendar.getInstance();
		// Set calendar to the first day of the month.
		c.set(Calendar.DAY_OF_MONTH, 1);
		return c.getTimeInMillis();
	}
}
