package com.hm.achievement.db;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class used to provide a cache wrapper for the database statistics, in order to reduce load of database and enable
 * more modularity for the user.
 * 
 * @author Pyves
 *
 */
public class DatabasePoolsManager {

	private AdvancedAchievements plugin;

	// Statistics of the different players for normal achievements; keys correspond to UUIDs.
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
	private Map<String, Integer> musicDiscHashMap;
	private Map<String, Integer> enderPearlHashMap;
	private Map<String, Integer> distanceFootHashMap;
	private Map<String, Integer> distanceHorseHashMap;
	private Map<String, Integer> distancePigHashMap;
	private Map<String, Integer> distanceBoatHashMap;
	private Map<String, Integer> distanceMinecartHashMap;
	private Map<String, Integer> distanceGlidingHashMap;
	private Map<String, Long> playTimeHashMap;

	// Statistics of the different players for multiple achievements; keys correspond to concatenated UUIDs and
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
			deathHashMap = new ConcurrentHashMap<>();
			arrowHashMap = new ConcurrentHashMap<>();
			snowballHashMap = new ConcurrentHashMap<>();
			eggHashMap = new ConcurrentHashMap<>();
			fishHashMap = new ConcurrentHashMap<>();
			itemBreakHashMap = new ConcurrentHashMap<>();
			eatenItemHashMap = new ConcurrentHashMap<>();
			shearHashMap = new ConcurrentHashMap<>();
			milkHashMap = new ConcurrentHashMap<>();
			tradeHashMap = new ConcurrentHashMap<>();
			anvilHashMap = new ConcurrentHashMap<>();
			enchantmentHashMap = new ConcurrentHashMap<>();
			bedHashMap = new ConcurrentHashMap<>();
			xpHashMap = new ConcurrentHashMap<>();
			consumedPotionHashMap = new ConcurrentHashMap<>();
			dropHashMap = new ConcurrentHashMap<>();
			hoePlowingHashMap = new ConcurrentHashMap<>();
			fertiliseHashMap = new ConcurrentHashMap<>();
			tameHashMap = new ConcurrentHashMap<>();
			brewingHashMap = new ConcurrentHashMap<>();
			fireworkHashMap = new ConcurrentHashMap<>();
			musicDiscHashMap = new ConcurrentHashMap<>();
			enderPearlHashMap = new ConcurrentHashMap<>();
			blockPlaceHashMap = new ConcurrentHashMap<>();
			blockBreakHashMap = new ConcurrentHashMap<>();
			killHashMap = new ConcurrentHashMap<>();
			craftHashMap = new ConcurrentHashMap<>();
			distanceFootHashMap = new ConcurrentHashMap<>();
			distanceHorseHashMap = new ConcurrentHashMap<>();
			distancePigHashMap = new ConcurrentHashMap<>();
			distanceBoatHashMap = new ConcurrentHashMap<>();
			distanceMinecartHashMap = new ConcurrentHashMap<>();
			distanceGlidingHashMap = new ConcurrentHashMap<>();
			playTimeHashMap = new ConcurrentHashMap<>();
		} else {
			deathHashMap = new HashMap<>();
			arrowHashMap = new HashMap<>();
			snowballHashMap = new HashMap<>();
			eggHashMap = new HashMap<>();
			fishHashMap = new HashMap<>();
			itemBreakHashMap = new HashMap<>();
			eatenItemHashMap = new HashMap<>();
			shearHashMap = new HashMap<>();
			milkHashMap = new HashMap<>();
			tradeHashMap = new HashMap<>();
			anvilHashMap = new HashMap<>();
			enchantmentHashMap = new HashMap<>();
			bedHashMap = new HashMap<>();
			xpHashMap = new HashMap<>();
			consumedPotionHashMap = new HashMap<>();
			dropHashMap = new HashMap<>();
			hoePlowingHashMap = new HashMap<>();
			fertiliseHashMap = new HashMap<>();
			tameHashMap = new HashMap<>();
			brewingHashMap = new HashMap<>();
			fireworkHashMap = new HashMap<>();
			enderPearlHashMap = new HashMap<>();
			musicDiscHashMap = new HashMap<>();
			blockPlaceHashMap = new HashMap<>();
			blockBreakHashMap = new HashMap<>();
			killHashMap = new HashMap<>();
			craftHashMap = new HashMap<>();
			distanceFootHashMap = new HashMap<>();
			distanceHorseHashMap = new HashMap<>();
			distancePigHashMap = new HashMap<>();
			distanceBoatHashMap = new HashMap<>();
			distanceMinecartHashMap = new HashMap<>();
			distanceGlidingHashMap = new HashMap<>();
			playTimeHashMap = new HashMap<>();
		}
	}

	public Map<String, Integer> getDeathHashMap() {

		return deathHashMap;
	}

	public int getPlayerDeathAmount(Player player) {

		Integer amount = deathHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "deaths");
		else
			return amount;
	}

	public Map<String, Integer> getArrowHashMap() {

		return arrowHashMap;
	}

	public int getPlayerArrowAmount(Player player) {

		Integer amount = arrowHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "arrows");
		else
			return amount;
	}

	public Map<String, Integer> getSnowballHashMap() {

		return snowballHashMap;
	}

	public int getPlayerSnowballAmount(Player player) {

		Integer amount = snowballHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "snowballs");
		else
			return amount;
	}

	public Map<String, Integer> getEggHashMap() {

		return eggHashMap;
	}

	public int getPlayerEggAmount(Player player) {

		Integer amount = eggHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "eggs");
		else
			return amount;
	}

	public Map<String, Integer> getFishHashMap() {

		return fishHashMap;
	}

	public int getPlayerFishAmount(Player player) {

		Integer amount = fishHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "fish");
		else
			return amount;
	}

	public Map<String, Integer> getItemBreakHashMap() {

		return itemBreakHashMap;
	}

	public int getPlayerItemBreakAmount(Player player) {

		Integer amount = itemBreakHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "itembreaks");
		else
			return amount;
	}

	public Map<String, Integer> getEatenItemsHashMap() {

		return eatenItemHashMap;
	}

	public int getPlayerEatenItemAmount(Player player) {

		Integer amount = eatenItemHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "eatenitems");
		else
			return amount;
	}

	public Map<String, Integer> getShearHashMap() {

		return shearHashMap;
	}

	public int getPlayerShearAmount(Player player) {

		Integer amount = shearHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "shears");
		else
			return amount;
	}

	public Map<String, Integer> getMilkHashMap() {

		return milkHashMap;
	}

	public int getPlayerMilkAmount(Player player) {

		Integer amount = milkHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "milks");
		else
			return amount;
	}

	public Map<String, Integer> getTradeHashMap() {

		return tradeHashMap;
	}

	public int getPlayerTradeAmount(Player player) {

		Integer amount = tradeHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "trades");
		else
			return amount;
	}

	public Map<String, Integer> getAnvilHashMap() {

		return anvilHashMap;
	}

	public int getPlayerAnvilAmount(Player player) {

		Integer amount = anvilHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "anvils");
		else
			return amount;
	}

	public Map<String, Integer> getEnchantmentHashMap() {

		return enchantmentHashMap;
	}

	public int getPlayerEnchantmentAmount(Player player) {

		Integer amount = enchantmentHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "enchantments");
		else
			return amount;
	}

	public Map<String, Integer> getBedHashMap() {

		return bedHashMap;
	}

	public int getPlayerBedAmount(Player player) {

		Integer amount = bedHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "beds");
		else
			return amount;
	}

	public Map<String, Integer> getXpHashMap() {

		return xpHashMap;
	}

	public int getPlayerXPAmount(Player player) {

		Integer amount = xpHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "levels");
		else
			return amount;
	}

	public Map<String, Integer> getConsumedPotionsHashMap() {

		return consumedPotionHashMap;
	}

	public int getPlayerConsumedPotionAmount(Player player) {

		Integer amount = consumedPotionHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "consumedpotions");
		else
			return amount;
	}

	public Map<String, Integer> getDropHashMap() {

		return dropHashMap;
	}

	public int getPlayerDropAmount(Player player) {

		Integer amount = dropHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "drops");
		else
			return amount;
	}

	public Map<String, Integer> getHoePlowingHashMap() {

		return hoePlowingHashMap;
	}

	public int getPlayerHoePlowingAmount(Player player) {

		Integer amount = hoePlowingHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "hoeplowing");
		else
			return amount;
	}

	public Map<String, Integer> getFertiliseHashMap() {

		return fertiliseHashMap;
	}

	public int getPlayerFertiliseAmount(Player player) {

		Integer amount = fertiliseHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "fertilising");
		else
			return amount;
	}

	public Map<String, Integer> getTameHashMap() {

		return tameHashMap;
	}

	public int getPlayerTameAmount(Player player) {

		Integer amount = tameHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "tames");
		else
			return amount;
	}

	public Map<String, Integer> getBrewingHashMap() {

		return brewingHashMap;
	}

	public int getPlayerBrewingAmount(Player player) {

		Integer amount = brewingHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "brewing");
		else
			return amount;
	}

	public Map<String, Integer> getFireworkHashMap() {

		return fireworkHashMap;
	}

	public int getPlayerFireworkAmount(Player player) {

		Integer amount = fireworkHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "fireworks");
		else
			return amount;
	}

	public Map<String, Integer> getMusicDiscHashMap() {

		return musicDiscHashMap;
	}

	public int getPlayerMusicDiscAmount(Player player) {

		Integer amount = musicDiscHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "musicdiscs");
		else
			return amount;
	}

	public Map<String, Integer> getEnderPearlHashMap() {

		return enderPearlHashMap;
	}

	public int getPlayerEnderPearlAmount(Player player) {

		Integer amount = enderPearlHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "enderpearls");
		else
			return amount;
	}

	public Map<String, Integer> getBlockPlaceHashMap() {

		return blockPlaceHashMap;
	}

	public int getPlayerBlockPlaceAmount(Player player, String blockName) {

		// Concatenate player name and block name to put in HashMap.
		Integer amount = blockPlaceHashMap.get(player.getUniqueId().toString() + blockName);
		if (amount == null)
			return plugin.getDb().getPlaces(player, blockName);
		else
			return amount;
	}

	public Map<String, Integer> getBlockBreakHashMap() {

		return blockBreakHashMap;
	}

	public int getPlayerBlockBreakAmount(Player player, String blockName) {

		// Concatenate player name and block name to put in HashMap.
		Integer amount = blockBreakHashMap.get(player.getUniqueId().toString() + blockName);
		if (amount == null)
			return plugin.getDb().getBreaks(player, blockName);
		else
			return amount;
	}

	public Map<String, Integer> getKillHashMap() {

		return killHashMap;
	}

	public int getPlayerKillAmount(Player player, String mobName) {

		// Concatenate player name and mob name to put in HashMap.
		Integer amount = killHashMap.get(player.getUniqueId().toString() + mobName);
		if (amount == null)
			return plugin.getDb().getKills(player, mobName);
		else
			return amount;
	}

	public Map<String, Integer> getCraftHashMap() {

		return craftHashMap;
	}

	public int getPlayerCraftAmount(Player player, String craftName) {

		// Concatenate player name and item name to put in HashMap.
		Integer amount = craftHashMap.get(player.getUniqueId().toString() + craftName);
		if (amount == null)
			return plugin.getDb().getCrafts(player, craftName);
		else
			return amount;
	}
	
	public Map<String, Integer> getDistanceFootHashMap() {

		return distanceFootHashMap;
	}
	
	public int getPlayerDistanceFootAmount(Player player) {

		Integer amount = distanceFootHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "distancefoot");
		else
			return amount;
	}

	public Map<String, Integer> getDistanceHorseHashMap() {

		return distanceHorseHashMap;
	}
	
	public int getPlayerDistanceHorseAmount(Player player) {

		Integer amount = distanceHorseHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "distancehorse");
		else
			return amount;
	}

	public Map<String, Integer> getDistancePigHashMap() {

		return distancePigHashMap;
	}
	
	public int getPlayerDistancePigAmount(Player player) {

		Integer amount = distancePigHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "distancepig");
		else
			return amount;
	}

	public Map<String, Integer> getDistanceMinecartHashMap() {

		return distanceMinecartHashMap;
	}
	
	public int getPlayerDistanceMinecartAmount(Player player) {

		Integer amount = distanceMinecartHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "distanceminecart");
		else
			return amount;
	}

	public Map<String, Integer> getDistanceBoatHashMap() {

		return distanceBoatHashMap;
	}
	
	public int getPlayerDistanceBoatAmount(Player player) {

		Integer amount = distanceBoatHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "distanceboat");
		else
			return amount;
	}

	public Map<String, Integer> getDistanceGlidingHashMap() {

		return distanceGlidingHashMap;
	}
	
	public int getPlayerDistanceGlidingAmount(Player player) {

		Integer amount = distanceGlidingHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getNormalAchievementAmount(player, "distancegliding");
		else
			return amount;
	}

	public Map<String, Long> getPlayedTimeHashMap() {

		return playTimeHashMap;
	}

	public long getPlayerPlayTimeAmount(Player player) {

		Long amount = playTimeHashMap.get(player.getUniqueId().toString());
		if (amount == null)
			return plugin.getDb().getPlaytime(player);
		else
			return amount;
	}

}
