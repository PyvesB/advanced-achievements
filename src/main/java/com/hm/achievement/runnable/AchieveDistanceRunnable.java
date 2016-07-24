package com.hm.achievement.runnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.hm.achievement.utils.YamlManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.particle.ReflectionUtils.PackageType;

public class AchieveDistanceRunnable implements Runnable {

	private AdvancedAchievements plugin;

	boolean ignoreVerticalDistance;

	// Lists of achievement amounts (=thresholds) extracted from configuration.
	private int[] achievementsFoot;
	private int[] achievementsPig;
	private int[] achievementsHorse;
	private int[] achievementsMinecart;
	private int[] achievementsBoat;
	private int[] achievementsGliding;

	// Sets corresponding to whether a player has obtained a specific
	// distance achievement.
	// Uses as pseudo-caching system to reduce load on database.
	private HashSet<?>[] playerAchievementsFoot;
	private HashSet<?>[] playerAchievementsHorse;
	private HashSet<?>[] playerAchievementsPig;
	private HashSet<?>[] playerAchievementsMinecart;
	private HashSet<?>[] playerAchievementsBoat;
	private HashSet<?>[] playerAchievementsGliding;

	// HashMaps containing distance statistics for each player.
	private Map<String, Integer> distancesFoot;
	private Map<String, Integer> distancesHorse;
	private Map<String, Integer> distancesPig;
	private Map<String, Integer> distancesBoat;
	private Map<String, Integer> distancesMinecart;
	private Map<String, Integer> distancesGliding;
	private HashMap<Player, Location> playerLocations;

	// Minecraft version to deal with gliding.
	private Integer version;

	public AchieveDistanceRunnable(AdvancedAchievements plugin) {

		this.plugin = plugin;

		// Simple and fast check to compare versions. Might need to
		// be updated in the future depending on how the Minecraft
		// versions change in the future.
		version =  Integer.parseInt(PackageType.getServerVersion().split("_")[1]);

		if (plugin.isAsyncPooledRequestsSender()) {
			distancesFoot = new ConcurrentHashMap<String, Integer>();
			distancesHorse = new ConcurrentHashMap<String, Integer>();
			distancesPig = new ConcurrentHashMap<String, Integer>();
			distancesBoat = new ConcurrentHashMap<String, Integer>();
			distancesMinecart = new ConcurrentHashMap<String, Integer>();
			distancesGliding = new ConcurrentHashMap<String, Integer>();
		} else {
			distancesFoot = new HashMap<String, Integer>();
			distancesHorse = new HashMap<String, Integer>();
			distancesPig = new HashMap<String, Integer>();
			distancesBoat = new HashMap<String, Integer>();
			distancesMinecart = new HashMap<String, Integer>();
			distancesGliding = new HashMap<String, Integer>();
		}
		playerLocations = new HashMap<Player, Location>();

		extractAchievementsFromConfig(plugin);

	}

	/**
	 * Load list of achievements from configuration.
	 */
	public void extractAchievementsFromConfig(AdvancedAchievements plugin) {

		YamlManager config = plugin.getPluginConfig();

		ignoreVerticalDistance = config.getBoolean("IgnoreVerticalDistance", false);

		achievementsFoot = extractDistanceAchievementFromConfig(config, "DistanceFoot");
		achievementsPig = extractDistanceAchievementFromConfig(config, "DistancePig");
		achievementsHorse = extractDistanceAchievementFromConfig(config, "DistanceHorse");
		achievementsMinecart = extractDistanceAchievementFromConfig(config, "DistanceMinecart");
		achievementsBoat = extractDistanceAchievementFromConfig(config, "DistanceBoat");
		achievementsGliding = extractDistanceAchievementFromConfig(config, "DistanceGliding");

		playerAchievementsFoot = initializePlayerAchievements(achievementsFoot.length);
		playerAchievementsPig = initializePlayerAchievements(achievementsPig.length);
		playerAchievementsHorse = initializePlayerAchievements(achievementsHorse.length);
		playerAchievementsMinecart = initializePlayerAchievements(achievementsMinecart.length);
		playerAchievementsBoat = initializePlayerAchievements(achievementsBoat.length);
		playerAchievementsGliding = initializePlayerAchievements(achievementsGliding.length);

	}

