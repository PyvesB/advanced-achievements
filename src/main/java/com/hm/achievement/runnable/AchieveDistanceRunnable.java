package com.hm.achievement.runnable;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import com.hm.achievement.AdvancedAchievements;

public class AchieveDistanceRunnable implements Runnable {

	private AdvancedAchievements plugin;

	// Lists of achievements extracted from configuration.
	private int[] achievementsFoot;
	private int[] achievementsPig;
	private int[] achievementsHorse;
	private int[] achievementsMinecart;
	private int[] achievementsBoat;

	// Sets corresponding to whether a player has obtained a specific
	// distance achievement.
	// Used as pseudo-caching system to reduce load on database.
	private HashSet<?>[] playerAchievementsFoot;
	private HashSet<?>[] playerAchievementsHorse;
	private HashSet<?>[] playerAchievementsPig;
	private HashSet<?>[] playerAchievementsMinecart;
	private HashSet<?>[] playerAchievementsBoat;

	// HashMaps containing distance statistics for each player.
	private HashMap<Player, Integer> distancesFoot;
	private HashMap<Player, Integer> distancesHorse;
	private HashMap<Player, Integer> distancesPig;
	private HashMap<Player, Integer> distancesBoat;
	private HashMap<Player, Integer> distancesMinecart;
	private HashMap<Player, Location> playerLocations;

	public AchieveDistanceRunnable(AdvancedAchievements plugin) {

		this.plugin = plugin;
		distancesFoot = new HashMap<Player, Integer>();
		distancesHorse = new HashMap<Player, Integer>();
		distancesPig = new HashMap<Player, Integer>();
		distancesBoat = new HashMap<Player, Integer>();
		distancesMinecart = new HashMap<Player, Integer>();
		playerLocations = new HashMap<Player, Location>();

		extractAchievementsFromConfig(plugin);

	}

