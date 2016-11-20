package com.hm.achievement.runnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import com.google.common.collect.HashMultimap;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.particle.ReflectionUtils.PackageType;
import com.hm.achievement.utils.YamlManager;

/**
 * Class used to monitor distances travelled by players for the different available categories.
 * 
 * @author Pyves
 *
 */
public class AchieveDistanceRunnable implements Runnable {

	private final AdvancedAchievements plugin;
	private final HashMap<String, Location> playerLocations;
	// Minecraft version to deal with gliding.
	private final int version;

	private boolean ignoreVerticalDistance;

	// Multimaps corresponding to the players who have received specific distance achievements.
	// Each key in the multimap corresponds to one achievement threshold, and has its associated player Set.
	// Used as pseudo-caching system to reduce load on database as distances are monitored on a regular basis.
	private HashMultimap<Integer, String> footAchievementsCache;
	private HashMultimap<Integer, String> horseAchievementsCache;
	private HashMultimap<Integer, String> pigAchievementsCache;
	private HashMultimap<Integer, String> minecartAchievementsCache;
	private HashMultimap<Integer, String> boatAchievementsCache;
	private HashMultimap<Integer, String> glidingAchievementsCache;
	private HashMultimap<Integer, String> llamaAchievementsCache;

	public AchieveDistanceRunnable(AdvancedAchievements plugin) {

		this.plugin = plugin;

		// Simple and fast check to compare versions. Might need to be updated in the future depending on how the
		// Minecraft versions change in the future.
		version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);

		playerLocations = new HashMap<>();

