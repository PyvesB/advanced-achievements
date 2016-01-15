package com.hm.achievement.language;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
 
/**
* An enum for requesting strings from the language file.
* @author gomeow
*/
public enum Lang {
    
    CONFIGURATION_SUCCESSFULLY_RELOADED("configuration-successfully-reloaded", "Configuration successfully reloaded."),
    CONFIGURATION_RELOAD_FAILED("configuration-reload-failed", "Errors while reloading configuration. Please view logs for more details."),
    NO_PERMS("no-permissions", "You do not have the permission to do this."),
    TOP_ACHIEVEMENT("top-achievement", "Top achievement owners:"),
    PLAYER_RANK("player-rank", "Current rank:"),
    NUMBER_ACHIEVEMENTS("number-achievements", "Achievements received:"),
    BOOK_RECEIVED("book-received", "You received your achievements book!"),
    BOOK_DELAY("book-delay", "You must wait TIME seconds between each book reception!"),
    LIST_DELAY("list-delay", "You must wait TIME seconds between each list command!"),
    PLAYER_OFFLINE("player-offline", "The player PLAYER is offline!"),
    ACHIEVEMENT_ALREADY_RECEIVED("achievement-already-received", "The player PLAYER has already received this achievement!"),
    ACHIEVEMENT_NOT_FOUND("achievement-not-found", "The specified achievement was not found."),
    ACHIEVEMENT_GIVEN("achievement-given", "Achievement given!"),
    ACHIEVEMENT_RECEIVED("achievement-received", "PLAYER received the achievement:"),
    ACHIEVEMENT_NEW("achievement-new", "New Achievement:"),
    CHECK_ACHIEVEMENT_TRUE("check-achievement-true", "PLAYER has received the achievement ACH!"),
    CHECK_ACHIEVEMENT_FALSE("check-achievements-false", "PLAYER has not received the achievement ACH!"),
    DELETE_ACHIEVEMENT("delete-achievements", "The achievement ACH was deleted from PLAYER."),
    ITEM_REWARD_RECEIVED("item-reward-received", "You received an item reward:"),
    MONEY_REWARD_RECEIVED("money-reward-received", "You received: AMOUNT !"),
    COMMAND_REWARD("command-reward", "Reward command carried out!"),
    BOOK_NAME("book-name", "Achievements"),
    AACH_COMMAND_BOOK("aach-command-book", "Receive your achievements book."),
    AACH_COMMAND_STATS("aach-command-stats", "Amount of achievements you have received."),
    AACH_COMMAND_LIST("aach-command-list", "Display received and missing achievements."),
    AACH_COMMAND_TOP("aach-command-top", "Display personal and global rankings."),
    AACH_COMMAND_RELOAD("aach-command-reload", "Reload the plugin's configuration."),
    AACH_COMMAND_INFO("aach-command-info", "Display various information about the plugin."),
    AACH_COMMAND_GIVE("aach-command-give", "Give achievement ACH to player &7NAME."),
    AACH_COMMAND_CHECK("aach-command-check", "Check if player NAME has &7received ACH."),
    AACH_COMMAND_DELETE("aach-command-delete", "Delete achievement ACH from &7player NAME."),
    VERSION_COMMAND_NAME("version-command-name", "Name:"),
    VERSION_COMMAND_VERSION("version-command-version", "Version:"),
    VERSION_COMMAND_WEBSITE("version-command-website", "Website:"),
    VERSION_COMMAND_AUTHOR("version-command-author", "Author:"),
    VERSION_COMMAND_DESCRIPTION("version-command-description", "Description:"),
    VERSION_COMMAND_DESCRIPTION_DETAILS("version-command-description-details", "Advanced Achievements enables unique and challenging achievements. Try to collect as many as you can, earn rewards, climb the rankings and receive RP books!"),
    VERSION_COMMAND_VAULT("version-command-vault", "Vault integration:"),
    VERSION_COMMAND_DATABASE("version-command-database", "Database type:"),
    LIST_GUI_TITLE("list-gui-title", "&5§lAchievements List"),
    LIST_REWARD("list-reward", " Reward:"),
    LIST_REWARD_MONEY("list-reward-money", "money"),
    LIST_REWARD_ITEM("list-reward-item", "item"),
    LIST_REWARD_COMMAND("list-reward-command", "other"),
    LIST_AMOUNT("list-amount", " Lvl:"),
    LIST_CONNECTIONS("list-connections", "Connections"),
    LIST_PLACES("list-places", "Blocks Placed"),
    LIST_BREAKS("list-breaks", "Blocks Broken"),
    LIST_KILLS("list-kills", "Entities Killed"),
    LIST_CRAFTS("list-crafts", "Items Crafted"),
    LIST_DEATHS("list-deaths", "Number of Deaths"),
    LIST_ARROWS("list-arrows", "Arrows Shot"),
    LIST_SNOWBALLS("list-snowballs", "Snowballs Thrown"),
    LIST_EGGS("list-eggs", "Eggs Thrown"),
    LIST_FISH("list-fish", "Fish Caught"),
    LIST_ITEMBREAKS("list-itembreaks", "Items Broken"),
    LIST_EATENITEMS("list-eatenitems", "Items Eaten"),
    LIST_SHEAR("list-shear", "Sheeps Sheared"),
    LIST_MILK("list-milk", "Cows Milked"),
    LIST_TRADES("list-trades", "Number of Trades"),
    LIST_ANVILS("list-anvils", "Anvils Used"),
    LIST_ENCHANTMENTS("list-enchantments", "Items Enchanted"),
    LIST_BEDS("list-beds", "Beds Entered"),
    LIST_MAXLEVEL("list-maxlevel", "Max Level Reached"),
    LIST_POTIONS("list-potions", "Potions Consumed"),
    LIST_PLAYEDTIME("list-playedtime", "Time Played"),
    LIST_DISTANCE_FOOT("list-distance-foot", "Distance Travelled by Foot"),
    LIST_DISTANCE_PIG("list-distance-pig", "Distance Travelled on a Pig"),
    LIST_DISTANCE_HORSE("list-distance-horse", "Distance Travelled on a Horse"),
    LIST_DISTANCE_MINECART("list-distance-minecart", "Distance Travelled in a Minecart"),
    LIST_DISTANCE_BOAT("list-distance-boat", "Distance Travelled in a Boat"),
    LIST_ITEMDROPS("list-itemdrops", "Items Dropped"),
    LIST_HOEPLOWINGS("list-hoeplowings", "Surface Plowed"),
    LIST_FERTILISING("list-fertilising", "Plants Fertilised"),
    LIST_TAMING("list-taming", "Animals Tamed"),
    LIST_BREWING("list-brewing", "Potions Brewed"),
    LIST_FIREWORKS("list-fireworks", "Fireworks Launched"),
    LIST_COMMANDS("list-commands", "Other Achievements");
    
    
 
    private String path;
    private String def;
    private static YamlConfiguration LANG;
 
    /**
    * Lang enum constructor.
    * @param path The string path.
    * @param start The default string.
    */
    Lang(String path, String start) {
        this.path = path;
        this.def = start;
    }
 
    /**
    * Set the {@code YamlConfiguration} to use.
    * @param config The config to set.
    */
    public static void setFile(YamlConfiguration config) {
        LANG = config;
    }
 
    @Override
    public String toString() {        
        return ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, def));
    }
 
    /**
    * Get the default value of the path.
    * @return The default value of the path.
    */
    public String getDefault() {
        return this.def;
    }
 
    /**
    * Get the path to the string.
    * @return The path to the string.
    */
    public String getPath() {
        return this.path;
    }
}