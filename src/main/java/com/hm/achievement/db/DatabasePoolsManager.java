package com.hm.achievement.db;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;

public class DatabasePoolsManager {

	private AdvancedAchievements plugin;

	// Statistics of the different players; keys correspond to UUIDs.
	private Map<String, Integer> deathHashMap;
	private Map<String, Integer> arrowHashMap;
	private Map<String, Integer> snowballHashMap;
	private Map<String, Integer> eggHashMap;
	private Map<String, Integer> fishHashMap;
	private Map<String, Integer> itemBreakHashMap;
	private Map<String, Integer> eatenItemHashMap;
	private Map<String, Integer> shearHashMap;
	private Map<String, Integer> milkHashMap;
	private Map<String, Integer> tradeHashMap;
	private Map<String, Integer> anvilHashMap;
	private Map<String, Integer> enchantmentHashMap;
	private Map<String, Integer> bedHashMap;
	private Map<String, Integer> xpHashMap;
	private Map<String, Integer> consumedPotionHashMap;
	private Map<String, Integer> dropHashMap;
	private Map<String, Integer> hoePlowingHashMap;
	private Map<String, Integer> fertiliseHashMap;
	private Map<String, Integer> tameHashMap;
	private Map<String, Integer> brewingHashMap;
	private Map<String, Integer> fireworkHashMap;
	// Statistics of the different players; keys correspond to UUIDs and
	// block/entity identifiers.
	private Map<String, Integer> blockPlaceHashMap;
	private Map<String, Integer> blockBreakHashMap;
	private Map<String, Integer> killHashMap;
	private Map<String, Integer> craftHashMap;

	public DatabasePoolsManager(AdvancedAchievements plugin) {
		this.plugin = plugin;
	}

	public void databasePoolsInit(boolean isAsync) {

		// If asynchronous task is used, ConcurrentHashMaps are necessary to
		// guarantee thread safety. Otherwise normal HashMaps are enough.
		if (isAsync) {
			deathHashMap = new ConcurrentHashMap<String, Integer>();
			arrowHashMap = new ConcurrentHashMap<String, Integer>();
			snowballHashMap = new ConcurrentHashMap<String, Integer>();
			eggHashMap = new ConcurrentHashMap<String, Integer>();
			fishHashMap = new ConcurrentHashMap<String, Integer>();
			itemBreakHashMap = new ConcurrentHashMap<String, Integer>();
			eatenItemHashMap = new ConcurrentHashMap<String, Integer>();
			shearHashMap = new ConcurrentHashMap<String, Integer>();
			milkHashMap = new ConcurrentHashMap<String, Integer>();
			tradeHashMap = new ConcurrentHashMap<String, Integer>();
			anvilHashMap = new ConcurrentHashMap<String, Integer>();
			enchantmentHashMap = new ConcurrentHashMap<String, Integer>();
			bedHashMap = new ConcurrentHashMap<String, Integer>();
			xpHashMap = new ConcurrentHashMap<String, Integer>();
			consumedPotionHashMap = new ConcurrentHashMap<String, Integer>();
			dropHashMap = new ConcurrentHashMap<String, Integer>();
			hoePlowingHashMap = new ConcurrentHashMap<String, Integer>();
			fertiliseHashMap = new ConcurrentHashMap<String, Integer>();
			tameHashMap = new ConcurrentHashMap<String, Integer>();
			brewingHashMap = new ConcurrentHashMap<String, Integer>();
			fireworkHashMap = new ConcurrentHashMap<String, Integer>();
			blockPlaceHashMap = new ConcurrentHashMap<String, Integer>();
			blockBreakHashMap = new ConcurrentHashMap<String, Integer>();
			killHashMap = new ConcurrentHashMap<String, Integer>();
			craftHashMap = new ConcurrentHashMap<String, Integer>();
		} else {
			deathHashMap = new HashMap<String, Integer>();
			arrowHashMap = new HashMap<String, Integer>();
			snowballHashMap = new HashMap<String, Integer>();
			eggHashMap = new HashMap<String, Integer>();
			fishHashMap = new HashMap<String, Integer>();
			itemBreakHashMap = new HashMap<String, Integer>();
			eatenItemHashMap = new HashMap<String, Integer>();
			shearHashMap = new HashMap<String, Integer>();
			milkHashMap = new HashMap<String, Integer>();
			tradeHashMap = new HashMap<String, Integer>();
			anvilHashMap = new HashMap<String, Integer>();
			enchantmentHashMap = new HashMap<String, Integer>();
			bedHashMap = new HashMap<String, Integer>();
			xpHashMap = new HashMap<String, Integer>();
			consumedPotionHashMap = new HashMap<String, Integer>();
			dropHashMap = new HashMap<String, Integer>();
			hoePlowingHashMap = new HashMap<String, Integer>();
			fertiliseHashMap = new HashMap<String, Integer>();
			tameHashMap = new HashMap<String, Integer>();
			brewingHashMap = new HashMap<String, Integer>();
			fireworkHashMap = new HashMap<String, Integer>();
			blockPlaceHashMap = new HashMap<String, Integer>();
			blockBreakHashMap = new HashMap<String, Integer>();
			killHashMap = new HashMap<String, Integer>();
			craftHashMap = new HashMap<String, Integer>();
		}
	}

	public Map<String, Integer> getDeathHashMap() {

		return deathHashMap;
	}

