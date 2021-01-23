package com.hm.achievement.command.executable;

import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.hm.achievement.category.CommandAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.domain.Achievement;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent;
import com.hm.achievement.utils.StringHelper;

/**
 * Class in charge of handling the /aach give command, which gives an achievement from the Commands category.
 * 
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "give", permission = "give", minArgs = 3, maxArgs = 3)
public class GiveCommand extends AbstractParsableCommand {

	private final CacheManager cacheManager;
	private final AchievementMap achievementMap;

	private boolean configMultiCommand;
	private String langAchievementAlreadyReceived;
	private String langAchievementGiven;
	private String langAchievementNotFound;
	private String langAchievementNoPermission;

	@Inject
	public GiveCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig,
			StringBuilder pluginHeader, CacheManager cacheManager, AchievementMap achievementMap) {
		super(mainConfig, langConfig, pluginHeader);
		this.cacheManager = cacheManager;
		this.achievementMap = achievementMap;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configMultiCommand = mainConfig.getBoolean("MultiCommand");

		langAchievementAlreadyReceived = pluginHeader + langConfig.getString("achievement-already-received");
		langAchievementGiven = pluginHeader + langConfig.getString("achievement-given");
		langAchievementNotFound = pluginHeader + langConfig.getString("achievement-not-found");
		langAchievementNoPermission = pluginHeader + langConfig.getString("achievement-no-permission");
	}

	@Override
	void onExecuteForPlayer(CommandSender sender, String[] args, Player player) {
		Optional<Achievement> achievement = achievementMap.getForCategory(CommandAchievements.COMMANDS).stream()
				.filter(ach -> ach.getSubcategory().equals(args[1]))
				.findAny();

		if (achievement.isPresent()) {
			// Check whether player has already received achievement and cannot receive it again.
			if (!configMultiCommand
					&& cacheManager.hasPlayerAchievement(player.getUniqueId(), achievement.get().getName())) {
				sender.sendMessage(StringUtils.replaceOnce(langAchievementAlreadyReceived, "PLAYER", args[2]));
				return;
			} else if (!player.hasPermission("achievement." + achievement.get().getName())) {
				sender.sendMessage(StringUtils.replaceOnce(langAchievementNoPermission, "PLAYER", args[2]));
				return;
			}

			Bukkit.getPluginManager().callEvent(new PlayerAdvancedAchievementEvent(player, achievement.get()));

			sender.sendMessage(langAchievementGiven);
		} else {
			Set<String> commandKeys = achievementMap.getSubcategoriesForCategory(CommandAchievements.COMMANDS);
			sender.sendMessage(StringUtils.replaceOnce(langAchievementNotFound, "CLOSEST_MATCH",
					StringHelper.getClosestMatch(args[1], commandKeys)));
		}
	}
}