	/**
	 * Load list of achievements from configuration.
	 */
	public void extractAchievementsFromConfig(AdvancedAchievements plugin) {

		achievementsFoot = new int[plugin.getConfig().getConfigurationSection("DistanceFoot").getKeys(false).size()];
		int i = 0;
		for (String distance : plugin.getConfig().getConfigurationSection("DistanceFoot").getKeys(false)) {
			achievementsFoot[i] = Integer.parseInt(distance);
			i++;
		}
		achievementsPig = new int[plugin.getConfig().getConfigurationSection("DistancePig").getKeys(false).size()];
		i = 0;
		for (String distance : plugin.getConfig().getConfigurationSection("DistancePig").getKeys(false)) {
			achievementsPig[i] = Integer.parseInt(distance);
			i++;
		}
		achievementsHorse = new int[plugin.getConfig().getConfigurationSection("DistanceHorse").getKeys(false).size()];
		i = 0;
		for (String distance : plugin.getConfig().getConfigurationSection("DistanceHorse").getKeys(false)) {
			achievementsHorse[i] = Integer.parseInt(distance);
			i++;
		}
		achievementsMinecart = new int[plugin.getConfig().getConfigurationSection("DistanceMinecart").getKeys(false)
				.size()];
		i = 0;
		for (String distance : plugin.getConfig().getConfigurationSection("DistanceMinecart").getKeys(false)) {
			achievementsMinecart[i] = Integer.parseInt(distance);
			i++;
		}
		achievementsBoat = new int[plugin.getConfig().getConfigurationSection("DistanceBoat").getKeys(false).size()];
		i = 0;
		for (String distance : plugin.getConfig().getConfigurationSection("DistanceBoat").getKeys(false)) {
			achievementsBoat[i] = Integer.parseInt(distance);
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
	}

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

		// If player location not found, add it in table.
		if (previousLocation == null) {
			playerLocations.put(player, player.getLocation());
			return;
		}

		// Distance difference since last runnable.
		int difference = (int) previousLocation.distance(player.getLocation());

		// Check if player has not moved since last run.
		if (difference == 0)
			return;

		if (player.isInsideVehicle()) {
			if (player.getVehicle() instanceof Horse && player.hasPermission("achievement.count.distancehorse")) {

				Integer distance = distancesHorse.get(player);

				if (distance == null) {
					distancesHorse.put(player, plugin.getDb().updateAndGetDistance(player, 0, "distancehorse"));
					return;
				}

				distance += difference;
				distancesHorse.put(player, distance);

				for (int i = 0; i < achievementsHorse.length; i++) {
					if (distance > achievementsHorse[i] && !playerAchievementsHorse[i].contains(player)) {
						if (!plugin.getDb().hasPlayerAchievement(player,
								plugin.getConfig().getString("DistanceHorse." + achievementsHorse[i] + ".Name")))
							awardDistanceAchievement(player, achievementsHorse[i], "DistanceHorse.");

						((HashSet<Player>) playerAchievementsHorse[i]).add(player);
					}
				}
			} else if (player.getVehicle() instanceof Pig && player.hasPermission("achievement.count.distancepig")) {

				Integer distance = distancesPig.get(player);

				if (distance == null) {
					distancesPig.put(player, plugin.getDb().updateAndGetDistance(player, 0, "distancepig"));
					return;
				}

				distance += difference;

				for (int i = 0; i < achievementsPig.length; i++) {
					if (distance > achievementsPig[i] && !playerAchievementsPig[i].contains(player)) {
						if (!plugin.getDb().hasPlayerAchievement(player,
								plugin.getConfig().getString("DistancePig." + achievementsPig[i] + ".Name")))
							awardDistanceAchievement(player, achievementsPig[i], "DistancePig.");

						((HashSet<Player>) playerAchievementsPig[i]).add(player);
					}
				}
			} else if (player.getVehicle() instanceof Minecart
					&& player.hasPermission("achievement.count.distanceminecart")) {

				Integer distance = distancesMinecart.get(player);

				if (distance == null) {
					distancesMinecart.put(player, plugin.getDb().updateAndGetDistance(player, 0, "distanceminecart"));
					return;
				}

				distance += difference;
				distancesMinecart.put(player, distance);

				for (int i = 0; i < achievementsMinecart.length; i++) {
					if (distance > achievementsMinecart[i] && !playerAchievementsMinecart[i].contains(player)) {
						if (!plugin.getDb().hasPlayerAchievement(player,
								plugin.getConfig().getString("DistanceMinecart." + achievementsMinecart[i] + ".Name")))
							awardDistanceAchievement(player, achievementsMinecart[i], "DistanceMinecart.");

						((HashSet<Player>) playerAchievementsMinecart[i]).add(player);
					}
				}
			} else if (player.getVehicle() instanceof Boat && player.hasPermission("achievement.count.distanceboat")) {

				Integer distance = distancesBoat.get(player);

				if (distance == null) {
					distancesBoat.put(player, plugin.getDb().updateAndGetDistance(player, 0, "distanceboat"));
					return;
				}

				distance += difference;
				distancesBoat.put(player, distance);

				for (int i = 0; i < achievementsBoat.length; i++) {
					if (distance > achievementsBoat[i] && !playerAchievementsBoat[i].contains(player)) {
						if (!plugin.getDb().hasPlayerAchievement(player,
								plugin.getConfig().getString("DistanceBoat." + achievementsBoat[i] + ".Name")))
							awardDistanceAchievement(player, achievementsBoat[i], "DistanceBoat.");

						((HashSet<Player>) playerAchievementsBoat[i]).add(player);
					}
				}
			}
		} else if (player.hasPermission("achievement.count.distancefoot")) {

			Integer distance = distancesFoot.get(player);

			if (distance == null) {
				distancesFoot.put(player, plugin.getDb().updateAndGetDistance(player, 0, "distancefoot"));
				return;
			}

			distance += difference;
			distancesFoot.put(player, distance);

			for (int i = 0; i < achievementsFoot.length; i++) {
				if (distance > achievementsFoot[i] && !playerAchievementsFoot[i].contains(player)) {
					if (!plugin.getDb().hasPlayerAchievement(player,
							plugin.getConfig().getString("DistanceFoot." + achievementsFoot[i] + ".Name")))
						awardDistanceAchievement(player, achievementsFoot[i], "DistanceFoot.");

					((HashSet<Player>) playerAchievementsFoot[i]).add(player);
				}
			}
		}
		// Update player's location.
		playerLocations.put(player, player.getLocation());

	}

	public void awardDistanceAchievement(Player player, int achievementDistance, String type) {

		plugin.getAchievementDisplay().displayAchievement(player, type + achievementDistance);
		plugin.getDb().registerAchievement(player, plugin.getConfig().getString(type + achievementDistance + ".Name"),
				plugin.getConfig().getString(type + achievementDistance + ".Message"));
		plugin.getReward().checkConfig(player, type + achievementDistance);
	}

	public HashMap<Player, Integer> getAchievementDistancesFoot() {

		return distancesFoot;
	}

	public HashMap<Player, Integer> getAchievementDistancesHorse() {

		return distancesHorse;
	}

	public HashMap<Player, Integer> getAchievementDistancesPig() {

		return distancesPig;
	}

	public HashMap<Player, Integer> getAchievementDistancesBoat() {

		return distancesBoat;
	}

	public HashMap<Player, Integer> getAchievementDistancesMinecart() {

		return distancesMinecart;
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

}
