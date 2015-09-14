package com.hm.achievement.language;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
 
/**
* An enum for requesting strings from the language file.
* @author gomeow
*/
public enum Lang {
    
    CONFIGURATION_SUCCESSFULLY_RELOADED("configuration-successfully-reloaded", "Configuration successfully reloaded."),
    NO_PERMS("no-permissions", "You do not have the permission to do this."),
    TOP_ACHIEVEMENT("top-achievement", "Top achievement owners:"),
    PLAYER_RANK("player-rank", "Current rank:"),
    NUMBER_ACHIEVEMENTS("number-achievements", "Achievements received: "),
    BOOK_RECEIVED("book-received","You received your achievements book!"),
    BOOK_DELAY("book-delay","You must wait TIME seconds between each book reception!"),
    PLAYER_OFFLINE("player-offline","The player PLAYER is offline!"),
    ACHIEVEMENT_ALREADY_RECEIVED("achievement-already-received", "The player PLAYER has already received this achievement!"),
    ACHIEVEMENT_NOT_FOUND("achievement-not-found", "The specified achievement was not found."),
    ACHIEVEMENT_GIVEN("achievement-given","Achievement given!"),
    ACHIEVEMENT_RECEIVED("achievement-received","PLAYER received the achievement:"),
    ACHIVEMENT_NEW("achievement-new", "New Achievement:"),
    ITEM_REWARD_RECEIVED("item-reward-received", "You received an item reward!"),
    MONEY_REWARD_RECEIVED("money-reward-received", "You received: AMOUNT !"),
    COMMAND_REWARD("command-reward", "Reward command carried out!"),
    BOOK_NAME("book-name", "Achievements"),
    AACH_COMMAND_BOOK("aach-command-book", "Receive your achievements book."),
    AACH_COMMAND_STATS("aach-command-stats", "Amount of achievements you have received."),
    AACH_COMMAND_LIST("aach-command-list", "Display received and missing achievements."),
    AACH_COMMAND_TOP("aach-command-top", "Display personal and global rankings."),
    AACH_COMMAND_RELOAD("aach-command-reload", "Reload the plugin's configuration."),
    AACH_COMMAND_GIVE("aach-command-give", "Give the achievement ACH to the player NAME.");
    
 
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