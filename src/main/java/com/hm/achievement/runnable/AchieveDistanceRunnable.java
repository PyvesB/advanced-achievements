package com.hm.achievement.runnable;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.utils.Cleanable;

/**
 * Class used to monitor distances travelled by players for the different available categories.
 * 
 * @author Pyves
 *
 */
public class AchieveDistanceRunnable extends AbstractRunnable implements Cleanable, Runnable {

	private final Map<String, Location> playerLocations;
	// Keys in the map are thresholds as Long values, values are the paths to the achievements.
	private final Map<Long, String> horseThresholds;
	private final Map<Long, String> pigThresholds;
	private final Map<Long, String> minecartThresholds;
	private final Map<Long, String> boatThresholds;
	private final Map<Long, String> llamaThresholds;
	private final Map<Long, String> footThresholds;
	private final Map<Long, String> glidingThresholds;

	private boolean configIgnoreVerticalDistance;

	public AchieveDistanceRunnable(AdvancedAchievements plugin) {
		super(plugin);

		playerLocations = new HashMap<>();
		horseThresholds = new TreeMap<>();
		pigThresholds = new TreeMap<>();
		minecartThresholds = new TreeMap<>();
		boatThresholds = new TreeMap<>();
		llamaThresholds = new TreeMap<>();
		footThresholds = new TreeMap<>();
		glidingThresholds = new TreeMap<>();
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configIgnoreVerticalDistance = plugin.getPluginConfig().getBoolean("IgnoreVerticalDistance", false);

		populateThresholds(NormalAchievements.DISTANCEHORSE, horseThresholds);
		populateThresholds(NormalAchievements.DISTANCEPIG, pigThresholds);
		populateThresholds(NormalAchievements.DISTANCEMINECART, minecartThresholds);
		populateThresholds(NormalAchievements.DISTANCEBOAT, boatThresholds);
		populateThresholds(NormalAchievements.DISTANCELLAMA, llamaThresholds);
		populateThresholds(NormalAchievements.DISTANCEFOOT, footThresholds);
		populateThresholds(NormalAchievements.DISTANCEGLIDING, glidingThresholds);
	}

	@Override
	public void cleanPlayerData(UUID uuid) {
		playerLocations.remove(uuid.toString());
	}

	@Override
	public void run() {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			validateMovementAndUpdateDistance(player);
		}
	}

	public Map<String, Location> getPlayerLocations() {
		return playerLocations;
	}

	/**
	 * Update distances and store them into server's memory until player disconnects.
	 * 
	 * @param player
	 */
	private void validateMovementAndUpdateDistance(Player player) {
		String uuid = player.getUniqueId().toString();
		Location previousLocation = playerLocations.get(uuid);

		// Update new location.
		playerLocations.put(uuid, player.getLocation());

		// If player location not found or if player has changed world, ignore previous location.
		// Evaluating distance would give an exception.
		if (previousLocation == null || !previousLocation.getWorld().getName().equals(player.getWorld().getName())) {
			return;
		}

		// If player is in restricted game mode or is in a blocked world, don't update distances.
		if (!shouldRunBeTakenIntoAccount(player)) {
			return;
		}

		int difference = getDistanceDifference(player, previousLocation);
		// Player has not moved.
		if (difference == 0L) {
			return;
		}

		if (player.getVehicle() instanceof Horse) {
			updateDistance(difference, player, NormalAchievements.DISTANCEHORSE, horseThresholds);
		} else if (player.getVehicle() instanceof Pig) {
			updateDistance(difference, player, NormalAchievements.DISTANCEPIG, pigThresholds);
		} else if (player.getVehicle() instanceof Minecart) {
			updateDistance(difference, player, NormalAchievements.DISTANCEMINECART, minecartThresholds);
		} else if (player.getVehicle() instanceof Boat) {
			updateDistance(difference, player, NormalAchievements.DISTANCEBOAT, boatThresholds);
		} else if (version >= 11 && player.getVehicle() instanceof Llama) {
			updateDistance(difference, player, NormalAchievements.DISTANCELLAMA, llamaThresholds);
		} else if (!player.isFlying() && (version < 9 || !player.isGliding())) {
			updateDistance(difference, player, NormalAchievements.DISTANCEFOOT, footThresholds);
		} else if (version >= 9 && player.isGliding()) {
			updateDistance(difference, player, NormalAchievements.DISTANCEGLIDING, glidingThresholds);
		}
	}

	/**
	 * Calculates the difference between the player's last location and his current one.
	 * 
	 * @param player
	 * @param previousLocation
	 * @return difference
	 */
	private int getDistanceDifference(Player player, Location previousLocation) {
		// Distance difference since last runnable; ignore the vertical axis or not.
		double difference;
		if (configIgnoreVerticalDistance) {
			difference = Math.sqrt(NumberConversions.square(previousLocation.getX() - player.getLocation().getX())
					+ NumberConversions.square(previousLocation.getZ() - player.getLocation().getZ()));
		} else {
			difference = previousLocation.distance(player.getLocation());
		}
		return (int) difference;
	}

	/**
	 * Updates disatance if all conditions are met and awards achievements if necessary.
	 * 
	 * @param difference
	 * @param player
	 * @param category
	 * @param thresholds
	 */
	private void updateDistance(int difference, Player player, NormalAchievements category,
			Map<Long, String> thresholds) {
		if (!player.hasPermission(category.toPermName())
				|| plugin.getDisabledCategorySet().contains(category.toString())) {
			return;
		}

		long distance = plugin.getCacheManager().getAndIncrementStatisticAmount(category, player.getUniqueId(),
				difference);

		checkThresholdsAndAchievements(player, thresholds, distance);
	}

	/**
	 * Parses the configuration for a given category and populates the relevant map accordingly.
	 * 
	 * @param category
	 * @param thresholds
	 */
	private void populateThresholds(NormalAchievements category, Map<Long, String> thresholds) {
		thresholds.clear();
		for (String achievementThreshold : plugin.getPluginConfig().getConfigurationSection(category.toString())
				.getKeys(false)) {
			thresholds.put(Long.parseLong(achievementThreshold), category + "." + achievementThreshold);
		}
	}
}