	@Override
	public void run() {

		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			refreshDistance(player);
		}

	}

	@SuppressWarnings("unchecked")
	/**
	 * Update distances and store them into server's memory until player
	 * disconnects.
	 */
	public void refreshDistance(Player player) {

		Location previousLocation = playerLocations.get(player);
		String uuid = player.getUniqueId().toString();

		// If player location not found, add it in table.
		// If player has changed world, ignore previous location;
		// evaluating distance would give an exception.
		if (previousLocation == null || !previousLocation.getWorld().getName().equals(player.getWorld().getName())) {
			playerLocations.put(player, player.getLocation());
			return;
		}

		// If player is in restricted creative mode or is in a blocked world,
		// don't update distances.
		if (plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE || plugin.isInExludedWorld(player))
			return;

		// Distance difference since last runnable; ignore the vertical axis or not.
		int difference;
		if (ignoreVerticalDistance)
			difference = (int) Math.sqrt(NumberConversions.square(previousLocation.getX() - player.getLocation().getX())
					+ NumberConversions.square(previousLocation.getZ() - player.getLocation().getZ()));
		else
			difference = (int) previousLocation.distance(player.getLocation());

		// Check if player has not moved since last run.
		if (difference == 0)
			return;

		if (player.isInsideVehicle()) {
			if (player.getVehicle() instanceof Horse && player.hasPermission("achievement.count.distancehorse")
					&& !plugin.getDisabledCategorySet().contains("DistanceHorse")) {

				boolean updateLocation = checkDistanceAchievement(
						difference, player, uuid,
						distancesHorse, "DistanceHorse",
						achievementsHorse, playerAchievementsHorse);

				if (!updateLocation)
					return;

			} else if (player.getVehicle() instanceof Pig && player.hasPermission("achievement.count.distancepig")
					&& !plugin.getDisabledCategorySet().contains("DistancePig")) {

				boolean updateLocation = checkDistanceAchievement(
						difference, player, uuid,
						distancesPig, "DistancePig",
						achievementsPig, playerAchievementsPig);

				if (!updateLocation)
					return;

			} else if (player.getVehicle() instanceof Minecart
					&& player.hasPermission("achievement.count.distanceminecart")
					&& !plugin.getDisabledCategorySet().contains("DistanceMinecart")) {

				boolean updateLocation = checkDistanceAchievement(
						difference, player, uuid,
						distancesMinecart, "DistanceMinecart",
						achievementsMinecart, playerAchievementsMinecart);

				if (!updateLocation)
					return;

			} else if (player.getVehicle() instanceof Boat && player.hasPermission("achievement.count.distanceboat")
					&& !plugin.getDisabledCategorySet().contains("DistanceBoat")) {

				boolean updateLocation = checkDistanceAchievement(
						difference, player, uuid,
						distancesBoat, "DistanceBoat",
						achievementsBoat, playerAchievementsBoat);

				if (!updateLocation)
					return;

			}
		} else if (player.hasPermission("achievement.count.distancefoot") && !player.isFlying()
				&& (version < 9 || !player.isGliding()) && !plugin.getDisabledCategorySet().contains("DistanceFoot")) {

			boolean updateLocation = checkDistanceAchievement(
					difference, player, uuid,
					distancesFoot, "DistanceFoot",
					achievementsFoot, playerAchievementsFoot);

			if (!updateLocation)
				return;

		} else if (player.hasPermission("achievement.count.distancegliding") && version >= 9 && player.isGliding()
				&& !plugin.getDisabledCategorySet().contains("DistanceGliding")) {

			boolean updateLocation = checkDistanceAchievement(
					difference, player, uuid,
					distancesGliding, "DistanceGliding",
					achievementsGliding, playerAchievementsGliding);

			if (!updateLocation)
				return;
		}

		// Update player's location.
		playerLocations.put(player, player.getLocation());

	}

	private void awardDistanceAchievement(Player player, int achievementDistance, String type) {

		YamlManager config = plugin.getPluginConfig();
		plugin.getAchievementDisplay().displayAchievement(player, type + achievementDistance);
		plugin.getDb().registerAchievement(player,
				config.getString(type + achievementDistance + ".Name"),
				config.getString(type + achievementDistance + ".Message"));
		plugin.getReward().checkConfig(player, type + achievementDistance);
	}


	/**
	 * Compare the distance difference to the movement achievement thresholds
	 * If a threshold is reached, award the achievment
	 * Update the various tracking objects
	 * @param difference
	 * @param player
	 * @param uuid
	 * @param distances
	 * @param achievementKeyName
	 * @param achievementAmounts
	 * @param playerAchievements
	 * @return True if the player location should be updated; false if the distances Map does not contain the uuid
	 */
	private boolean checkDistanceAchievement(
			int difference,
			Player player,
			String uuid,
			Map<String, Integer> distances,
			String achievementKeyName,
			int[] achievementAmounts,
			HashSet<?>[] playerAchievements) {

		Integer distance = distances.get(uuid);

		if (distance == null) {
			distances.put(uuid, plugin.getDb().updateAndGetDistance(uuid, 0, achievementKeyName.toLowerCase()));
			return false;
		}

		distance += difference;
		distances.put(uuid, distance);

		for (int i = 0; i < achievementAmounts.length; i++) {
			if (distance > achievementAmounts[i] && !playerAchievements[i].contains(player)) {
				String achievementName = plugin.getPluginConfig()
						.getString(achievementKeyName + "." + achievementAmounts[i] + ".Name");

				if (!plugin.getDb().hasPlayerAchievement(player, achievementName))
					awardDistanceAchievement(player, achievementAmounts[i], achievementKeyName + ".");

				((HashSet<Player>) playerAchievements[i]).add(player);
			}
		}

		return true;
	}

	private int[] extractDistanceAchievementFromConfig(YamlManager config, String achievementKeyName) {

		Set<String> configKeys = config.getConfigurationSection(achievementKeyName).getKeys(false);

		int[] achievementThresholds = new int[configKeys.size()];

		int i = 0;
		for (String distance : configKeys) {
			achievementThresholds[i] = Integer.parseInt(distance);
			i++;
		}

		return achievementThresholds;
	}

	private HashSet<?>[] initializePlayerAchievements(int thresholdCount) {

		HashSet<?>[] playerAchievements = new HashSet<?>[thresholdCount];

		for (int i = 0; i < playerAchievements.length; ++i)
			playerAchievements[i] = new HashSet<Player>();

		return playerAchievements;
	}

	public Map<String, Integer> getAchievementDistancesFoot() {

		return distancesFoot;
	}

	public Map<String, Integer> getAchievementDistancesHorse() {

		return distancesHorse;
	}

	public Map<String, Integer> getAchievementDistancesPig() {

		return distancesPig;
	}

	public Map<String, Integer> getAchievementDistancesMinecart() {

		return distancesMinecart;
	}

	public Map<String, Integer> getAchievementDistancesBoat() {

		return distancesBoat;
	}

	public Map<String, Integer> getAchievementDistancesGliding() {

		return distancesGliding;
	}

	public HashMap<Player, Location> getPlayerLocations() {

		return playerLocations;
	}

	public HashSet<?>[] getPlayerAchievementsFoot() {

		return playerAchievementsFoot;
	}

	public HashSet<?>[] getPlayerAchievementsHorse() {

		return playerAchievementsHorse;
	}

	public HashSet<?>[] getPlayerAchievementsPig() {

		return playerAchievementsPig;
	}

	public HashSet<?>[] getPlayerAchievementsMinecart() {

		return playerAchievementsMinecart;
	}

	public HashSet<?>[] getPlayerAchievementsBoat() {

		return playerAchievementsBoat;
	}

	public HashSet<?>[] getPlayerAchievementsGliding() {

		return playerAchievementsGliding;
	}

}
