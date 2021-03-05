package com.hm.achievement.runnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import com.hm.achievement.category.Category;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.lifecycle.Cleanable;
import com.hm.achievement.utils.StatisticIncreaseHandler;

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
	public AchieveDistanceRunnable(@Named("main") YamlConfiguration mainConfig, int serverVersion,
			AchievementMap achievementMap, CacheManager cacheManager, Set<Category> disabledCategories) {
		super(mainConfig, serverVersion, achievementMap, cacheManager);
		this.disabledCategories = disabledCategories;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configIgnoreVerticalDistance = mainConfig.getBoolean("IgnoreVerticalDistance");
	}

	@Override
	public void cleanPlayerData() {
		playerLocations.keySet().removeIf(player -> !Bukkit.getOfflinePlayer(player).isOnline());
	}

	@Override
	public void run() {
		Bukkit.getOnlinePlayers().forEach(this::validateMovementAndUpdateDistance);
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
		Location currentLocation = player.getLocation();
		Location previousLocation = playerLocations.put(player.getUniqueId(), currentLocation);

		// If player location not found or if player has changed world, ignore previous location.
		// Evaluating distance would give an exception.
		if (previousLocation == null || !previousLocation.getWorld().getUID().equals(player.getWorld().getUID())) {
			return;
		}

		int difference = getDistanceDifference(previousLocation, currentLocation);
		if (difference == 0L) { // Player has not moved.
			return;
		}

		if (player.isInsideVehicle()) {
			EntityType vehicleType = player.getVehicle().getType();
			if (vehicleType == EntityType.HORSE) {
				updateDistance(difference, player, NormalAchievements.DISTANCEHORSE);
			} else if (vehicleType == EntityType.PIG) {
				updateDistance(difference, player, NormalAchievements.DISTANCEPIG);
			} else if (vehicleType == EntityType.MINECART) {
				updateDistance(difference, player, NormalAchievements.DISTANCEMINECART);
			} else if (vehicleType == EntityType.BOAT) {
				updateDistance(difference, player, NormalAchievements.DISTANCEBOAT);
			} else if (serverVersion >= 11 && vehicleType == EntityType.LLAMA) {
				updateDistance(difference, player, NormalAchievements.DISTANCELLAMA);
			}
		} else if (serverVersion >= 9 && player.isGliding()) {
			updateDistance(difference, player, NormalAchievements.DISTANCEGLIDING);
		} else if (player.isSneaking()) {
			updateDistance(difference, player, NormalAchievements.DISTANCESNEAKING);
		} else if (!player.isFlying()) {
			updateDistance(difference, player, NormalAchievements.DISTANCEFOOT);
		}
	}

	/**
	 * Calculates the difference between the player's last location and his current one. May ignore the vertical axis or
	 * not depending on configuration..
	 * 
	 * @param previousLocation
	 * @param currentLocation
	 * 
	 * @return difference
	 */
	private int getDistanceDifference(Location previousLocation, Location currentLocation) {
		if (configIgnoreVerticalDistance) {
			double xSquared = NumberConversions.square(previousLocation.getX() - currentLocation.getX());
			double zSquared = NumberConversions.square(previousLocation.getZ() - currentLocation.getZ());
			return (int) Math.sqrt(xSquared + zSquared);
		} else {
			return (int) previousLocation.distance(currentLocation);
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
		if (!shouldIncreaseBeTakenIntoAccount(player, category) || disabledCategories.contains(category)) {
			return;
		}

		long distance = cacheManager.getAndIncrementStatisticAmount(category, player.getUniqueId(), difference);
		checkThresholdsAndAchievements(player, category, distance);
	}
}
