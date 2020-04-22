package com.hm.achievement.listener.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.InventoryHelper;
import com.hm.achievement.utils.MaterialHelper;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Listener class to deal with Brewing achievements.
 *
 * @author Pyves
 *
 */
@Singleton
public class BrewingListener extends AbstractRateLimitedListener {

	private final MaterialHelper materialHelper;
	private final InventoryHelper inventoryHelper;

	@Inject
	public BrewingListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser,
			AdvancedAchievements advancedAchievements, @Named("lang") CommentedYamlConfiguration langConfig, Logger logger,
			MaterialHelper materialHelper, InventoryHelper inventoryHelper) {
		super(NormalAchievements.BREWING, mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser,
				advancedAchievements, langConfig, logger);
		this.materialHelper = materialHelper;
		this.inventoryHelper = inventoryHelper;
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBrew(BrewEvent event) {
		for(HumanEntity he : event.getContents().getViewers()){
			if(he instanceof Player){
				Player p = (Player)he;
				BrewerInventory inv = event.getContents();
				for (int i = 0; i <= 2; ++i) {
					if(inv.getItem(i) == null) continue;
					Integer amount = inv.getItem(i).getAmount();
					updateStatisticAndAwardAchievementsIfAvailable(p, amount, i);
				}
			}
		}
	}

	/**
	 * Determine whether the event corresponds to a brewable potion, i.e. not water.
	 *
	 * @param event
	 * @return true if for any brewable potion
	 */
	private boolean isBrewablePotion(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();
		return item != null &&
				(materialHelper.isAnyPotionButWater(item) || serverVersion >= 9 && item.getType() == Material.SPLASH_POTION);
	}
}
