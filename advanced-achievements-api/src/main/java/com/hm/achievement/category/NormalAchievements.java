package com.hm.achievement.category;

import java.util.HashMap;
import java.util.Map;

/**
 * List of standard achievements.
 *
 * @author Pyves
 */
public enum NormalAchievements implements Category {

	CONNECTIONS("Connections"),
	DEATHS("Deaths"),
	ARROWS("Arrows"),
	SNOWBALLS("Snowballs"),
	EGGS("Eggs"),
	FISH("Fish"),
	TREASURES("Treasures"),
	ITEMBREAKS("ItemBreaks"),
	EATENITEMS("EatenItems"),
	SHEARS("Shear"),
	MILKS("Milk"),
	LAVABUCKETS("LavaBuckets"),
	WATERBUCKETS("WaterBuckets"),
	TRADES("Trades"),
	ANVILS("AnvilsUsed"),
	ENCHANTMENTS("Enchantments"),
	BEDS("Beds"),
	LEVELS("MaxLevel"),
	CONSUMEDPOTIONS("ConsumedPotions"),
	PLAYEDTIME("PlayedTime"),
	DROPS("ItemDrops"),
	PICKUPS("ItemPickups"),
	HOEPLOWING("HoePlowings"),
	FERTILISING("Fertilising"),
	TAMES("Taming"),
	BREWING("Brewing"),
	FIREWORKS("Fireworks"),
	MUSICDISCS("MusicDiscs"),
	ENDERPEARLS("EnderPearls"),
	PETMASTERGIVE("PetMasterGive"),
	PETMASTERRECEIVE("PetMasterReceive"),
	SMELTING("Smelting"),
	DISTANCEFOOT("DistanceFoot"),
	DISTANCEPIG("DistancePig"),
	DISTANCEHORSE("DistanceHorse"),
	DISTANCEMINECART("DistanceMinecart"),
	DISTANCEBOAT("DistanceBoat"),
	DISTANCEGLIDING("DistanceGliding"),
	DISTANCELLAMA("DistanceLlama"),
	DISTANCESNEAKING("DistanceSneaking"),
	RAIDSWON("RaidsWon"),
	RIPTIDES("Riptides"),
	ADVANCEMENTSCOMPLETED("AdvancementsCompleted"),
	BOOKSEDITED("BooksEdited");

	private static final Map<String, NormalAchievements> CATEGORY_NAMES_TO_ENUM = new HashMap<>();
	static {
		for (NormalAchievements category : NormalAchievements.values()) {
			CATEGORY_NAMES_TO_ENUM.put(category.categoryName, category);
		}
	}

	private final String categoryName;
	private final String dbName;
	private final String permName;

	NormalAchievements(String categoryName) {
		this.categoryName = categoryName;
		this.dbName = name().toLowerCase();
		this.permName = "achievement.count." + categoryName.toLowerCase();
	}

	/**
	 * Finds the category matching the provided name.
	 * 
	 * @param categoryName
	 * @return a category or null if not found
	 */
	public static NormalAchievements getByName(String categoryName) {
		return CATEGORY_NAMES_TO_ENUM.get(categoryName);
	}

	@Override
	public String toString() {
		return categoryName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toDBName() {
		return dbName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toPermName() {
		return permName;
	}
}
