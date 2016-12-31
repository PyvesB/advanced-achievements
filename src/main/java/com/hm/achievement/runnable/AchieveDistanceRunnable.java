package com.hm.achievement.runnable;

import java.util.HashMap;
import java.util.Map;

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

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.utils.AchievementCommentedYamlConfiguration;
import com.hm.mcshared.particle.ReflectionUtils.PackageType;

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

		ignoreVerticalDistance = plugin.getPluginConfig().getBoolean("IgnoreVerticalDistance", false);
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
						NormalAchievements.DISTANCEHORSE);
			} else if (player.getVehicle() instanceof Pig
					&& !plugin.getDisabledCategorySet().contains(NormalAchievements.DISTANCEPIG.toString())) {
				updateLocation = updateDistanceAndCheckAchievements(difference, player, NormalAchievements.DISTANCEPIG);
			} else if (player.getVehicle() instanceof Minecart
					&& !plugin.getDisabledCategorySet().contains(NormalAchievements.DISTANCEMINECART.toString())) {
				updateLocation = updateDistanceAndCheckAchievements(difference, player,
						NormalAchievements.DISTANCEMINECART);
			} else if (player.getVehicle() instanceof Boat
					&& !plugin.getDisabledCategorySet().contains(NormalAchievements.DISTANCEBOAT.toString())) {
				updateLocation = updateDistanceAndCheckAchievements(difference, player,
						NormalAchievements.DISTANCEBOAT);
			} else if (version >= 11 && player.getVehicle() instanceof Llama
					&& !plugin.getDisabledCategorySet().contains(NormalAchievements.DISTANCELLAMA.toString())) {
				updateLocation = updateDistanceAndCheckAchievements(difference, player,
						NormalAchievements.DISTANCELLAMA);
			}
		} else if (!player.isFlying() && (version < 9 || !player.isGliding())
				&& !plugin.getDisabledCategorySet().contains(NormalAchievements.DISTANCEFOOT.toString())) {
			updateLocation = updateDistanceAndCheckAchievements(difference, player, NormalAchievements.DISTANCEFOOT);
		} else if (version >= 9 && player.isGliding()
				&& !plugin.getDisabledCategorySet().contains(NormalAchievements.DISTANCEGLIDING.toString())) {
			updateLocation = updateDistanceAndCheckAchievements(difference, player, NormalAchievements.DISTANCEGLIDING);
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
	 * Compares the distance travelled to the achievement thresholds. If a threshold is reached, awards the achievement.
	 * Updates the various tracking objects.
	 * 
	 * @param difference
	 * @param player
	 * @param category
	 * @param achievementsCache
	 * @return True if the player location should be updated; false if the distances Map does not contain the uuid
	 */
	private boolean updateDistanceAndCheckAchievements(int difference, Player player, NormalAchievements category) {

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
		for (String achievementThreshold : plugin.getPluginConfig().getConfigurationSection(category.toString())
				.getKeys(false)) {
			int threshold = Integer.parseInt(achievementThreshold);
			String achievementName = plugin.getPluginConfig()
					.getString(category + "." + achievementThreshold + ".Name");
			// Check whether player has met the threshold and whether we he has not yet received the achievement.
			if (distance > threshold && !plugin.getPoolsManager().hasPlayerAchievement(player, achievementName)) {
				awardDistanceAchievement(player, threshold, category + ".");
			}
		}
		return true;
	}

	/**
	 * Gives a distance achievement to the player.
	 * 
	 * @param player
	 * @param achievementDistance
	 * @param type
	 */
	private void awardDistanceAchievement(Player player, int achievementDistance, String type) {

		AchievementCommentedYamlConfiguration config = plugin.getPluginConfig();
		plugin.getAchievementDisplay().displayAchievement(player, type + achievementDistance);
		String achievementName = config.getString(type + achievementDistance + ".Name");
		plugin.getDb().registerAchievement(player, achievementName,
				config.getString(type + achievementDistance + ".Message"));
		String uuid = player.getUniqueId().toString();
		plugin.getPoolsManager().getReceivedAchievementsCache().put(uuid, achievementName);
		plugin.getPoolsManager().getNotReceivedAchievementsCache().remove(uuid, achievementName);
		plugin.getReward().checkConfig(player, type + achievementDistance);
	}

	public Map<String, Location> getPlayerLocations() {

		return playerLocations;
	}
}
