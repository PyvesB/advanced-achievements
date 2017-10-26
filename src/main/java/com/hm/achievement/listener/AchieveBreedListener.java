package com.hm.achievement.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityBreedEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;

/**
 * Listener class to deal with Breeding achievements.
 */
public class AchieveBreedListener extends AbstractListener {

	public AchieveBreedListener(AdvancedAchievements plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityBreed(EntityBreedEvent event) {
		if (!(event.getBreeder() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getBreeder();

		if (player == null) {
			return;
		}

		if (!shouldIncreaseBeTakenIntoAccountNoPermissions(player)) {
			return;
		}

		Entity entity = event.getMother();

		if (!(entity instanceof LivingEntity)) {
			return;
		}

		String mobName = entity.getType().name().toLowerCase();

		MultipleAchievements category = MultipleAchievements.BREEDING;

		if (plugin.getPluginConfig().isConfigurationSection(category + "." + mobName)
				&& player.hasPermission(category.toPermName() + '.' + mobName)) {
			updateStatisticAndAwardAchievementsIfAvailable(player, category, mobName, 1);
		}
	}
}
