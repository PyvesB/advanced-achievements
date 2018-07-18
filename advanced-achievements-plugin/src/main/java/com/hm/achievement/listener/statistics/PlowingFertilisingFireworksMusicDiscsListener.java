package com.hm.achievement.listener.statistics;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with HoePlowings, Fertilising, Fireworks and MusicDiscs achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class PlowingFertilisingFireworksMusicDiscsListener extends AbstractRateLimitedListener {

	private final Set<String> disabledCategories;

	@Inject
	public PlowingFertilisingFireworksMusicDiscsListener(@Named("main") CommentedYamlConfiguration mainConfig,
			int serverVersion, Map<String, List<Long>> sortedThresholds, CacheManager cacheManager,
			RewardParser rewardParser, AdvancedAchievements advancedAchievements,
			@Named("lang") CommentedYamlConfiguration langConfig, Logger logger, Set<String> disabledCategories) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser, advancedAchievements, langConfig,
				logger);
		this.disabledCategories = disabledCategories;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getItem() == null) {
			return;
		}

		Player player = event.getPlayer();
		NormalAchievements category;

		Material clickedMaterial = event.getClickedBlock().getType();
		if (event.getItem().getType().name().contains("HOE")
				&& (clickedMaterial == Material.GRASS || clickedMaterial == Material.DIRT)
				&& event.getClickedBlock().getRelative(BlockFace.UP).getType() == Material.AIR) {
			category = NormalAchievements.HOEPLOWING;
		} else if (isBoneMeal(event.getItem()) && canBeFertilised(clickedMaterial, event.getClickedBlock())) {
			category = NormalAchievements.FERTILISING;
		} else if (isFirework(event.getItem().getType()) && canAccommodateFireworkLaunch(clickedMaterial)) {
			category = NormalAchievements.FIREWORKS;
		} else if (event.getItem().getType().isRecord() && clickedMaterial == Material.JUKEBOX) {
			category = NormalAchievements.MUSICDISCS;
		} else {
			return;
		}

		if (disabledCategories.contains(category.toString())) {
			return;
		}

		if (!shouldIncreaseBeTakenIntoAccount(player, category)
				|| category == NormalAchievements.MUSICDISCS && isInCooldownPeriod(player, true, category)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}

	/**
	 * Determines whether the used item is bone meal.
	 * 
	 * @param itemStack
	 * @return true if the item is bone meal, false otherwise
	 */
	private boolean isBoneMeal(ItemStack itemStack) {
		return serverVersion >= 13 ? itemStack.getType() == Material.BONE_MEAL
				: itemStack.isSimilar(new ItemStack(Material.valueOf("INK_SACK"), 1, (short) 15));
	}

	/**
	 * Determines whether clickedMaterial can be fertilised.
	 * 
	 * @param clickedMaterial
	 * @param block
	 * @return true if the block can be fertilised, false otherwise
	 */
	@SuppressWarnings("deprecation")
	private boolean canBeFertilised(Material clickedMaterial, Block block) {
		short durability = block.getState().getData().toItemStack(0).getDurability();
		if (clickedMaterial.name().equals("DOUBLE_PLANT")) {
			if (durability == 10) {
				// Upper part of double plant. We must look at the lower part to get the double plant type.
				durability = block.getRelative(BlockFace.DOWN).getState().getData().toItemStack(0).getDurability();
			}
			// Fertilisation does not work on double tallgrass and large fern.
			return durability != 2 && durability != 3;
		}
		return clickedMaterial == Material.GRASS || clickedMaterial.name().endsWith("SAPLING")
				|| clickedMaterial == Material.POTATO && durability < 7
				|| clickedMaterial == Material.CARROT && durability < 7
				|| clickedMaterial.name().equals("CROPS") && durability < 7
				|| clickedMaterial == Material.PUMPKIN_STEM && durability < 7
				|| clickedMaterial == Material.MELON_STEM && durability < 7 || clickedMaterial == Material.BROWN_MUSHROOM
				|| clickedMaterial == Material.RED_MUSHROOM || clickedMaterial == Material.COCOA && durability < 9
				|| serverVersion >= 9 && clickedMaterial.name().equals("BEETROOT_BLOCK") && durability < 3
				|| serverVersion >= 13 && (clickedMaterial == Material.FARMLAND && durability < 7
						|| clickedMaterial == Material.BEETROOTS && durability < 3
						|| clickedMaterial == Material.SUNFLOWER || clickedMaterial == Material.LILAC
						|| clickedMaterial == Material.ROSE_BUSH || clickedMaterial == Material.PEONY);
	}

	/**
	 * Determines whether the used material is a firework.
	 * 
	 * @param material
	 * @return true if the material is a firework, false otherwise
	 */
	private boolean isFirework(Material material) {
		return serverVersion >= 13 ? material == Material.FIREWORK_ROCKET : material.name().equals("FIREWORK");
	}

	/**
	 * Determines whether a firework can be launched when interacting with this block.
	 * 
	 * @param clickedMaterialName
	 * @return true if the material can be used to launch a firework, false otherwise
	 */
	private boolean canAccommodateFireworkLaunch(Material clickedMaterialName) {
		switch (clickedMaterialName.name()) {
			case "FURNACE":
			case "DISPENSER":
			case "CHEST":
			case "NOTE_BLOCK":
			case "LEVER":
			case "STONE_BUTTON":
			case "ENDER_CHEST":
			case "BEACON":
			case "ANVIL":
			case "TRAPPED_CHEST":
			case "HOPPER":
			case "DROPPER":
			case "PAINTING":
			case "MINECART":
			case "HOPPER_MINECART":
			case "BREWING_STAND":
			case "ITEM_FRAME":
			case "CRAFTING_TABLE":
			case "ACACIA_BUTTON":
			case "BIRCH_BUTTON":
			case "DARK_OAK_BUTTON":
			case "JUNGLE_BUTTON":
			case "OAK_BUTTON":
			case "SPRUCE_BUTTON":
			case "ACACIA_DOOR":
			case "BIRCH_DOOR":
			case "DARK_OAK_DOOR":
			case "JUNGLE_DOOR":
			case "OAK_DOOR":
			case "SPRUCE_DOOR":
			case "ACACIA_FENCE_GATE":
			case "BIRCH_FENCE_GATE":
			case "DARK_OAK_FENCE_GATE":
			case "JUNGLE_FENCE_GATE":
			case "OAK_FENCE_GATE":
			case "SPRUCE_FENCE_GATE":
			case "ENCHANTING_TABLE":
			case "ACACIA_TRAPDOOR":
			case "BIRCH_TRAPDOOR":
			case "DARK_OAK_TRAPDOOR":
			case "JUNGLE_TRAPDOOR":
			case "OAK_TRAPDOOR":
			case "SPRUCE_TRAPDOOR":
			case "TNT_MINECART":
			case "COMMAND_BLOCK_MINECART":
			case "FURNACE_MINECART":
			case "CHEST_MINECART":
			case "ACACIA_BOAT":
			case "BIRCH_BOAT":
			case "DARK_OAK_BOAT":
			case "JUNGLE_BOAT":
			case "OAK_BOAT":
			case "SPRUCE_BOAT":
			case "BLACK_BED":
			case "BLUE_BED":
			case "BROWN_BED":
			case "CYAN_BED":
			case "GRAY_BED":
			case "GREEN_BED":
			case "LIGHT_BLUE_BED":
			case "LIGHT_GRAY_BED":
			case "LIME_BED":
			case "MAGENTA_BED":
			case "ORANGE_BED":
			case "PINK_BED":
			case "PURPLE_BED":
			case "RED_BED":
			case "WHITE_BED":
			case "YELLOW_BED":
			case "CAKE":
			case "COMPARATOR":
			case "REPEATER":
			case "COMMAND_BLOCK":
			case "ARMOR_STAND":
			case "CHAIN_COMMAND_BLOCK":
			case "REPEATING_COMMAND_BLOCK":
			case "BLACK_SHULKER_BOX":
			case "BLUE_SHULKER_BOX":
			case "BROWN_SHULKER_BOX":
			case "CYAN_SHULKER_BOX":
			case "GRAY_SHULKER_BOX":
			case "GREEN_SHULKER_BOX":
			case "LIGHT_BLUE_SHULKER_BOX":
			case "LIME_SHULKER_BOX":
			case "MAGENTA_SHULKER_BOX":
			case "ORANGE_SHULKER_BOX":
			case "PINK_SHULKER_BOX":
			case "PURPLE_SHULKER_BOX":
			case "RED_SHULKER_BOX":
			case "WHITE_SHULKER_BOX":
			case "YELLOW_SHULKER_BOX":
			case "SHULKER_BOX":
				// Pre Minecraft 1.13":
			case "WORKBENCH":
			case "BURNING_FURNACE":
			case "WOOD_BUTTON":
			case "TRAP_DOOR":
			case "FENCE_GATE":
			case "ENCHANTMENT_TABLE":
			case "WOOD_DOOR":
			case "WOODEN_DOOR":
			case "EXPLOSIVE_MINECART":
			case "COMMAND_MINECART":
			case "POWERED_MINECART":
			case "STORAGE_MINECART":
			case "BOAT":
			case "BED_BLOCK":
			case "CAKE_BLOCK":
			case "REDSTONE_COMPARATOR_OFF":
			case "REDSTONE_COMPARATOR_ON":
			case "DIODE_BLOCK_OFF":
			case "DIODE_BLOCK_ON":
			case "COMMAND":
			case "BOAT_ACACIA":
			case "BOAT_BIRCH":
			case "BOAT_DARK_OAK":
			case "BOAT_JUNGLE":
			case "BOAT_SPRUCE":
			case "COMMAND_REPEATING":
			case "COMMAND_CHAIN":
			case "SILVER_SHULKER_BOX":
				return false;
			default:
				return true;
		}
	}
}
