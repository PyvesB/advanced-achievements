package com.hm.achievement.category;

/**
 * List of standard achievements.
 * 
 * @author Pyves
 *
 */
public enum NormalAchievements {

	CONNECTIONS("Connections"),
	DEATHS("Deaths"),
	ARROWS("Arrows"),
	SNOWBALLS("Snowballs"),
	EGGS("Eggs"),
	FISH("Fish"),
	ITEMBREAKS("ItemBreaks"),
	EATENITEMS("EatenItems"),
	SHEARS("Shear"),
	MILKS("Milk"),
	TRADES("Trades"),
	ANVILS("AnvilsUsed"),
	ENCHANTMENTS("Enchantments"),
	BEDS("Beds"),
	LEVELS("MaxLevel"),
	CONSUMEDPOTIONS("ConsumedPotions"),
	PLAYEDTIME("PlayedTime"),
	DROPS("ItemDrops"),
	HOEPLOWING("HoePlowings"),
	FERTILISING("Fertilising"),
	TAMES("Taming"),
	BREWING("Brewing"),
	FIREWORKS("Fireworks"),
	MUSICDISCS("MusicDiscs"),
	ENDERPEARLS("EnderPearls"),
	DISTANCEFOOT("DistanceFoot"),
	DISTANCEPIG("DistancePig"),
	DISTANCEHORSE("DistanceHorse"),
	DISTANCEMINECART("DistanceMinecart"),
	DISTANCEBOAT("DistanceBoat"),
	DISTANCEGLIDING("DistanceGliding");

	private String categoryName;

	private NormalAchievements(String categoryName) {
		this.categoryName = categoryName;
	}

	@Override
	public String toString() {

		return categoryName;
	}

	/**
	 * Converts to database name: name of the enum in lower case.
	 * 
	 * @param type
	 * @return
	 */
	public String toDBName() {

		return name().toLowerCase();
	}

	/**
	 * Converts to permission name: common prefix + name of the category in lower case.
	 * 
	 * @param type
	 * @return
	 */
	public String toPermName() {

		return "achievement.count." + categoryName.toLowerCase();
	}
}
