package com.hm.achievement.runnable;

import java.util.HashMap;
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

import com.google.common.collect.HashMultimap;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.particle.ReflectionUtils.PackageType;

/**
 * Class used to monitor distances travelled by players for the different available categories.
 * 
 * @author Pyves
 *
 */
public class AchieveDistanceRunnable implements Runnable {

	private AdvancedAchievements plugin;

	private boolean ignoreVerticalDistance;

	// Multimaps corresponding to the players who have received specific distance achievements.
	// Each key in the multimap corresponds to one achievement threshold, and has its associated player Set.
	// Used as pseudo-caching system to reduce load on database as distances are monitored on a regular basis.
	HashMultimap<Integer, String> footAchievementsCache;
	HashMultimap<Integer, String> horseAchievementsCache;
	HashMultimap<Integer, String> pigAchievementsCache;
	HashMultimap<Integer, String> minecartAchievementsCache;
	HashMultimap<Integer, String> boatAchievementsCache;
	HashMultimap<Integer, String> glidingAchievementsCache;

	// HashMaps containing distance statistics for each player.
	private Map<String, Integer> distancesFoot;
	private Map<String, Integer> distancesHorse;
	private Map<String, Integer> distancesPig;
	private Map<String, Integer> distancesBoat;
	private Map<String, Integer> distancesMinecart;
	private Map<String, Integer> distancesGliding;
	private HashMap<Player, Location> playerLocations;

	// Minecraft version to deal with gliding.
	private int version;

	public AchieveDistanceRunnable(AdvancedAchievements plugin) {

		this.plugin = plugin;

		// Simple and fast check to compare versions. Might need to be updated in the future depending on how the
		// Minecraft versions change in the future.
		version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);

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

