package com.hm.achievement.command;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class in charge of handling the /aach top command, which displays global rankings.
 * 
 * @author Pyves
 */
public class TopCommand extends AbstractRankingCommand {

	public TopCommand(AdvancedAchievements plugin) {
		super(plugin, "top-achievement", "Top achievement owners:");
	}

	@Override
	protected long getRankingStartTime() {
		// All time ranking, no time start.
		return 0L;
	}
}
