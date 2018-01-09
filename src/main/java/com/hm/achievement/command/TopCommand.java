package com.hm.achievement.command;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.lang.command.CmdLang;

/**
 * Class in charge of handling the /aach top command, which displays global rankings.
 *
 * @author Pyves
 */
public class TopCommand extends AbstractRankingCommand {

	public TopCommand(AdvancedAchievements plugin) {
		super(plugin, CmdLang.TOP_ACHIEVEMENT);
	}

	@Override
	protected long getRankingStartTime() {
		// All time ranking, no time start.
		return 0L;
	}
}