		extractAchievementsFromConfig();
	}

	/**
	 * Loads list of achievements from configuration.
	 * 
	 */
	public void extractAchievementsFromConfig() {

		ignoreVerticalDistance = plugin.getConfig().getBoolean("IgnoreVerticalDistance", false);

		footAchievementsCache = extractDistanceAchievementFromConfig(NormalAchievements.DISTANCEFOOT);
		horseAchievementsCache = extractDistanceAchievementFromConfig(NormalAchievements.DISTANCEHORSE);
		pigAchievementsCache = extractDistanceAchievementFromConfig(NormalAchievements.DISTANCEPIG);
		minecartAchievementsCache = extractDistanceAchievementFromConfig(NormalAchievements.DISTANCEMINECART);
		boatAchievementsCache = extractDistanceAchievementFromConfig(NormalAchievements.DISTANCEBOAT);
		glidingAchievementsCache = extractDistanceAchievementFromConfig(NormalAchievements.DISTANCEGLIDING);
		llamaAchievementsCache = extractDistanceAchievementFromConfig(NormalAchievements.DISTANCELLAMA);
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

		String uuid = player.getUniqueId().toString();
		Location previousLocation = playerLocations.get(uuid);

		// If player location not found, add it in table. If player has changed world, ignore previous location.
		// Evaluating distance would give an exception.
		if (previousLocation == null || !previousLocation.getWorld().getName().equals(player.getWorld().getName())) {
			playerLocations.put(uuid, player.getLocation());
			return;
		}

		// If player is in restricted creative mode or is in a blocked world,
		// don't update distances.
		if (plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player)) {
			return;
		}

		int difference = getDistanceDifference(player, previousLocation);
		// Player has not moved.
		if (difference == 0) {
			return;
		}

		// Should be the player's location be updated in the map?
		boolean updateLocation = true;

		if (player.isInsideVehicle()) {
			if (player.getVehicle() instanceof Horse
					&& !plugin.getDisabledCategorySet().contains(NormalAchievements.DISTANCEHORSE.toString())) {
				updateLocation = updateDistanceAndCheckAchievements(difference, player,
						NormalAchievements.DISTANCEHORSE, horseAchievementsCache);
			} else if (player.getVehicle() instanceof Pig
					&& !plugin.getDisabledCategorySet().contains(NormalAchievements.DISTANCEPIG.toString())) {
				updateLocation = updateDistanceAndCheckAchievements(difference, player, NormalAchievements.DISTANCEPIG,
						pigAchievementsCache);
			} else if (player.getVehicle() instanceof Minecart
					&& !plugin.getDisabledCategorySet().contains(NormalAchievements.DISTANCEMINECART.toString())) {
				updateLocation = updateDistanceAndCheckAchievements(difference, player,
						NormalAchievements.DISTANCEMINECART, minecartAchievementsCache);
			} else if (player.getVehicle() instanceof Boat
					&& !plugin.getDisabledCategorySet().contains(NormalAchievements.DISTANCEBOAT.toString())) {
				updateLocation = updateDistanceAndCheckAchievements(difference, player, NormalAchievements.DISTANCEBOAT,
						boatAchievementsCache);
			} else if (version >= 11 && player.getVehicle() instanceof Llama
					&& !plugin.getDisabledCategorySet().contains(NormalAchievements.DISTANCELLAMA.toString())) {
				updateLocation = updateDistanceAndCheckAchievements(difference, player,
						NormalAchievements.DISTANCELLAMA, llamaAchievementsCache);
			}
		} else if (!player.isFlying() && (version < 9 || !player.isGliding())
				&& !plugin.getDisabledCategorySet().contains(NormalAchievements.DISTANCEFOOT.toString())) {
			updateLocation = updateDistanceAndCheckAchievements(difference, player, NormalAchievements.DISTANCEFOOT,
					footAchievementsCache);
		} else if (version >= 9 && player.isGliding()
				&& !plugin.getDisabledCategorySet().contains(NormalAchievements.DISTANCEGLIDING.toString())) {
			updateLocation = updateDistanceAndCheckAchievements(difference, player, NormalAchievements.DISTANCEGLIDING,
					glidingAchievementsCache);
		}

		if (updateLocation) {
			playerLocations.put(uuid, player.getLocation());
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
		int difference;
		if (ignoreVerticalDistance) {
			difference = (int) Math.sqrt(NumberConversions.square(previousLocation.getX() - player.getLocation().getX())
					+ NumberConversions.square(previousLocation.getZ() - player.getLocation().getZ()));
		} else {
			difference = (int) previousLocation.distance(player.getLocation());
		}

		return difference;
	}

	/**
	 * Gives a distance achievement to the player.
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
	 * Compares the distance travelled to the achievement thresholds. If a threshold is reached, awards the achievement.
	 * Updates the various tracking objects.
	 * 
	 * @param difference
	 * @param player
	 * @param category
	 * @param achievementsCache
	 * @return True if the player location should be updated; false if the distances Map does not contain the uuid
	 */
	private boolean updateDistanceAndCheckAchievements(int difference, Player player, NormalAchievements category,
			HashMultimap<Integer, String> achievementsCache) {

		if (!player.hasPermission(category.toPermName())) {
			return true;
		}

		Map<String, Integer> map = plugin.getPoolsManager().getHashMap(category);
		String uuid = player.getUniqueId().toString();

		Integer distance = map.get(uuid);
		// Distance didn't previously exist in the cache; retrieve it from the database and return.
		if (distance == null) {
			map.put(uuid, plugin.getDb().getNormalAchievementAmount(player, category));
			return false;
		}

		distance += difference;
		map.put(uuid, distance);
		// Iterate through all the different achievements.
		for (Integer achievementThreshold : achievementsCache.keySet()) {
			// Check whether player has met the threshold and whether we he has not yet received the achievement.
			if (distance > achievementThreshold && !achievementsCache.get(achievementThreshold).contains(uuid)) {
				String achievementName = plugin.getPluginConfig()
						.getString(category + "." + achievementThreshold + ".Name");
				// The cache does not contain information about the reception of the achievement. Query database.
				if (!plugin.getDb().hasPlayerAchievement(player, achievementName)) {
					awardDistanceAchievement(player, achievementThreshold, category + ".");
				}
				// Player has received this achievement.
				achievementsCache.put(achievementThreshold, uuid);
			}
		}
		return true;
	}

	/**
	 * Extracts the different thresholds from the config to initially populate the cache.
	 * 
	 * @param achievementKeyName
	 * @return array containing thresholds for the achievement keyName.
	 */
	private HashMultimap<Integer, String> extractDistanceAchievementFromConfig(NormalAchievements category) {

		Set<String> configKeys = plugin.getConfig().getConfigurationSection(category.toString()).getKeys(false);

		HashMultimap<Integer, String> achievementsCache = HashMultimap.create(configKeys.size(), 1);

		// Populate the multimap with the different threshold keys and null values. This is used to easily iterate
		// through the thresholds without referring to the config file again.
		for (String distance : configKeys) {
			achievementsCache.put(Integer.valueOf(distance), null);
		}
		return achievementsCache;
	}

	public Map<String, Location> getPlayerLocations() {

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

	public HashMultimap<Integer, String> getLlamaAchievementsCache() {

		return llamaAchievementsCache;
	}
}