		extractAchievementsFromConfig();
	}

	/**
	 * Loads list of achievements from configuration.
	 * 
	 */
	public void extractAchievementsFromConfig() {

		ignoreVerticalDistance = plugin.getConfig().getBoolean("IgnoreVerticalDistance", false);

		footAchievementsCache = extractDistanceAchievementFromConfig("DistanceFoot");
		horseAchievementsCache = extractDistanceAchievementFromConfig("DistanceHorse");
		pigAchievementsCache = extractDistanceAchievementFromConfig("DistancePig");
		minecartAchievementsCache = extractDistanceAchievementFromConfig("DistanceMinecart");
		boatAchievementsCache = extractDistanceAchievementFromConfig("DistanceBoat");
		glidingAchievementsCache = extractDistanceAchievementFromConfig("DistanceGliding");
	}

	@Override
	public void run() {

		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			refreshDistance(player);
		}
	}

	/**
	 * Update distances and store them into server's memory until player disconnects.
	 * 
	 * @param player
	 */
	public void refreshDistance(Player player) {

		Location previousLocation = playerLocations.get(player);

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

				boolean updateLocation = updateDistanceAndCheckAchievements(difference, player, distancesHorse,
						"DistanceHorse", horseAchievementsCache);

				if (!updateLocation)
					return;

			} else if (player.getVehicle() instanceof Pig && player.hasPermission("achievement.count.distancepig")
					&& !plugin.getDisabledCategorySet().contains("DistancePig")) {

				boolean updateLocation = updateDistanceAndCheckAchievements(difference, player, distancesPig,
						"DistancePig", pigAchievementsCache);

				if (!updateLocation)
					return;

			} else if (player.getVehicle() instanceof Minecart
					&& player.hasPermission("achievement.count.distanceminecart")
					&& !plugin.getDisabledCategorySet().contains("DistanceMinecart")) {

				boolean updateLocation = updateDistanceAndCheckAchievements(difference, player, distancesMinecart,
						"DistanceMinecart", minecartAchievementsCache);

				if (!updateLocation)
					return;

			} else if (player.getVehicle() instanceof Boat && player.hasPermission("achievement.count.distanceboat")
					&& !plugin.getDisabledCategorySet().contains("DistanceBoat")) {

				boolean updateLocation = updateDistanceAndCheckAchievements(difference, player, distancesBoat,
						"DistanceBoat", boatAchievementsCache);

				if (!updateLocation)
					return;

			}
		} else if (player.hasPermission("achievement.count.distancefoot") && !player.isFlying()
				&& (version < 9 || !player.isGliding()) && !plugin.getDisabledCategorySet().contains("DistanceFoot")) {

			boolean updateLocation = updateDistanceAndCheckAchievements(difference, player, distancesFoot,
					"DistanceFoot", footAchievementsCache);

			if (!updateLocation)
				return;

		} else if (player.hasPermission("achievement.count.distancegliding") && version >= 9 && player.isGliding()
				&& !plugin.getDisabledCategorySet().contains("DistanceGliding")) {

			boolean updateLocation = updateDistanceAndCheckAchievements(difference, player, distancesGliding,
					"DistanceGliding", glidingAchievementsCache);

			if (!updateLocation)
				return;
		}

		// Update player's location.
		playerLocations.put(player, player.getLocation());

	}

	/**
	 * Give a distance achievement to the player.
	 * 
	 * @param player
	 * @param achievementDistance
	 * @param type
	 */
	private void awardDistanceAchievement(Player player, int achievementDistance, String type) {

		YamlManager config = plugin.getPluginConfig();
		plugin.getAchievementDisplay().displayAchievement(player, type + achievementDistance);
		plugin.getDb().registerAchievement(player, config.getString(type + achievementDistance + ".Name"),
				config.getString(type + achievementDistance + ".Message"));
		plugin.getReward().checkConfig(player, type + achievementDistance);
	}

	/**
	 * Compare the distance travelled to the achievement thresholds. If a threshold is reached, award the achievement.
	 * Update the various tracking objects.
	 * 
	 * @param difference
	 * @param player
	 * @param distances
	 * @param achievementKeyName
	 * @param achievementsCache
	 * @return True if the player location should be updated; false if the distances Map does not contain the uuid
	 */
	private boolean updateDistanceAndCheckAchievements(int difference, Player player, Map<String, Integer> distances,
			String achievementKeyName, HashMultimap<Integer, String> achievementsCache) {

		String uuid = player.getUniqueId().toString();

		Integer distance = distances.get(uuid);
		// Distance didn't previously exist in the cache; retrieve it from the database and return.
		if (distance == null) {
			distances.put(uuid, plugin.getDb().updateAndGetDistance(uuid, 0, achievementKeyName.toLowerCase()));
			return false;
		}

		distance += difference;
		distances.put(uuid, distance);
		// Iterate through all the different achievements.
		for (Integer achievementThreshold : achievementsCache.keySet()) {
			// Check whether player has met the threshold and whether we he has not yet received the achievement.
			if (distance > achievementThreshold) {
				if (!achievementsCache.get(achievementThreshold).contains(uuid)) {
					String achievementName = plugin.getPluginConfig()
							.getString(achievementKeyName + "." + achievementThreshold + ".Name");
					// The cache does not contain information about the reception of the achievement. Query database.
					if (!plugin.getDb().hasPlayerAchievement(player, achievementName))
						awardDistanceAchievement(player, achievementThreshold, achievementKeyName + ".");
					// Player has received this achievement.
					achievementsCache.put(achievementThreshold, uuid);
				}
			}
		}
		return true;
	}

	/**
	 * Extract the different thresholds from the config to initially populate the cache.
	 * 
	 * @param achievementKeyName
	 * @return array containing thresholds for the achievement keyName.
	 */
	private HashMultimap<Integer, String> extractDistanceAchievementFromConfig(String achievementKeyName) {

		Set<String> configKeys = plugin.getConfig().getConfigurationSection(achievementKeyName).getKeys(false);

		HashMultimap<Integer, String> achievementsCache = HashMultimap.create(configKeys.size(), 1);

		// Populate the multimap with the different threshold keys and null values. This is used to easily iterate
		// through the thresholds without referring to the config file again.
		for (String distance : configKeys)
			achievementsCache.put(Integer.valueOf(distance), null);

		return achievementsCache;
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

	public HashMultimap<Integer, String> getFootAchievementsCache() {

		return footAchievementsCache;
	}

	public HashMultimap<Integer, String> getHorseAchievementsCache() {

		return horseAchievementsCache;
	}

	public HashMultimap<Integer, String> getPigAchievementsCache() {

		return pigAchievementsCache;
	}

	public HashMultimap<Integer, String> getMinecartAchievementsCache() {

		return minecartAchievementsCache;
	}

	public HashMultimap<Integer, String> getBoatAchievementsCache() {

		return boatAchievementsCache;
	}

	public HashMultimap<Integer, String> getGlidingAchievementsCache() {

		return glidingAchievementsCache;
	}

}
