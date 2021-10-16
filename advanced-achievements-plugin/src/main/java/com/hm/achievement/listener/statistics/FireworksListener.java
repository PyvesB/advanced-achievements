package com.hm.achievement.listener.statistics;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;

/**
 * Listener class to deal with Fireworks achievements.
 *
 * @author Pyves
 *
 */
@Singleton
public class FireworksListener extends AbstractListener {

	private Set<String> forbiddenFireworkBlocks;
	private Set<String> forbiddenFireworkBlocksWhenNotSneaking;
	private final int serverVersion;

	@Inject
	public FireworksListener(@Named("main") YamlConfiguration mainConfig, int serverVersion, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(NormalAchievements.FIREWORKS, mainConfig, achievementMap, cacheManager);
		this.serverVersion = serverVersion;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		forbiddenFireworkBlocks = new HashSet<>();
		for (String block : mainConfig.getStringList("ForbiddenFireworkBlocks")) {
			forbiddenFireworkBlocks.add(block.toUpperCase());
		}
		forbiddenFireworkBlocksWhenNotSneaking = new HashSet<>();
		for (String block : mainConfig.getStringList("ForbiddenFireworkBlocksWhenNotSneaking")) {
			forbiddenFireworkBlocksWhenNotSneaking.add(block.toUpperCase());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR) // Do NOT set ignoreCancelled to true, see SPIGOT-4793.
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.useItemInHand() == Result.DENY || !event.hasItem()) {
			return;
		}

		Player player = event.getPlayer();
		if (event.getMaterial() != Material.FIREWORK_ROCKET
				|| !canAccommodateFireworkLaunch(event.getClickedBlock(), player, event.getAction())) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, 1);
	}

	/**
	 * Determines whether a firework can be launched when interacting with this block.
	 *
	 * @param clickedBlock
	 * @param player
	 * @param action
	 * @return true if the material can be used to launch a firework, false otherwise
	 */
	private boolean canAccommodateFireworkLaunch(Block clickedBlock, Player player, Action action) {
		// Players can launch fireworks without interacting with a block only if they're gliding.
		if (player.isGliding() && action == Action.RIGHT_CLICK_AIR) {
			return true;
		} else if (action != Action.RIGHT_CLICK_BLOCK) {
			return false;
		}
		if (!player.isSneaking()) {
			if (serverVersion >= 14 && clickedBlock.getType() == Material.SWEET_BERRY_BUSH) {
				return ((Ageable) clickedBlock.getBlockData()).getAge() <= 1;
			} else if (forbiddenFireworkBlocksWhenNotSneaking.contains(clickedBlock.getType().name())) {
				return false;
			}
		}
		return !forbiddenFireworkBlocks.contains(clickedBlock.getType().name());
	}
}
