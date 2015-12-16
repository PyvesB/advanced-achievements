package com.hm.achievement.runnable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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
	private ArrayList<Integer> achievementsFoot;
	private ArrayList<Integer> achievementsPig;
	private ArrayList<Integer> achievementsHorse;
	private ArrayList<Integer> achievementsMinecart;
	private ArrayList<Integer> achievementsBoat;
	private static HashMap<Player, Long> achievementDistancesFoot;
	private static HashMap<Player, Long> achievementDistancesHorse;
	private static HashMap<Player, Long> achievementDistancesPig;
	private static HashMap<Player, Long> achievementDistancesBoat;
	private static HashMap<Player, Long> achievementDistancesMinecart;
	private static HashMap<Player, Location> achievementLocations;

	public AchieveDistanceRunnable(AdvancedAchievements plugin) {

		this.plugin = plugin;
		achievementDistancesFoot = new HashMap<Player, Long>();
		achievementDistancesHorse = new HashMap<Player, Long>();
		achievementDistancesPig = new HashMap<Player, Long>();
		achievementDistancesBoat = new HashMap<Player, Long>();
		achievementDistancesMinecart = new HashMap<Player, Long>();
		achievementLocations = new HashMap<Player, Location>();

		achievementsFoot = new ArrayList<Integer>();
		for (String distance : plugin.getConfig().getConfigurationSection("DistanceFoot").getKeys(false))
			achievementsFoot.add(Integer.parseInt(distance));
		achievementsPig = new ArrayList<Integer>();
		for (String distance : plugin.getConfig().getConfigurationSection("DistancePig").getKeys(false))
			achievementsPig.add(Integer.parseInt(distance));
		achievementsHorse = new ArrayList<Integer>();
		for (String distance : plugin.getConfig().getConfigurationSection("DistanceHorse").getKeys(false))
			achievementsHorse.add(Integer.parseInt(distance));
		achievementsMinecart = new ArrayList<Integer>();
		for (String distance : plugin.getConfig().getConfigurationSection("DistanceMinecart").getKeys(false))
			achievementsMinecart.add(Integer.parseInt(distance));
		achievementsBoat = new ArrayList<Integer>();
		for (String distance : plugin.getConfig().getConfigurationSection("DistanceBoat").getKeys(false))
			achievementsBoat.add(Integer.parseInt(distance));
	}

	public void run() {

		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			refreshDistance(player);
		}

	}

	public void refreshDistance(Player player) {

		/**
		 * Update distances and store them into server's memory until player
		 * disconnects.
		 */
		if (!achievementDistancesFoot.containsKey(player)) {
			achievementDistancesBoat.put(player, plugin.getDb().updateAndGetDistance(player, 0, "distanceboat"));
			achievementDistancesMinecart
					.put(player, plugin.getDb().updateAndGetDistance(player, 0, "distanceminecart"));
			achievementDistancesPig.put(player, plugin.getDb().updateAndGetDistance(player, 0, "distancepig"));
			achievementDistancesHorse.put(player, plugin.getDb().updateAndGetDistance(player, 0, "distancehorse"));
			achievementDistancesFoot.put(player, plugin.getDb().updateAndGetDistance(player, 0, "distancefoot"));

			AchieveDistanceRunnable.getAchievementLocations().put(player, player.getLocation());
		} else {

			if (player.isInsideVehicle()) {
				if (player.getVehicle() instanceof Horse) {
					achievementDistancesHorse.put(player,
							(long) (achievementDistancesHorse.get(player) + achievementLocations.get(player)
									.distanceSquared(player.getLocation())));

					for (int achievementDistance : achievementsHorse) {
						if (achievementDistancesHorse.get(player) > achievementDistance * achievementDistance
								&& !plugin.getDb().hasPlayerAchievement(player,
										plugin.getConfig().getString("DistanceHorse." + achievementDistance + ".Name")))

							awardDistanceAchievement(player, achievementDistance, "DistanceHorse.");

					}
				} else if (player.getVehicle() instanceof Pig) {
					achievementDistancesPig.put(player,
							(long) (achievementDistancesPig.get(player) + achievementLocations.get(player)
									.distanceSquared(player.getLocation())));

					for (int achievementDistance : achievementsPig) {
						if (achievementDistancesPig.get(player) > achievementDistance * achievementDistance
								&& !plugin.getDb().hasPlayerAchievement(player,
										plugin.getConfig().getString("DistancePig." + achievementDistance + ".Name")))

							awardDistanceAchievement(player, achievementDistance, "DistancePig.");
					}
				} else if (player.getVehicle() instanceof Minecart) {
					achievementDistancesMinecart.put(player,
							(long) (achievementDistancesMinecart.get(player) + achievementLocations.get(player)
									.distanceSquared(player.getLocation())));

					for (int achievementDistance : achievementsMinecart) {
						if (achievementDistancesMinecart.get(player) > achievementDistance * achievementDistance
								&& !plugin.getDb().hasPlayerAchievement(
										player,
										plugin.getConfig().getString(
												"DistanceMinecart." + achievementDistance + ".Name")))

							awardDistanceAchievement(player, achievementDistance, "DistanceMinecart.");
					}
				} else if (player.getVehicle() instanceof Boat) {
					achievementDistancesBoat.put(player,
							(long) (achievementDistancesBoat.get(player) + achievementLocations.get(player)
									.distanceSquared(player.getLocation())));

					for (int achievementDistance : achievementsBoat) {
						if (achievementDistancesBoat.get(player) > achievementDistance * achievementDistance
								&& !plugin.getDb().hasPlayerAchievement(player,
										plugin.getConfig().getString("DistanceBoat." + achievementDistance + ".Name")))

							awardDistanceAchievement(player, achievementDistance, "DistanceBoat.");
					}
				}
			} else {
				achievementDistancesFoot.put(player,
						(long) (achievementDistancesFoot.get(player) + achievementLocations.get(player)
								.distanceSquared(player.getLocation())));

				for (int achievementDistance : achievementsFoot) {
					if (achievementDistancesFoot.get(player) > achievementDistance * achievementDistance
							&& !plugin.getDb().hasPlayerAchievement(player,
									plugin.getConfig().getString("DistanceFoot." + achievementDistance + ".Name")))

						awardDistanceAchievement(player, achievementDistance, "DistanceFoot.");
				}
			}
			achievementLocations.put(player, player.getLocation());
		}
	}

	public void awardDistanceAchievement(Player player, int achievementDistance, String type) {

		plugin.getAchievementDisplay().displayAchievement(player, type + achievementDistance);
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		plugin.getDb().registerAchievement(player, plugin.getConfig().getString(type + achievementDistance + ".Name"),
				plugin.getConfig().getString(type + achievementDistance + ".Message"), format.format(new Date()));
		plugin.getReward().checkConfig(player, type + achievementDistance);
	}

	public static HashMap<Player, Long> getAchievementDistancesFoot() {

		return achievementDistancesFoot;
	}

	public static HashMap<Player, Long> getAchievementDistancesHorse() {

		return achievementDistancesHorse;
	}

	public static HashMap<Player, Long> getAchievementDistancesPig() {

		return achievementDistancesPig;
	}

	public static HashMap<Player, Long> getAchievementDistancesBoat() {

		return achievementDistancesBoat;
	}

	public static HashMap<Player, Long> getAchievementDistancesMinecart() {

		return achievementDistancesMinecart;
	}

	public static HashMap<Player, Location> getAchievementLocations() {

		return achievementLocations;
	}

}
