package com.hm.achievement.runnable;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.utils.Reloadable;
import com.hm.mcshared.particle.ReflectionUtils.PackageType;

/**
 * Abstract class in charge of factoring out common functionality for runnables.
 * 
 * @author Pyves
 */
public class AbstractRunnable implements Reloadable {

	protected final AdvancedAchievements plugin;
	protected final int version;

	private boolean configRestrictCreative;
	private boolean configRestrictSpectator;
	private Set<String> configExcludedWorlds;

	public AbstractRunnable(AdvancedAchievements plugin) {
		this.plugin = plugin;
		// Simple parsing of game version. Might need to be updated in the future depending on how the Minecraft
		// versions change in the future.
		version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);
	}

	@Override
	public void extractConfigurationParameters() {
		configRestrictCreative = plugin.getPluginConfig().getBoolean("RestrictCreative", false);
		configRestrictSpectator = plugin.getPluginConfig().getBoolean("RestrictSpectator", true);
		// Spectator mode introduced in Minecraft 1.8.
		if (configRestrictSpectator && version < 8) {
			configRestrictSpectator = false;
			plugin.getLogger().warning(
					"Overriding configuration: disabling RestrictSpectator. Please set it to false in your config.");
		}
		configExcludedWorlds = new HashSet<>(plugin.getPluginConfig().getList("ExcludedWorlds"));
	}

	/**
	 * Determines whether the listened event should be taken into account. Ignore permission check.
	 * 
	 * @param player
	 * @param category
	 * @return
	 */
	protected boolean shouldRunBeTakenIntoAccount(Player player) {
		boolean isNPC = player.hasMetadata("NPC");
		boolean restrictedCreative = configRestrictCreative && player.getGameMode() == GameMode.CREATIVE;
		boolean restrictedSpectator = configRestrictSpectator && player.getGameMode() == GameMode.SPECTATOR;
		boolean excludedWorld = configExcludedWorlds.contains(player.getWorld().getName());

		return !isNPC && !restrictedCreative && !restrictedSpectator && !excludedWorld;
	}

}
