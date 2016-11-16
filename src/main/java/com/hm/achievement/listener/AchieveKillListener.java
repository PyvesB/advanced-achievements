package com.hm.achievement.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;

/**
 * Listener class to deal with Kills achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveKillListener extends AbstractListener implements Listener {

	public AchieveKillListener(AdvancedAchievements plugin) {

		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event) {

		Player player = event.getEntity().getKiller();

		if (player == null)
			return;

		if (plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE || plugin.isInExludedWorld(player))
			return;
		Entity entity = event.getEntity();

		if (!(entity instanceof LivingEntity)) {
			return;
		}

		String mobName;
		if (entity instanceof Player) {
			mobName = "player";
		} else {
			mobName = entity.getType().name().toLowerCase();
			if (entity instanceof Creeper) {
				Creeper c = (Creeper) entity;
				if (c.isPowered()) {
					mobName = "poweredcreeper";
				}
			}
		}

		MultipleAchievements category = MultipleAchievements.KILLS;

		if (!plugin.getPluginConfig().isConfigurationSection(category + "." + mobName)
				|| !player.hasPermission(category.toPermName() + '.' + mobName))
			return;

		updateStatisticAndAwardAchievementsIfAvailable(player, category, mobName, 1);
	}
}
