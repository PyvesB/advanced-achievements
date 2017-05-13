package com.hm.achievement.runnable;

import java.util.HashMap;
import java.util.Map;
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
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent.PlayerAdvancedAchievementEventBuilder;

/**
 * Class used to monitor distances travelled by players for the different available categories.
 * 
 * @author Pyves
 *
 */
public class AchieveDistanceRunnable extends AbstractRunnable implements Cleanable, Runnable {

	private final Map<String, Location> playerLocations;

	private boolean configIgnoreVerticalDistance;

	public AchieveDistanceRunnable(AdvancedAchievements plugin) {
		super(plugin);

		playerLocations = new HashMap<>();
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configIgnoreVerticalDistance = plugin.getPluginConfig().getBoolean("IgnoreVerticalDistance", false);
	}

	@Override
	public void cleanPlayerData(UUID uuid) {
		playerLocations.remove(uuid.toString());
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
	private void refreshDistance(Player player) {
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
			updateDistanceAndCheckAchievements(difference, player, NormalAchievements.DISTANCEHORSE);
		} else if (player.getVehicle() instanceof Pig) {
			updateDistanceAndCheckAchievements(difference, player, NormalAchievements.DISTANCEPIG);
		} else if (player.getVehicle() instanceof Minecart) {
			updateDistanceAndCheckAchievements(difference, player, NormalAchievements.DISTANCEMINECART);
		} else if (player.getVehicle() instanceof Boat) {
			updateDistanceAndCheckAchievements(difference, player, NormalAchievements.DISTANCEBOAT);
		} else if (version >= 11 && player.getVehicle() instanceof Llama) {
			updateDistanceAndCheckAchievements(difference, player, NormalAchievements.DISTANCELLAMA);
		} else if (!player.isFlying() && (version < 9 || !player.isGliding())) {
			updateDistanceAndCheckAchievements(difference, player, NormalAchievements.DISTANCEFOOT);
		} else if (version >= 9 && player.isGliding()) {
			updateDistanceAndCheckAchievements(difference, player, NormalAchievements.DISTANCEGLIDING);
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
	 * Compares the distance travelled to the achievement thresholds. If a threshold is reached, awards the achievement.
	 * Updates the various tracking objects.
	 * 
	 * @param difference
	 * @param player
	 * @param category
	 */
	private void updateDistanceAndCheckAchievements(int difference, Player player, NormalAchievements category) {
		if (!player.hasPermission(category.toPermName())
				|| plugin.getDisabledCategorySet().contains(category.toString())) {
			return;
		}

		long distance = plugin.getCacheManager().getAndIncrementStatisticAmount(category, player.getUniqueId(),
				difference);

		// Iterate through all the different achievements.
		for (String achievementThreshold : plugin.getPluginConfig().getConfigurationSection(category.toString())
				.getKeys(false)) {
			long threshold = Long.parseLong(achievementThreshold);
			String achievementName = plugin.getPluginConfig()
					.getString(category + "." + achievementThreshold + ".Name");

			// Check whether player has met the threshold and whether we he has not yet received the achievement.
			if (distance > threshold
					&& !plugin.getCacheManager().hasPlayerAchievement(player.getUniqueId(), achievementName)) {
				String configAchievement = category.toString() + "." + achievementThreshold;
				// Fire achievement event.
				PlayerAdvancedAchievementEventBuilder playerAdvancedAchievementEventBuilder = new PlayerAdvancedAchievementEventBuilder()
						.player(player).name(plugin.getPluginConfig().getString(configAchievement + ".Name"))
						.displayName(plugin.getPluginConfig().getString(configAchievement + ".DisplayName"))
						.message(plugin.getPluginConfig().getString(configAchievement + ".Message"))
						.commandRewards(plugin.getRewardParser().getCommandRewards(configAchievement, player))
						.itemReward(plugin.getRewardParser().getItemReward(configAchievement))
						.moneyReward(plugin.getRewardParser().getMoneyAmount(configAchievement));

				Bukkit.getServer().getPluginManager().callEvent(playerAdvancedAchievementEventBuilder.build());
			}
		}
	}

	public Map<String, Location> getPlayerLocations() {
		return playerLocations;
	}
}
