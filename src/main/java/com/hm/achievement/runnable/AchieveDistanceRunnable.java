package com.hm.achievement.runnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
	// Used as pseudo-caching system to reduce load on database.
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

		ignoreVerticalDistance = plugin.getPluginConfig().getBoolean("IgnoreVerticalDistance", false);

		// Simple and fast check to compare versions. Might need to
		// be updated in the future depending on how the Minecraft
		// versions change in the future.
		version = Integer.valueOf(Bukkit.getBukkitVersion().charAt(2) + "");

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

		achievementsFoot = new int[plugin.getPluginConfig().getConfigurationSection("DistanceFoot").getKeys(false)
				.size()];
		int i = 0;
		for (String distance : plugin.getPluginConfig().getConfigurationSection("DistanceFoot").getKeys(false)) {
			achievementsFoot[i] = Integer.parseInt(distance);
			i++;
		}
		achievementsPig = new int[plugin.getPluginConfig().getConfigurationSection("DistancePig").getKeys(false)
				.size()];
		i = 0;
		for (String distance : plugin.getPluginConfig().getConfigurationSection("DistancePig").getKeys(false)) {
			achievementsPig[i] = Integer.parseInt(distance);
			i++;
		}
		achievementsHorse = new int[plugin.getPluginConfig().getConfigurationSection("DistanceHorse").getKeys(false)
				.size()];
		i = 0;
		for (String distance : plugin.getPluginConfig().getConfigurationSection("DistanceHorse").getKeys(false)) {
			achievementsHorse[i] = Integer.parseInt(distance);
			i++;
		}
		achievementsMinecart = new int[plugin.getPluginConfig().getConfigurationSection("DistanceMinecart")
				.getKeys(false).size()];
		i = 0;
		for (String distance : plugin.getPluginConfig().getConfigurationSection("DistanceMinecart").getKeys(false)) {
			achievementsMinecart[i] = Integer.parseInt(distance);
			i++;
		}
		achievementsBoat = new int[plugin.getPluginConfig().getConfigurationSection("DistanceBoat").getKeys(false)
				.size()];
		i = 0;
		for (String distance : plugin.getPluginConfig().getConfigurationSection("DistanceBoat").getKeys(false)) {
			achievementsBoat[i] = Integer.parseInt(distance);
			i++;
		}
		achievementsGliding = new int[plugin.getPluginConfig().getConfigurationSection("DistanceGliding").getKeys(false)
				.size()];
		i = 0;
		for (String distance : plugin.getPluginConfig().getConfigurationSection("DistanceGliding").getKeys(false)) {
			achievementsGliding[i] = Integer.parseInt(distance);
			i++;
		}

		playerAchievementsFoot = new HashSet<?>[achievementsFoot.length];
		for (i = 0; i < playerAchievementsFoot.length; ++i)
			playerAchievementsFoot[i] = new HashSet<Player>();

		playerAchievementsHorse = new HashSet<?>[achievementsHorse.length];
		for (i = 0; i < playerAchievementsHorse.length; ++i)
			playerAchievementsHorse[i] = new HashSet<Player>();

		playerAchievementsPig = new HashSet<?>[achievementsPig.length];
		for (i = 0; i < playerAchievementsPig.length; ++i)
			playerAchievementsPig[i] = new HashSet<Player>();

		playerAchievementsBoat = new HashSet<?>[achievementsBoat.length];
		for (i = 0; i < playerAchievementsBoat.length; ++i)
			playerAchievementsBoat[i] = new HashSet<Player>();

		playerAchievementsMinecart = new HashSet<?>[achievementsMinecart.length];
		for (i = 0; i < playerAchievementsMinecart.length; ++i)
			playerAchievementsMinecart[i] = new HashSet<Player>();

		playerAchievementsGliding = new HashSet<?>[achievementsGliding.length];
		for (i = 0; i < playerAchievementsGliding.length; ++i)
			playerAchievementsGliding[i] = new HashSet<Player>();
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
		// If player has changed world, ignore previous location; evaluating
		// distance would give an exception.
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

				Integer distance = distancesHorse.get(uuid);

				if (distance == null) {
					distancesHorse.put(uuid, plugin.getDb().updateAndGetDistance(uuid, 0, "distancehorse"));
					return;
				}

				distance += difference;
				distancesHorse.put(uuid, distance);

				for (int i = 0; i < achievementsHorse.length; i++) {
					if (distance > achievementsHorse[i] && !playerAchievementsHorse[i].contains(player)) {
						if (!plugin.getDb().hasPlayerAchievement(player,
								plugin.getPluginConfig().getString("DistanceHorse." + achievementsHorse[i] + ".Name")))
							awardDistanceAchievement(player, achievementsHorse[i], "DistanceHorse.");

						((HashSet<Player>) playerAchievementsHorse[i]).add(player);
					}
				}
			} else if (player.getVehicle() instanceof Pig && player.hasPermission("achievement.count.distancepig")
					&& !plugin.getDisabledCategorySet().contains("DistancePig")) {

				Integer distance = distancesPig.get(uuid);

				if (distance == null) {
					distancesPig.put(uuid, plugin.getDb().updateAndGetDistance(uuid, 0, "distancepig"));
					return;
				}

				distance += difference;
				distancesPig.put(uuid, distance);

				for (int i = 0; i < achievementsPig.length; i++) {
					if (distance > achievementsPig[i] && !playerAchievementsPig[i].contains(player)) {
						if (!plugin.getDb().hasPlayerAchievement(player,
								plugin.getPluginConfig().getString("DistancePig." + achievementsPig[i] + ".Name")))
							awardDistanceAchievement(player, achievementsPig[i], "DistancePig.");

						((HashSet<Player>) playerAchievementsPig[i]).add(player);
					}
				}
			} else if (player.getVehicle() instanceof Minecart
					&& player.hasPermission("achievement.count.distanceminecart")
					&& !plugin.getDisabledCategorySet().contains("DistanceMinecart")) {

				Integer distance = distancesMinecart.get(uuid);

				if (distance == null) {
					distancesMinecart.put(uuid, plugin.getDb().updateAndGetDistance(uuid, 0, "distanceminecart"));
					return;
				}

				distance += difference;
				distancesMinecart.put(uuid, distance);

				for (int i = 0; i < achievementsMinecart.length; i++) {
					if (distance > achievementsMinecart[i] && !playerAchievementsMinecart[i].contains(player)) {
						if (!plugin.getDb().hasPlayerAchievement(player, plugin.getPluginConfig()
								.getString("DistanceMinecart." + achievementsMinecart[i] + ".Name")))
							awardDistanceAchievement(player, achievementsMinecart[i], "DistanceMinecart.");

						((HashSet<Player>) playerAchievementsMinecart[i]).add(player);
					}
				}
			} else if (player.getVehicle() instanceof Boat && player.hasPermission("achievement.count.distanceboat")
					&& !plugin.getDisabledCategorySet().contains("DistanceBoat")) {

				Integer distance = distancesBoat.get(uuid);

				if (distance == null) {
					distancesBoat.put(uuid, plugin.getDb().updateAndGetDistance(uuid, 0, "distanceboat"));
					return;
				}

				distance += difference;
				distancesBoat.put(uuid, distance);

				for (int i = 0; i < achievementsBoat.length; i++) {
					if (distance > achievementsBoat[i] && !playerAchievementsBoat[i].contains(player)) {
						if (!plugin.getDb().hasPlayerAchievement(player,
								plugin.getPluginConfig().getString("DistanceBoat." + achievementsBoat[i] + ".Name")))
							awardDistanceAchievement(player, achievementsBoat[i], "DistanceBoat.");

						((HashSet<Player>) playerAchievementsBoat[i]).add(player);
					}
				}
			}
		} else if (player.hasPermission("achievement.count.distancefoot") && !player.isFlying()
				&& (version < 9 || !player.isGliding()) && !plugin.getDisabledCategorySet().contains("DistanceFoot")) {

			Integer distance = distancesFoot.get(uuid);

			if (distance == null) {
				distancesFoot.put(uuid, plugin.getDb().updateAndGetDistance(uuid, 0, "distancefoot"));
				return;
			}

			distance += difference;
			distancesFoot.put(uuid, distance);

			for (int i = 0; i < achievementsFoot.length; i++) {
				if (distance > achievementsFoot[i] && !playerAchievementsFoot[i].contains(player)) {
					if (!plugin.getDb().hasPlayerAchievement(player,
							plugin.getPluginConfig().getString("DistanceFoot." + achievementsFoot[i] + ".Name")))
						awardDistanceAchievement(player, achievementsFoot[i], "DistanceFoot.");

					((HashSet<Player>) playerAchievementsFoot[i]).add(player);
				}
			}
		} else if (player.hasPermission("achievement.count.distancegliding") && version >= 9 && player.isGliding()
				&& !plugin.getDisabledCategorySet().contains("DistanceGliding")) {

			Integer distance = distancesGliding.get(uuid);

			if (distance == null) {
				distancesGliding.put(uuid, plugin.getDb().updateAndGetDistance(uuid, 0, "distancegliding"));
				return;
			}

			distance += difference;
			distancesGliding.put(uuid, distance);

			for (int i = 0; i < achievementsGliding.length; i++) {
				if (distance > achievementsGliding[i] && !playerAchievementsGliding[i].contains(player)) {
					if (!plugin.getDb().hasPlayerAchievement(player,
							plugin.getPluginConfig().getString("DistanceGliding." + achievementsGliding[i] + ".Name")))
						awardDistanceAchievement(player, achievementsGliding[i], "DistanceGliding.");

					((HashSet<Player>) playerAchievementsGliding[i]).add(player);
				}
			}
		}
		// Update player's location.
		playerLocations.put(player, player.getLocation());

	}

	private void awardDistanceAchievement(Player player, int achievementDistance, String type) {

		plugin.getAchievementDisplay().displayAchievement(player, type + achievementDistance);
		plugin.getDb().registerAchievement(player,
				plugin.getPluginConfig().getString(type + achievementDistance + ".Name"),
				plugin.getPluginConfig().getString(type + achievementDistance + ".Message"));
		plugin.getReward().checkConfig(player, type + achievementDistance);
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
