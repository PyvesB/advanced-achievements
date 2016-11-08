package com.hm.achievement.command;

import java.util.Calendar;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class in charge of handling the /aach week command, which displays weekly rankings.
 * 
 * @author Pyves
 */
public class WeekCommand extends AbstractRankingCommand {

	public WeekCommand(AdvancedAchievements plugin) {

		super(plugin);
		languageHeaderKey = "week-achievement";
		defaultHeaderMessage = "Weekly achievement rankings:";
	}

	@Override
	protected long getRankingStartTime() {

		Calendar c = Calendar.getInstance();
		// Set calendar to the first day of the week.
		c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
		return c.getTimeInMillis();
	}
}
