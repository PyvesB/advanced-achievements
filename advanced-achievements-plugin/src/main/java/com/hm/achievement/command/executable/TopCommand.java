package com.hm.achievement.command.executable;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.utils.SoundPlayer;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of handling the /aach top command, which displays global rankings.
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "top", permission = "top", minArgs = 1, maxArgs = 2)
public class TopCommand extends AbstractRankingCommand {

	@Inject
	public TopCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, Logger logger,
			int serverVersion, AbstractDatabaseManager databaseManager, SoundPlayer soundPlayer) {
		super(mainConfig, langConfig, pluginHeader, logger, serverVersion, CmdLang.TOP_ACHIEVEMENT, databaseManager,
				soundPlayer);
	}

	@Override
	long getRankingStartTime() {
		// All time ranking, no time start.
		return 0L;
	}
}
