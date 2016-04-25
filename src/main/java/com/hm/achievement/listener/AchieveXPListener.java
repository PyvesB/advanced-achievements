package com.hm.achievement.listener;

import java.util.HashSet;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.DatabasePools;

public class AchieveXPListener implements Listener {

	private AdvancedAchievements plugin;

	// Lists of achievements extracted from configuration.
	private int[] achievementsMaxLevel;

	// Sets corresponding to whether a player has obtained a specific
	// level achievement.
	// Used as pseudo-caching system to reduce load on database.
	private HashSet<?>[] playerAchievements;

	public AchieveXPListener(AdvancedAchievements plugin) {

		this.plugin = plugin;

		extractAchievementsFromConfig(plugin);

	}

	public void extractAchievementsFromConfig(AdvancedAchievements plugin) {

		achievementsMaxLevel = new int[plugin.getPluginConfig().getConfigurationSection("MaxLevel").getKeys(false).size()];
		int i = 0;
		for (String level : plugin.getPluginConfig().getConfigurationSection("MaxLevel").getKeys(false)) {
			achievementsMaxLevel[i] = Integer.parseInt(level);
			i++;
		}

		playerAchievements = new HashSet<?>[achievementsMaxLevel.length];
		for (i = 0; i < playerAchievements.length; ++i)
			playerAchievements[i] = new HashSet<Player>();
	}

	@SuppressWarnings("unchecked")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerExpChange(PlayerLevelChangeEvent event) {

		Player player = event.getPlayer();

		if (!player.hasPermission("achievement.count.maxlevel")
				|| plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player))
			return;

		int levels;
		if (!DatabasePools.getXpHashMap().containsKey(player.getUniqueId().toString()))
			levels = plugin.getDb().getNormalAchievementAmount(player, "levels");
		else
			levels = DatabasePools.getXpHashMap().get(player.getUniqueId().toString());

		if (event.getNewLevel() > levels)
			DatabasePools.getXpHashMap().put(player.getUniqueId().toString(), event.getNewLevel());
		else
			return;

		for (int i = 0; i < achievementsMaxLevel.length; i++) {
			if (event.getNewLevel() >= achievementsMaxLevel[i] && !playerAchievements[i].contains(player)) {
				if (!plugin.getDb().hasPlayerAchievement(player,
						plugin.getPluginConfig().getString("MaxLevel." + achievementsMaxLevel[i] + ".Name"))) {
					plugin.getAchievementDisplay().displayAchievement(player, "MaxLevel." + achievementsMaxLevel[i]);
					plugin.getDb().registerAchievement(player,
							plugin.getPluginConfig().getString("MaxLevel." + achievementsMaxLevel[i] + ".Name"),
							plugin.getPluginConfig().getString("MaxLevel." + achievementsMaxLevel[i] + ".Message"));
					plugin.getReward().checkConfig(player, "MaxLevel." + achievementsMaxLevel[i]);
				}

				((HashSet<Player>) playerAchievements[i]).add(player);
			}
		}
	}

	public HashSet<?>[] getPlayerAchievements() {

		return playerAchievements;
	}

}
