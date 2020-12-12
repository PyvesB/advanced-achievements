package com.hm.achievement.category;

import java.util.HashMap;
import java.util.Map;

/**
 * List of standard achievements.
 *
 * @author Pyves
 */
public enum NormalAchievements implements Category {

	CONNECTIONS("Connections", "list-connections"),
	DEATHS("Deaths", "list-deaths"),
	ARROWS("Arrows", "list-arrows"),
	SNOWBALLS("Snowballs", "list-snowballs"),
	EGGS("Eggs", "list-eggs"),
	FISH("Fish", "list-fish"),
	TREASURES("Treasures", "list-treasure"),
	ITEMBREAKS("ItemBreaks", "list-itembreaks"),
	EATENITEMS("EatenItems", "list-eatenitems"),
	SHEARS("Shear", "list-shear"),
	MILKS("Milk", "list-milk"),
	LAVABUCKETS("LavaBuckets", "list-lavabuckets"),
	WATERBUCKETS("WaterBuckets", "list-waterbuckets"),
	TRADES("Trades", "list-trades"),
	ANVILS("AnvilsUsed", "list-anvils"),
	ENCHANTMENTS("Enchantments", "list-enchantments"),
	BEDS("Beds", "list-beds"),
	LEVELS("MaxLevel", "list-maxlevel"),
	CONSUMEDPOTIONS("ConsumedPotions", "list-potions"),
	PLAYEDTIME("PlayedTime", "list-playedtime"),
	DROPS("ItemDrops", "list-itemdrops"),
	PICKUPS("ItemPickups", "list-itempickups"),
	HOEPLOWING("HoePlowings", "list-hoeplowings"),
	FERTILISING("Fertilising", "list-fertilising"),
	TAMES("Taming", "list-taming"),
	BREWING("Brewing", "list-brewing"),
	FIREWORKS("Fireworks", "list-fireworks"),
	MUSICDISCS("MusicDiscs", "list-musicdiscs"),
	ENDERPEARLS("EnderPearls", "list-enderpearls"),
	PETMASTERGIVE("PetMasterGive", "list-petmastergive"),
	PETMASTERRECEIVE("PetMasterReceive", "list-petmasterreceive"),
	SMELTING("Smelting", "list-smelting"),
	DISTANCEFOOT("DistanceFoot", "list-distance-foot"),
	DISTANCEPIG("DistancePig", "list-distance-pig"),
	DISTANCEHORSE("DistanceHorse", "list-distance-horse"),
	DISTANCEMINECART("DistanceMinecart", "list-distance-minecart"),
	DISTANCEBOAT("DistanceBoat", "list-distance-boat"),
	DISTANCEGLIDING("DistanceGliding", "list-distance-gliding"),
	DISTANCELLAMA("DistanceLlama", "list-distance-llama"),
	DISTANCESNEAKING("DistanceSneaking", "list-distance-sneaking"),
	RAIDSWON("RaidsWon", "list-raids-won"),
	RIPTIDES("Riptides", "list-riptides"),
	ADVANCEMENTSCOMPLETED("AdvancementsCompleted", "list-advancements-completed");

	private static final Map<String, NormalAchievements> CATEGORY_NAMES_TO_ENUM = new HashMap<>();
	static {
		for (NormalAchievements category : NormalAchievements.values()) {
			CATEGORY_NAMES_TO_ENUM.put(category.categoryName, category);
		}
	}

	private final String categoryName;
	private final String langName;
	private final String dbName;
	private final String permName;

	NormalAchievements(String categoryName, String langName) {
		this.categoryName = categoryName;
		this.langName = langName;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toLangKey() {
		return langName;
	}
}
