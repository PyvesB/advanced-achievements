package com.hm.achievement.category;

import java.util.HashMap;
import java.util.Map;

/**
 * List of standard achievements.
 *
 * @author Pyves
 */
public enum NormalAchievements implements Category {

	CONNECTIONS("Connections", "list-connections", "Connections", "When a player connects during the day; statistic increases at most once per day."),
	DEATHS("Deaths", "list-deaths", "Number of Deaths", "When the player dies."),
	ARROWS("Arrows", "list-arrows", "Arrows Shot", "When an arrow is shot."),
	SNOWBALLS("Snowballs", "list-snowballs", "Snowballs Thrown", "When a snowball is thrown."),
	EGGS("Eggs", "list-eggs", "Eggs Thrown", "When an egg is thrown."),
	FISH("Fish", "list-fish", "Fish Caught", "When a fish is caught."),
	TREASURES("Treasures", "list-treasure", "Treasures Fished", "When a treasure is caught with a fishing rod."),
	ITEMBREAKS("ItemBreaks", "list-itembreaks", "Items Broken", "When a tool/armor/weapon is broken."),
	EATENITEMS("EatenItems", "list-eatenitems", "Items Eaten", "When an item is eaten (excludes potions and milk)."),
	SHEARS("Shear", "list-shear", "Sheeps Sheared", "When a sheep is sheared."),
	MILKS("Milk", "list-milk", "Cows Milked", "When a cow is milked."),
	LAVABUCKETS("LavaBuckets", "list-lavabuckets", "Buckets Filled with Lava", "When a bucket is filled with lava."),
	WATERBUCKETS("WaterBuckets", "list-waterbuckets", "Buckets Filled with Water", "When a bucket is filled with water."),
	TRADES("Trades", "list-trades", "Number of Trades", "When a trade with a villager is made."),
	ANVILS("AnvilsUsed", "list-anvils", "Anvils Used", "When an anvil is used."),
	ENCHANTMENTS("Enchantments", "list-enchantments", "Items Enchanted", "When an enchantment is performed."),
	BEDS("Beds", "list-beds", "Beds Entered", "When a bed is entered."),
	LEVELS("MaxLevel", "list-maxlevel", "Max Level Reached", "Maximum level reached."),
	CONSUMEDPOTIONS("ConsumedPotions", "list-potions", "Potions Consumed", "When a potion is consumed."),
	PLAYEDTIME("PlayedTime", "list-playedtime", "Time Played", "Amount of time played on the server (in hours, use integers)."),
	DROPS("ItemDrops", "list-itemdrops", "Items Dropped", "When an item is dropped on the ground."),
	PICKUPS("ItemPickups", "list-itempickups", "Items Picked Up", "When an item is picked up from the ground."),
	HOEPLOWING("HoePlowings", "list-hoeplowings", "Surface Plowed", "When soil is plowed with a hoe."),
	FERTILISING("Fertilising", "list-fertilising", "Plants Fertilised", "When bone meal is used to fertilise plants."),
	TAMES("Taming", "list-taming", "Animals Tamed", "When an animal is tamed."),
	BREWING("Brewing", "list-brewing", "Potions Brewed", "When a potion is brewed."),
	FIREWORKS("Fireworks", "list-fireworks", "Fireworks Launched", "When a firework is launched."),
	MUSICDISCS("MusicDiscs", "list-musicdiscs", "Music Discs Played", "When a music disc is played."),
	ENDERPEARLS("EnderPearls", "list-enderpearls", "Teleportations with Ender Pearls", "When a player teleports with an enderpearl."),
	PETMASTERGIVE("PetMasterGive", "list-petmastergive", "Pets Given to Another Player", "When a player gives a pet to another player (requires PetMaster plugin version 1.3 or above)."),
	PETMASTERRECEIVE("PetMasterReceive", "list-petmasterreceive", "Pets Received from Another Player", "When a player receives a pet from another player (requires PetMaster plugin version 1.3 or above)."),
	SMELTING("Smelting", "list-smelting", "Items Smelt", "When an item is smelt in a furnace."),
	DISTANCEFOOT("DistanceFoot", "list-distance-foot", "Distance Travelled by Foot", "When a distance is traveled by foot."),
	DISTANCEPIG("DistancePig", "list-distance-pig", "Distance Travelled on a Pig", "When a distance is traveled on a pig."),
	DISTANCEHORSE("DistanceHorse", "list-distance-horse", "Distance Travelled on a Horse", "When a distance is traveled on a horse."),
	DISTANCEMINECART("DistanceMinecart", "list-distance-minecart", "Distance Travelled in a Minecart", "When a distance is traveled in a minecart."),
	DISTANCEBOAT("DistanceBoat", "list-distance-boat", "Distance Travelled in a Boat", "When a distance is traveled in a boat."),
	DISTANCEGLIDING("DistanceGliding", "list-distance-gliding", "Distance Travelled with Elytra", "When a distance is traveled with elytra."),
	DISTANCELLAMA("DistanceLlama", "list-distance-llama", "Distance Travelled on a Llama", "When a distance is traveled on a llama."),
	RAIDSWON("RaidsWon", "list-raids-won", "Raids Won", "When a raid is won."),
	RIPTIDES("Riptides", "list-riptides", "Riptides Used", "When riptide is used with a trident"),
	ADVANCEMENTSCOMPLETED("AdvancementsCompleted", "list-advancements-completed", "Advancements Completed", "When a certain number of advancements have been completed.");

	private static final Map<String, NormalAchievements> CATEGORY_NAMES_TO_ENUM = new HashMap<>();
	static {
		for (NormalAchievements category : NormalAchievements.values()) {
			CATEGORY_NAMES_TO_ENUM.put(category.categoryName, category);
		}
	}

	private final String categoryName;
	private final String langName;
	private final String langDefault;
	private final String configComment;
	private final String dbName;
	private final String permName;

	NormalAchievements(String categoryName, String langName, String langDefault, String configComment) {
		this.categoryName = categoryName;
		this.langName = langName;
		this.langDefault = langDefault;
		this.configComment = configComment;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toLangDefault() {
		return langDefault;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toConfigComment() {
		return configComment;
	}
}