	public int getPlayerDeathAmount(Player player) {

		if (!deathHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "deaths");
		else
			return deathHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getArrowHashMap() {

		return arrowHashMap;
	}

	public int getPlayerArrowAmount(Player player) {

		if (!arrowHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "arrows");
		else
			return arrowHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getSnowballHashMap() {

		return snowballHashMap;
	}

	public int getPlayerSnowballAmount(Player player) {

		if (!snowballHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "snowballs");
		else
			return snowballHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getEggHashMap() {

		return eggHashMap;
	}

	public int getPlayerEggAmount(Player player) {

		if (!eggHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "eggs");
		else
			return eggHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getFishHashMap() {

		return fishHashMap;
	}

	public int getPlayerFishAmount(Player player) {

		if (!fishHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "fish");
		else
			return fishHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getItemBreakHashMap() {

		return itemBreakHashMap;
	}

	public int getPlayerItemBreakAmount(Player player) {

		if (!itemBreakHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "itembreaks");
		else
			return itemBreakHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getEatenItemsHashMap() {

		return eatenItemHashMap;
	}

	public int getPlayerEatenItemAmount(Player player) {

		if (!eatenItemHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "eatenitems");
		else
			return eatenItemHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getShearHashMap() {

		return shearHashMap;
	}

	public int getPlayerShearAmount(Player player) {

		if (!shearHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "shears");
		else
			return shearHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getMilkHashMap() {

		return milkHashMap;
	}

	public int getPlayerMilkAmount(Player player) {

		if (!milkHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "milks");
		else
			return milkHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getTradeHashMap() {

		return tradeHashMap;
	}

	public int getPlayerTradeAmount(Player player) {

		if (!tradeHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "trades");
		else
			return tradeHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getAnvilHashMap() {

		return anvilHashMap;
	}

	public int getPlayerAnvilAmount(Player player) {

		if (!anvilHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "anvils");
		else
			return anvilHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getEnchantmentHashMap() {

		return enchantmentHashMap;
	}

	public int getPlayerEnchantmentAmount(Player player) {

		if (!enchantmentHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "enchantments");
		else
			return enchantmentHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getBedHashMap() {

		return bedHashMap;
	}

	public int getPlayerBedAmount(Player player) {

		if (!bedHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "beds");
		else
			return bedHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getXpHashMap() {

		return xpHashMap;
	}

	public int getPlayerXPAmount(Player player) {

		if (!xpHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "levels");
		else
			return xpHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getConsumedPotionsHashMap() {

		return consumedPotionHashMap;
	}

	public int getPlayerConsumedPotionAmount(Player player) {

		if (!consumedPotionHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "consumedpotions");
		else
			return consumedPotionHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getDropHashMap() {

		return dropHashMap;
	}

	public int getPlayerDropAmount(Player player) {

		if (!dropHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "drops");
		else
			return dropHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getHoePlowingHashMap() {

		return hoePlowingHashMap;
	}

	public int getPlayerHoePlowingAmount(Player player) {

		if (!hoePlowingHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "hoeplowing");
		else
			return hoePlowingHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getFertiliseHashMap() {

		return fertiliseHashMap;
	}

	public int getPlayerFertiliseAmount(Player player) {

		if (!fertiliseHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "fertilising");
		else
			return fertiliseHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getTameHashMap() {

		return tameHashMap;
	}

	public int getPlayerTameAmount(Player player) {

		if (!tameHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "tames");
		else
			return tameHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getBrewingHashMap() {

		return brewingHashMap;
	}

	public int getPlayerBrewingAmount(Player player) {

		if (!brewingHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "brewing");
		else
			return brewingHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getFireworkHashMap() {

		return fireworkHashMap;
	}

	public int getPlayerFireworkAmount(Player player) {

		if (!fireworkHashMap.containsKey(player.getUniqueId().toString()))
			return plugin.getDb().getNormalAchievementAmount(player, "fireworks");
		else
			return fireworkHashMap.get(player.getUniqueId().toString());
	}

	public Map<String, Integer> getBlockPlaceHashMap() {

		return blockPlaceHashMap;
	}

	public int getPlayerBlockPlaceAmount(Player player, String blockName) {

		// Concatenate player name and block name to put in HashMap.
		if (!blockPlaceHashMap.containsKey(player.getUniqueId().toString() + blockName))
			return plugin.getDb().getPlaces(player, blockName);
		else
			return blockPlaceHashMap.get(player.getUniqueId().toString() + blockName);
	}

	public Map<String, Integer> getBlockBreakHashMap() {

		return blockBreakHashMap;
	}

	public int getPlayerBlockBreakAmount(Player player, String blockName) {

		// Concatenate player name and block name to put in HashMap.
		if (!blockBreakHashMap.containsKey(player.getUniqueId().toString() + blockName))
			return plugin.getDb().getBreaks(player, blockName);
		else
			return blockBreakHashMap.get(player.getUniqueId().toString() + blockName);
	}

	public Map<String, Integer> getKillHashMap() {

		return killHashMap;
	}

	public int getPlayerKillAmount(Player player, String mobName) {

		// Concatenate player name and mob name to put in HashMap.
		if (!killHashMap.containsKey(player.getUniqueId().toString() + mobName))
			return plugin.getDb().getKills(player, mobName);
		else
			return killHashMap.get(player.getUniqueId().toString() + mobName);
	}

	public Map<String, Integer> getCraftHashMap() {

		return craftHashMap;
	}

	public int getPlayerCraftAmount(Player player, String craftName) {

		// Concatenate player name and craft name to put in HashMap.
		if (!craftHashMap.containsKey(player.getUniqueId().toString() + craftName))
			return plugin.getDb().getCrafts(player, craftName);
		else
			return craftHashMap.get(player.getUniqueId().toString() + craftName);
	}

}
