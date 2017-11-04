package com.hm.achievement.listener;

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

/**
 * Listener class to deal with HoePlowings, Fertilising, Fireworks and MusicDiscs achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveHoeFertiliseFireworkMusicListener extends AbstractRateLimitedListener {

	public AchieveHoeFertiliseFireworkMusicListener(AdvancedAchievements plugin) {
		super(plugin);
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
		} else if (event.getItem().isSimilar(new ItemStack(Material.INK_SACK, 1, (short) 15))
				&& canBeFertilised(clickedMaterial, event.getClickedBlock())) {
			category = NormalAchievements.FERTILISING;
		} else if (event.getItem().getType() == Material.FIREWORK && canAccommodateFireworkLaunch(clickedMaterial)) {
			category = NormalAchievements.FIREWORKS;
		} else if (event.getItem().getType().isRecord() && clickedMaterial == Material.JUKEBOX) {
			category = NormalAchievements.MUSICDISCS;
		} else {
			return;
		}

		if (plugin.getDisabledCategorySet().contains(category.toString())) {
			return;
		}

		if (!shouldIncreaseBeTakenIntoAccount(player, category)
				|| category == NormalAchievements.MUSICDISCS && isInCooldownPeriod(player, true, category)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
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
		short durability = block.getState().getData().toItemStack().getDurability();
		if (clickedMaterial == Material.DOUBLE_PLANT) {
			if (durability == 10) {
				// Upper part of double plant. We must look at the lower part to get the double plant type.
				durability = block.getRelative(BlockFace.DOWN).getState().getData().toItemStack().getDurability();
			}
			// Fertilisation does not work on double tallgrass and large fern.
			return durability != 2 && durability != 3;
		}
		return clickedMaterial == Material.GRASS || clickedMaterial == Material.SAPLING
				|| clickedMaterial == Material.POTATO && durability < 7
				|| clickedMaterial == Material.CARROT && durability < 7
				|| clickedMaterial == Material.CROPS && durability < 7
				|| clickedMaterial == Material.PUMPKIN_STEM && durability < 7
				|| clickedMaterial == Material.MELON_STEM && durability < 7
				|| clickedMaterial == Material.BROWN_MUSHROOM || clickedMaterial == Material.RED_MUSHROOM
				|| clickedMaterial == Material.COCOA && durability < 9 || clickedMaterial == Material.LONG_GRASS
				|| (version >= 9 && clickedMaterial == Material.BEETROOT_BLOCK && durability < 3);
	}

	/**
	 * Determines whether a firework can be launched when interacting with this block.
	 * 
	 * @param clickedMaterial
	 * @return true if the material can be used to launch a firework, false otherwise
	 */
	private boolean canAccommodateFireworkLaunch(Material clickedMaterial) {
		switch (clickedMaterial) {
			case WORKBENCH:
			case FURNACE:
			case BURNING_FURNACE:
			case DISPENSER:
			case CHEST:
			case NOTE_BLOCK:
			case LEVER:
			case STONE_BUTTON:
			case WOOD_BUTTON:
			case TRAP_DOOR:
			case FENCE_GATE:
			case ENCHANTMENT_TABLE:
			case ENDER_CHEST:
			case BEACON:
			case ANVIL:
			case TRAPPED_CHEST:
			case HOPPER:
			case DROPPER:
			case WOOD_DOOR:
			case WOODEN_DOOR:
			case PAINTING:
			case MINECART:
			case HOPPER_MINECART:
			case EXPLOSIVE_MINECART:
			case COMMAND_MINECART:
			case POWERED_MINECART:
			case STORAGE_MINECART:
			case BOAT:
			case BED_BLOCK:
			case BREWING_STAND:
			case CAKE_BLOCK:
			case ITEM_FRAME:
			case REDSTONE_COMPARATOR_OFF:
			case REDSTONE_COMPARATOR_ON:
			case DIODE_BLOCK_OFF:
			case DIODE_BLOCK_ON:
			case COMMAND:
				return false;
			default:
				break;
		}

		if (version >= 8) {
			switch (clickedMaterial) {
				case ACACIA_FENCE_GATE:
				case BIRCH_FENCE_GATE:
				case DARK_OAK_FENCE_GATE:
				case JUNGLE_FENCE_GATE:
				case SPRUCE_FENCE_GATE:
				case ACACIA_DOOR:
				case BIRCH_DOOR:
				case DARK_OAK_DOOR:
				case JUNGLE_DOOR:
				case SPRUCE_DOOR:
				case ARMOR_STAND:
					return false;
				default:
					break;
			}
		}

		if (version >= 9) {
			switch (clickedMaterial) {
				case BOAT_ACACIA:
				case BOAT_BIRCH:
				case BOAT_DARK_OAK:
				case BOAT_JUNGLE:
				case BOAT_SPRUCE:
				case COMMAND_REPEATING:
				case COMMAND_CHAIN:
					return false;
				default:
					break;
			}
		}

		if (version >= 11) {
			switch (clickedMaterial) {
				case BLACK_SHULKER_BOX:
				case BLUE_SHULKER_BOX:
				case BROWN_SHULKER_BOX:
				case CYAN_SHULKER_BOX:
				case GRAY_SHULKER_BOX:
				case GREEN_SHULKER_BOX:
				case LIGHT_BLUE_SHULKER_BOX:
				case LIME_SHULKER_BOX:
				case MAGENTA_SHULKER_BOX:
				case ORANGE_SHULKER_BOX:
				case PINK_SHULKER_BOX:
				case PURPLE_SHULKER_BOX:
				case RED_SHULKER_BOX:
				case SILVER_SHULKER_BOX:
				case WHITE_SHULKER_BOX:
				case YELLOW_SHULKER_BOX:
					return false;
				default:
					break;
			}
		}
		return true;
	}
}
