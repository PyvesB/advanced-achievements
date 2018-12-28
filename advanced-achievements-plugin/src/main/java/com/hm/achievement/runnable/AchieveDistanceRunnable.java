package com.hm.achievement.runnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import com.hm.achievement.category.Category;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.lifecycle.Cleanable;
import com.hm.achievement.utils.RewardParser;
import com.hm.achievement.utils.StatisticIncreaseHandler;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class used to monitor distances travelled by players for the different available categories.
 * 
 * @author Pyves
 *
 */
@Singleton
public class AchieveDistanceRunnable extends StatisticIncreaseHandler implements Cleanable, Runnable {

	private final Map<UUID, Location> playerLocations = new HashMap<>();
	private final Set<Category> disabledCategories;

	private boolean configIgnoreVerticalDistance;

	@Inject
	public AchieveDistanceRunnable(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser,
			Set<Category> disabledCategories) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
		this.disabledCategories = disabledCategories;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configIgnoreVerticalDistance = mainConfig.getBoolean("IgnoreVerticalDistance", false);
	}

	@Override
	public void cleanPlayerData(UUID uuid) {
		playerLocations.remove(uuid);
	}

	@Override
	public void run() {
		Bukkit.getOnlinePlayers().stream().forEach(this::validateMovementAndUpdateDistance);
	}

	public void updateLocation(UUID uuid, Location location) {
		playerLocations.put(uuid, location);
	}

	/**
	 * Update distances and store them into server's memory until player disconnects.
	 * 
	 * @param player
	 */
	private void validateMovementAndUpdateDistance(Player player) {
		// Update new location.
		Location previousLocation = playerLocations.put(player.getUniqueId(), player.getLocation());

		// If player location not found or if player has changed world, ignore previous location.
		// Evaluating distance would give an exception.
		if (previousLocation == null || !previousLocation.getWorld().getUID().equals(player.getWorld().getUID())) {
			return;
		}

		// If player is in restricted game mode or is in a blocked world, don't update distances.
		if (!shouldIncreaseBeTakenIntoAccountNoPermissions(player)) {
			return;
		}

		int difference = getDistanceDifference(player, previousLocation);
		// Player has not moved.
		if (difference == 0L) {
			return;
		}

		if (player.getVehicle() instanceof Horse) {
			updateDistance(difference, player, NormalAchievements.DISTANCEHORSE);
		} else if (player.getVehicle() instanceof Pig) {
			updateDistance(difference, player, NormalAchievements.DISTANCEPIG);
		} else if (player.getVehicle() instanceof Minecart) {
			updateDistance(difference, player, NormalAchievements.DISTANCEMINECART);
		} else if (player.getVehicle() instanceof Boat) {
			updateDistance(difference, player, NormalAchievements.DISTANCEBOAT);
		} else if (serverVersion >= 11 && player.getVehicle() instanceof Llama) {
			updateDistance(difference, player, NormalAchievements.DISTANCELLAMA);
		} else if (serverVersion >= 9 && player.isGliding()) {
			updateDistance(difference, player, NormalAchievements.DISTANCEGLIDING);
		} else if (!player.isFlying()) {
			updateDistance(difference, player, NormalAchievements.DISTANCEFOOT);
		}
	}

	/**
	 * Calculates the difference between the player's last location and his current one. May ignore the vertical axis or
	 * not depending on configuration..
	 * 
	 * @param player
	 * @param previousLocation
	 * @return difference
	 */
	private int getDistanceDifference(Player player, Location previousLocation) {
		if (configIgnoreVerticalDistance) {
			double xSquared = NumberConversions.square(previousLocation.getX() - player.getLocation().getX());
			double zSquared = NumberConversions.square(previousLocation.getZ() - player.getLocation().getZ());
			return (int) Math.sqrt(xSquared + zSquared);
		} else {
			return (int) previousLocation.distance(player.getLocation());
		}
	}

	/**
	 * Updates distance if all conditions are met and awards achievements if necessary.
	 * 
	 * @param difference
	 * @param player
	 * @param category
	 */
	private void updateDistance(int difference, Player player, NormalAchievements category) {
		if (!player.hasPermission(category.toPermName()) || disabledCategories.contains(category)) {
			return;
		}

		long distance = cacheManager.getAndIncrementStatisticAmount(category, player.getUniqueId(), difference);
		checkThresholdsAndAchievements(player, category.toString(), distance);
	}
}
