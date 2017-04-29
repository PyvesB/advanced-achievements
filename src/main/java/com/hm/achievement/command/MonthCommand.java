package com.hm.achievement.command;

import java.util.Calendar;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class in charge of handling the /aach month command, which displays monthly rankings.
 * 
 * @author Pyves
 */
public class MonthCommand extends AbstractRankingCommand {

	public MonthCommand(AdvancedAchievements plugin) {
		super(plugin, "month-achievement", "Monthly achievement rankings:");
	}

	@Override
	protected long getRankingStartTime() {
		Calendar c = Calendar.getInstance();
		// Set calendar to the first day of the month.
		c.set(Calendar.DAY_OF_MONTH, 1);
		return c.getTimeInMillis();
	}
}
