package com.hm.achievement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.hm.achievement.db.SQLDatabases;
import com.hm.achievement.db.SendPooledRequests;
import com.hm.achievement.language.Lang;
import com.hm.achievement.listener.AchieveArrowListener;
import com.hm.achievement.listener.AchieveBedListener;
import com.hm.achievement.listener.AchieveBlockListener;
import com.hm.achievement.listener.AchieveBreakListener;
import com.hm.achievement.listener.AchieveConnectionListener;
import com.hm.achievement.listener.AchieveConsumeListener;
import com.hm.achievement.listener.AchieveCraftListener;
import com.hm.achievement.listener.AchieveDeathListener;
import com.hm.achievement.listener.AchieveEnchantListener;
import com.hm.achievement.listener.AchieveEntityListener;
import com.hm.achievement.listener.AchieveFishListener;
import com.hm.achievement.listener.AchieveInventoryClickListener;
import com.hm.achievement.listener.AchieveMilkListener;
import com.hm.achievement.listener.AchieveShearListener;
import com.hm.achievement.listener.AchieveSnowballEggsListener;
import com.hm.achievement.listener.AchieveXPListener;

/**
 * A plugin that enables custom achievements and RP books on your Minecraft
 * server. Some minor parts of the code are based on Achievement plugin by
 * Death_marine and captainawesome7, under Federation of Lost Lawn Chairs
 * license (http://dev.bukkit.org/licenses/1332-federation-of-lost-lawn-chairs)
 * AdvancedAchievements is under GNU General Public License version 3. Please
 * visit the plugin's GitHub for more information :
 * https://github.com/PyvesB/AdvancedAchievements
 * 
 * @since April 2015
 * @version 1.7
 * @author DarkPyves
 */

public class AdvancedAchievements extends JavaPlugin {

	private Economy economy;
	private AchieveBlockListener blockListener;
	private AchieveEntityListener entityListener;
	private AchieveCraftListener craftListener;
	private AchieveDeathListener deathListener;
	private AchieveArrowListener arrowListener;
	private AchieveSnowballEggsListener snowballListener;
	private AchieveFishListener fishListener;
	private AchieveBreakListener itemBreakListener;
	private AchieveConsumeListener eatenItemsListener;
	private AchieveShearListener shearListener;
	private AchieveMilkListener milkListener;
	private AchievementRewards reward;
	private SQLDatabases db;
	private AchievementDisplay achievementDisplay;
	private int time;
	private int databaseVersion;
	private HashMap<Player, Long> players;
	private AchieveConnectionListener connectionListener;
	private boolean updateNeeded;
	private AdvancedAchievementsUpdateChecker updateChecker;
	private boolean retroVault;
	private boolean firework;
	private boolean sound;
	private boolean chatNotify;
	private AchievementCommandGiver achievementCommandGiver;
	private AchieveInventoryClickListener inventoryClickListener;
	private AchieveEnchantListener enchantmentListener;
	private String icon;
	private AchieveXPListener xpListener;
	private AchieveBedListener bedListener;
	private int totalAchievements;
	private String bookSeparator;
	private ArrayList<String> achievementsTop;
	private long lastTopTime;
	private boolean restrictCreative;
	private boolean multiCommand;
	private boolean rewardCommandNotif;
	private boolean databaseBackup;
	private List<String> excludedWorldList;
	private int topList;

	public static YamlConfiguration LANG;
	public static File LANG_FILE;

	public AdvancedAchievements() {

		economy = null;

		blockListener = new AchieveBlockListener(this);
		entityListener = new AchieveEntityListener(this);
		craftListener = new AchieveCraftListener(this);
		deathListener = new AchieveDeathListener(this);
		arrowListener = new AchieveArrowListener(this);
		snowballListener = new AchieveSnowballEggsListener(this);
		fishListener = new AchieveFishListener(this);
		itemBreakListener = new AchieveBreakListener(this);
		eatenItemsListener = new AchieveConsumeListener(this);
		shearListener = new AchieveShearListener(this);
		milkListener = new AchieveMilkListener(this);
		connectionListener = new AchieveConnectionListener(this);
		inventoryClickListener = new AchieveInventoryClickListener(this);
		enchantmentListener = new AchieveEnchantListener(this);
		xpListener = new AchieveXPListener(this);
		bedListener = new AchieveBedListener(this);

		achievementDisplay = new AchievementDisplay(this);
		reward = new AchievementRewards(this);
		achievementCommandGiver = new AchievementCommandGiver(this);

		db = new SQLDatabases();

		players = new HashMap<Player, Long>();

	}

	public void onDisable() {

		new SendPooledRequests(this, false).sendRequests();
		this.getLogger().info(
				"Remaining requests sent to database, plugin disabled.");

	}

	public void onEnable() {

		if (!this.getDataFolder().exists())
			this.getDataFolder().mkdir();

		configurationLoad();

		PluginManager pm = getServer().getPluginManager();

		if (this.getConfig().getConfigurationSection("Breaks").getKeys(false)
				.size() != 0
				|| this.getConfig().getConfigurationSection("Places")
						.getKeys(false).size() != 0)
			pm.registerEvents(blockListener, this);

		if (this.getConfig().getConfigurationSection("Kills").getKeys(false)
				.size() != 0)
			pm.registerEvents(entityListener, this);

		if (this.getConfig().getConfigurationSection("Crafts").getKeys(false)
				.size() != 0)
			pm.registerEvents(craftListener, this);

		if (this.getConfig().getConfigurationSection("Deaths").getKeys(false)
				.size() != 0)
			pm.registerEvents(deathListener, this);

		if (this.getConfig().getConfigurationSection("Arrows").getKeys(false)
				.size() != 0)
			pm.registerEvents(arrowListener, this);

		if (this.getConfig().getConfigurationSection("Snowballs")
				.getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("Eggs")
						.getKeys(false).size() != 0)
			pm.registerEvents(snowballListener, this);

		if (this.getConfig().getConfigurationSection("Fish").getKeys(false)
				.size() != 0)
			pm.registerEvents(fishListener, this);

		if (this.getConfig().getConfigurationSection("ItemBreaks")
				.getKeys(false).size() != 0)
			pm.registerEvents(itemBreakListener, this);

		if (this.getConfig().getConfigurationSection("ConsumedPotions")
				.getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("EatenItems")
						.getKeys(false).size() != 0)
			pm.registerEvents(eatenItemsListener, this);

		if (this.getConfig().getConfigurationSection("Shear").getKeys(false)
				.size() != 0)
			pm.registerEvents(shearListener, this);

		if (this.getConfig().getConfigurationSection("Milk").getKeys(false)
				.size() != 0)
			pm.registerEvents(milkListener, this);

		if (this.getConfig().getConfigurationSection("Connections")
				.getKeys(false).size() != 0)
			pm.registerEvents(connectionListener, this);

		if (this.getConfig().getConfigurationSection("Trades").getKeys(false)
				.size() != 0
				|| this.getConfig().getConfigurationSection("AnvilsUsed")
						.getKeys(false).size() != 0)
			pm.registerEvents(inventoryClickListener, this);

		if (this.getConfig().getConfigurationSection("Enchantments")
				.getKeys(false).size() != 0)
			pm.registerEvents(enchantmentListener, this);

		if (this.getConfig().getConfigurationSection("MaxLevel").getKeys(false)
				.size() != 0)
			pm.registerEvents(xpListener, this);

		if (this.getConfig().getConfigurationSection("Beds").getKeys(false)
				.size() != 0)
			pm.registerEvents(bedListener, this);

		db.initialize(this);

		Bukkit.getServer()
				.getScheduler()
				.scheduleSyncRepeatingTask(
						Bukkit.getPluginManager().getPlugin(
								"AdvancedAchievements"),
						new SendPooledRequests(this, true), 2400, 1200);

		this.getLogger()
				.info("AdvancedAchievements configurations and database successfully loaded!");
	}

	private void backupConfigFile() {

		File original = new File(this.getDataFolder(), "config.yml");
		File backup = new File(this.getDataFolder(), "config.yml.bak");
		if (original.length() != backup.length() && original.length() != 0) {
			try {
				FileInputStream inStream = new FileInputStream(original);
				FileOutputStream outStream;
				outStream = new FileOutputStream(backup);

				byte[] buffer = new byte[1024];

				int length;
				while ((length = inStream.read(buffer)) > 0) {
					outStream.write(buffer, 0, length);
				}

				if (inStream != null)
					inStream.close();
				if (outStream != null)
					outStream.close();
				this.getLogger().info(
						"Successfully backed up configuration file.");

			} catch (FileNotFoundException e) {
				this.getLogger().severe("Error while backing up config file.");
			} catch (IOException e) {
				this.getLogger().severe("Error while backing up config file.");
			}
		}

	}

	private void backupDBFile() {

		if (!databaseBackup)
			return;

		File original = new File(this.getDataFolder(), "achievements.db");
		File backup = new File(this.getDataFolder(), "achievements.db.bak");
		if ((System.currentTimeMillis() - backup.lastModified() > 86400000 || backup
				.length() == 0) && original.length() != 0) {
			try {
				FileInputStream inStream = new FileInputStream(original);
				FileOutputStream outStream;
				outStream = new FileOutputStream(backup);

				byte[] buffer = new byte[1024];

				int length;
				while ((length = inStream.read(buffer)) > 0) {
					outStream.write(buffer, 0, length);
				}

				if (inStream != null)
					inStream.close();
				if (outStream != null)
					outStream.close();
				this.getLogger().info("Successfully backed up database file.");

			} catch (FileNotFoundException e) {
				e.printStackTrace();
				this.getLogger()
						.severe("Error while backing up database file.");
			} catch (IOException e) {
				e.printStackTrace();
				this.getLogger()
						.severe("Error while backing up database file.");
			}
		}

	}

	/**
	 * Load the lang.yml file.
	 * 
	 * @return The lang.yml config.
	 */
	public void loadLang() {
		File lang = new File(getDataFolder(), "lang.yml");
		if (!lang.exists()) {
			try {
				getDataFolder().mkdir();
				lang.createNewFile();
				Reader defConfigStream = new InputStreamReader(
						this.getResource("lang.yml"), "UTF8");
				if (defConfigStream != null) {
					YamlConfiguration defConfig = YamlConfiguration
							.loadConfiguration(defConfigStream);
					defConfig.save(lang);
					Lang.setFile(defConfig);
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
				this.getLogger().severe("Error while creating language file.");
			}
		}
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
		for (Lang item : Lang.values()) {
			if (conf.getString(item.getPath()) == null) {
				conf.set(item.getPath(), item.getDefault());
			}
		}
		Lang.setFile(conf);
		LANG = conf;
		LANG_FILE = lang;
		try {
			conf.save(getLangFile());
		} catch (IOException e) {
			e.printStackTrace();
			this.getLogger().severe("Error while saving language file.");
		}
	}

	private void configurationLoad() {

		backupConfigFile();
		loadLang();

		this.saveDefaultConfig();

		// Workaround to keep configuration file comments.
		File config = new File(this.getDataFolder(), "config.yml");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(config));

			StringBuilder configString = new StringBuilder("");

			String currentLine;

			while ((currentLine = reader.readLine()) != null) {
				if (currentLine.startsWith("#")) {
					currentLine = currentLine.replace(" ", "$");
					currentLine = currentLine.replace(":", "&");
					currentLine = currentLine.replace(".", "%");

					currentLine = "COMMENT" + currentLine;
					currentLine = currentLine + ": true";

				}
				configString.append(currentLine + "\n");
			}

			BufferedWriter writer = null;

			writer = new BufferedWriter(new FileWriter(config));

			writer.write(configString.toString());
			writer.flush();
			writer.close();
			reader.close();
		} catch (IOException e) {

			this.getLogger().severe("Saving comments in config file failed.");
		}

		// Update configurations from older plugin versions.
		if (!this.getConfig().getKeys(false).contains("CheckForUpdate")) {
			this.getConfig().set("CheckForUpdate", true);
			this.saveConfig();

		}

		if (!this.getConfig().getKeys(false).contains("RetroVault")) {
			this.getConfig().set("RetroVault", false);
			this.saveConfig();

		}

		if (!this.getConfig().getKeys(false).contains("Firework")) {
			this.getConfig().set("Firework", true);
			this.saveConfig();

		}

		if (!this.getConfig().getKeys(false).contains("Sound")) {
			this.getConfig().set("Sound", true);
			this.saveConfig();

		}

		if (!this.getConfig().getKeys(false).contains("DatabaseVersion")) {
			this.getConfig().set("DatabaseVersion", 1);
			this.saveConfig();

		}

		if (!this.getConfig().getKeys(false).contains("Icon")) {
			this.getConfig().set("Icon", "\u2618");
			this.saveConfig();

		}

		if (!this.getConfig().getKeys(false).contains("ChatNotify")) {
			this.getConfig().set("ChatNotify", false);
			this.saveConfig();

		}

		if (!this.getConfig().getKeys(false).contains("BookSeparator")) {
			this.getConfig().set("BookSeparator", "");
			this.saveConfig();

		}

		if (!this.getConfig().getKeys(false).contains("RestrictCreative")) {
			this.getConfig().set("RestrictCreative", false);
			this.saveConfig();

		}

		if (!this.getConfig().getKeys(false).contains("MultiCommand")) {
			this.getConfig().set("MultiCommand", true);
			this.saveConfig();

		}

		if (!this.getConfig().getKeys(false).contains("RewardCommandNotif")) {
			this.getConfig().set("RewardCommandNotif", true);
			this.saveConfig();

		}

		if (!this.getConfig().getKeys(false).contains("DatabaseBackup")) {
			this.getConfig().set("DatabaseBackup", true);
			this.saveConfig();

		}

		if (!this.getConfig().getKeys(false).contains("ExcludedWorlds")) {
			List<String> list = new ArrayList<String>();
			this.getConfig().set("ExcludedWorlds", list);
			this.saveConfig();

		}

		if (!this.getConfig().getKeys(false).contains("TopList")) {
			this.getConfig().set("TopList", 5);
			this.saveConfig();

		}

		try {
			reader = new BufferedReader(new FileReader(config));
			StringBuilder configString = new StringBuilder("");

			String currentLine;

			while ((currentLine = reader.readLine()) != null) {
				if (currentLine.startsWith("COMMENT")) {
					currentLine = currentLine.replace("COMMENT", "");
					currentLine = currentLine.replace(": true", "");
					currentLine = currentLine.replace("$", " ");
					currentLine = currentLine.replace("&", ":");
					currentLine = currentLine.replace("%", ".");
					configString.append(currentLine + "\n");

				} else if (!currentLine.startsWith("#"))
					configString.append(currentLine + "\n");
			}

			BufferedWriter writer = null;

			writer = new BufferedWriter(new FileWriter(config));

			writer.write(configString.toString());
			writer.flush();
			writer.close();
			reader.close();
		} catch (IOException e) {

			this.getLogger().severe("Saving comments in config file failed.");
		}

		time = this.getConfig().getInt("Time", 900) * 1000;
		retroVault = this.getConfig().getBoolean("RetroVault", false);
		firework = this.getConfig().getBoolean("Firework", true);
		sound = this.getConfig().getBoolean("Sound", true);
		databaseVersion = this.getConfig().getInt("DatabaseVersion", 1);
		icon = this.getConfig().getString("Icon", "\u2618");
		chatNotify = this.getConfig().getBoolean("ChatNotify", false);
		bookSeparator = this.getConfig().getString("BookSeparator", "");
		restrictCreative = this.getConfig().getBoolean("RestrictCreative",
				false);
		multiCommand = this.getConfig().getBoolean("MultiCommand", true);
		rewardCommandNotif = this.getConfig().getBoolean("RewardCommandNotif",
				true);
		databaseBackup = this.getConfig().getBoolean("DatabaseBackup", true);
		excludedWorldList = this.getConfig().getStringList("ExcludedWorlds");
		topList = this.getConfig().getInt("TopList", 5);

		lastTopTime = 0;

		if (this.getConfig().getBoolean("CheckForUpdate", true)) {
			updateChecker = new AdvancedAchievementsUpdateChecker(this,
					"http://dev.bukkit.org/bukkit-plugins/advanced-achievements/files.rss");
			updateNeeded = updateChecker.updateNeeded();
		}

		backupDBFile();

		totalAchievements = 0;

		totalAchievements += this.getConfig()
				.getConfigurationSection("Connections").getKeys(false).size();
		for (String item : this.getConfig().getConfigurationSection("Places")
				.getKeys(false))
			totalAchievements += this.getConfig()
					.getConfigurationSection("Places." + item).getKeys(false)
					.size();
		for (String item : this.getConfig().getConfigurationSection("Breaks")
				.getKeys(false))
			totalAchievements += this.getConfig()
					.getConfigurationSection("Breaks." + item).getKeys(false)
					.size();
		for (String item : this.getConfig().getConfigurationSection("Kills")
				.getKeys(false))
			totalAchievements += this.getConfig()
					.getConfigurationSection("Kills." + item).getKeys(false)
					.size();
		for (String item : this.getConfig().getConfigurationSection("Crafts")
				.getKeys(false))
			totalAchievements += this.getConfig()
					.getConfigurationSection("Crafts." + item).getKeys(false)
					.size();
		totalAchievements += this.getConfig().getConfigurationSection("Deaths")
				.getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("Arrows")
				.getKeys(false).size();
		totalAchievements += this.getConfig()
				.getConfigurationSection("Snowballs").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("Eggs")
				.getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("Fish")
				.getKeys(false).size();
		totalAchievements += this.getConfig()
				.getConfigurationSection("ItemBreaks").getKeys(false).size();
		totalAchievements += this.getConfig()
				.getConfigurationSection("EatenItems").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("Shear")
				.getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("Milk")
				.getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("Trades")
				.getKeys(false).size();
		totalAchievements += this.getConfig()
				.getConfigurationSection("AnvilsUsed").getKeys(false).size();
		totalAchievements += this.getConfig()
				.getConfigurationSection("Enchantments").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("Beds")
				.getKeys(false).size();
		totalAchievements += this.getConfig()
				.getConfigurationSection("MaxLevel").getKeys(false).size();
		totalAchievements += this.getConfig()
				.getConfigurationSection("ConsumedPotions").getKeys(false)
				.size();
		totalAchievements += this.getConfig()
				.getConfigurationSection("Commands").getKeys(false).size();
	}

	public boolean isInExludedWorld(Player player) {

		if (excludedWorldList.size() == 0)
			return false;

		for (int i = 0; i < excludedWorldList.size(); i++) {
			if (player.getWorld().getName()
					.equalsIgnoreCase(excludedWorldList.get(i)))
				return true;
		}

		return false;
	}

	public boolean setupEconomy() {
		try {
			RegisteredServiceProvider<Economy> economyProvider = getServer()
					.getServicesManager().getRegistration(
							net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
			}

			return (economy != null);
		} catch (NoClassDefFoundError e) {
			return false;
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String args[]) {

		if (sender instanceof Player) {

			Player player = ((Player) sender);
			String name = player.getName();
			if (cmd.getName().equalsIgnoreCase("aach") && (args.length == 1)
					&& !args[0].equalsIgnoreCase("help")) {
				if (args[0].equalsIgnoreCase("book")
						&& player.hasPermission("achievement.book")) {

					giveBook(player, name);

				} else if (args[0].equalsIgnoreCase("reload")) {
					if (player.hasPermission("achievement.reload")) {
						this.reloadConfig();
						configurationLoad();
						player.sendMessage(ChatColor.GRAY + "["
								+ ChatColor.DARK_PURPLE + icon + ChatColor.GRAY
								+ "] "
								+ Lang.CONFIGURATION_SUCCESSFULLY_RELOADED);
					} else {
						player.sendMessage(ChatColor.GRAY + "["
								+ ChatColor.DARK_PURPLE + icon + ChatColor.GRAY
								+ "] " + Lang.NO_PERMS);
					}
				} else if (args[0].equalsIgnoreCase("stats")) {
					getStats(player);
				} else if (args[0].equalsIgnoreCase("top")) {
					getTop(player);
				}

			} else if (cmd.getName().equalsIgnoreCase("aach")
					&& (args.length == 3) && args[0].equalsIgnoreCase("give")) {

				if (player.hasPermission("achievement.give")) {

					achievementCommandGiver.achievementGive(sender, args);

				}

				else {

					player.sendMessage(ChatColor.GRAY + "["
							+ ChatColor.DARK_PURPLE + icon + ChatColor.GRAY
							+ "] " + Lang.NO_PERMS);

				}

			} else if (cmd.getName().equalsIgnoreCase("aach")
					|| (args.length == 1) && !args[0].equalsIgnoreCase("help")) {

				player.sendMessage((new StringBuilder())
						.append(ChatColor.DARK_PURPLE)
						.append("-=-=-=-=-")
						.append(ChatColor.GRAY)
						.append("[")
						.append(ChatColor.DARK_PURPLE)
						.append(icon)
						.append("§lAdvancedAchievements")
						.append(" §lv" + this.getDescription().getVersion()
								+ "§r").append(ChatColor.DARK_PURPLE)
						.append(icon).append(ChatColor.GRAY).append("]")
						.append(ChatColor.DARK_PURPLE).append("-=-=-=-=-")
						.toString());

				sender.sendMessage((new StringBuilder())
						.append(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
								+ icon + ChatColor.GRAY + "] ")
						.append(ChatColor.DARK_PURPLE + "/aach book")
						.append(ChatColor.GRAY)
						.append(" - " + Lang.AACH_COMMAND_BOOK).toString());

				sender.sendMessage((new StringBuilder())
						.append(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
								+ icon + ChatColor.GRAY + "] ")
						.append(ChatColor.DARK_PURPLE + "/aach stats")
						.append(ChatColor.GRAY)
						.append(" - " + Lang.AACH_COMMAND_STATS).toString());

				sender.sendMessage((new StringBuilder())
						.append(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
								+ icon + ChatColor.GRAY + "] ")
						.append(ChatColor.DARK_PURPLE + "/aach top")
						.append(ChatColor.GRAY)
						.append(" - " + Lang.AACH_COMMAND_TOP).toString());

				sender.sendMessage((new StringBuilder())
						.append(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
								+ icon + ChatColor.GRAY + "] ")
						.append(ChatColor.DARK_PURPLE + "/aach reload")
						.append(ChatColor.GRAY)
						.append(" - " + Lang.AACH_COMMAND_RELOAD).toString());

				sender.sendMessage((new StringBuilder())
						.append(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
								+ icon + ChatColor.GRAY + "] ")
						.append(ChatColor.DARK_PURPLE
								+ "/aach give §oach name§r")
						.append(ChatColor.GRAY)
						.append(" - "
								+ ChatColor.translateAlternateColorCodes('&',
										Lang.AACH_COMMAND_GIVE.toString()
												.replace("ACH", "§oach§r&7")
												.replace("NAME", "§oname§r&7")))
						.toString());

				player.sendMessage((new StringBuilder())
						.append(ChatColor.DARK_PURPLE)
						.append("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")
						.toString());
			}
		}

		else if (cmd.getName().equalsIgnoreCase("aach") && (args.length == 3)
				&& args[0].equalsIgnoreCase("give")) {

			achievementCommandGiver.achievementGive(sender, args);

		}
		return true;

	}

	private void getTop(Player player) {

		if (timeAuthorisedTop())
			achievementsTop = db.getTop(topList);

		player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon
				+ ChatColor.GRAY + "] " + Lang.TOP_ACHIEVEMENT);

		for (int i = 0; i < achievementsTop.size(); i += 2) {
			try {
				player.sendMessage(ChatColor.GRAY
						+ "["
						+ ChatColor.DARK_PURPLE
						+ (i + 2)
						/ 2
						+ ChatColor.GRAY
						+ "] "
						+ Bukkit.getServer()
								.getOfflinePlayer(
										UUID.fromString(achievementsTop.get(i)))
								.getName() + " - " + achievementsTop.get(i + 1));
			} catch (Exception ex) {
				this.getLogger()
						.warning("Name corresponding to UUID not found");
			}
		}

	}

	private void getStats(Player player) {

		int achievements = db.countAchievements(player);

		player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon
				+ ChatColor.GRAY + "] " + Lang.NUMBER_ACHIEVEMENTS + " "
				+ ChatColor.DARK_PURPLE + achievements + ChatColor.GRAY + "/"
				+ ChatColor.DARK_PURPLE + totalAchievements);

	}

	private void giveBook(Player player, String name) {

		if (timeAuthorised(player)) {

			if (sound)
				player.getWorld().playSound(player.getLocation(),
						Sound.LEVEL_UP, 1, 0);

			// Up to three achievement books can be generated (= 150
			// achievements received by a player).
			ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
			ItemStack book2 = new ItemStack(Material.WRITTEN_BOOK);
			ItemStack book3 = new ItemStack(Material.WRITTEN_BOOK);
			ArrayList<String> achievements = this.db.getAchievements(player);
			ArrayList<String> pages = new ArrayList<String>();
			ArrayList<String> pages2 = new ArrayList<String>();
			ArrayList<String> pages3 = new ArrayList<String>();

			int i = 0;
			while (i < achievements.size() && i < 150) {
				try {
					String currentAchievement = achievements.get(i) + "\n"
							+ bookSeparator + "\n" + achievements.get(i + 1)
							+ "\n" + bookSeparator + "\n"
							+ achievements.get(i + 2);
					currentAchievement = ChatColor
							.translateAlternateColorCodes('&',
									currentAchievement);
					pages.add(currentAchievement);
					i = i + 3;
				} catch (Exception e) {
					Logger log = this.getLogger();
					log.severe("Error while creating book pages.");
				}

			}

			BookMeta bm = (BookMeta) book.getItemMeta();

			bm.setPages(pages);

			bm.setAuthor(name);

			bm.setTitle(Lang.BOOK_NAME.toString());

			book.setItemMeta(bm);
			if (player.getInventory().firstEmpty() != -1)
				player.getInventory().addItem(new ItemStack[] { book });
			else
				for (ItemStack item : new ItemStack[] { book })
					player.getWorld().dropItem(player.getLocation(), item);

			if (i > 149 && achievements.size() != 150) {
				while (i < achievements.size() && i < 300) {
					try {
						String currentAchievement = achievements.get(i) + "\n"
								+ bookSeparator + "\n"
								+ achievements.get(i + 1) + "\n"
								+ bookSeparator + "\n"
								+ achievements.get(i + 2);
						currentAchievement = ChatColor
								.translateAlternateColorCodes('&',
										currentAchievement);
						pages2.add(currentAchievement);
						i = i + 3;
					} catch (Exception e) {
						Logger log = this.getLogger();
						log.severe("Error while creating book pages.");
					}

				}
				BookMeta bm2 = (BookMeta) book2.getItemMeta();

				bm2.setPages(pages2);

				bm2.setAuthor(name);

				bm2.setTitle(Lang.BOOK_NAME + " 2");

				book2.setItemMeta(bm2);
				if (player.getInventory().firstEmpty() != -1)
					player.getInventory().addItem(new ItemStack[] { book2 });
				else
					for (ItemStack item : new ItemStack[] { book2 })
						player.getWorld().dropItem(player.getLocation(), item);
			}

			if (i > 299 && achievements.size() != 300) {
				while (i < achievements.size() && i < 450) {
					try {
						String currentAchievement = achievements.get(i) + "\n"
								+ bookSeparator + "\n"
								+ achievements.get(i + 1) + "\n"
								+ bookSeparator + "\n"
								+ achievements.get(i + 2);
						currentAchievement = ChatColor
								.translateAlternateColorCodes('&',
										currentAchievement);
						pages3.add(currentAchievement);
						i = i + 3;
					} catch (Exception e) {
						Logger log = this.getLogger();
						log.severe("Error while creating book pages.");
					}

				}
				BookMeta bm3 = (BookMeta) book3.getItemMeta();

				bm3.setPages(pages3);

				bm3.setAuthor(name);

				bm3.setTitle(Lang.BOOK_NAME + " 3");

				book3.setItemMeta(bm3);
				if (player.getInventory().firstEmpty() != -1)
					player.getInventory().addItem(new ItemStack[] { book3 });
				else
					for (ItemStack item : new ItemStack[] { book3 })
						player.getWorld().dropItem(player.getLocation(), item);
			}

			player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
					+ icon + ChatColor.GRAY + "] " + Lang.BOOK_RECEIVED);
		} else {

			player.sendMessage(ChatColor.GRAY
					+ "["
					+ ChatColor.DARK_PURPLE
					+ icon
					+ ChatColor.GRAY
					+ "] "
					+ Lang.BOOK_DELAY.toString().replace("TIME",
							"" + time / 1000));
		}
	}

	private boolean timeAuthorised(Player player) {

		long currentTime = System.currentTimeMillis();
		long lastBookTime = 0;
		if (players.containsKey(player))
			lastBookTime = players.get(player);
		if (currentTime - lastBookTime < time)
			return false;
		players.put(player, currentTime);
		return true;

	}

	private boolean timeAuthorisedTop() {

		long currentTime = System.currentTimeMillis();
		if (currentTime - lastTopTime < 60000)
			return false;
		else
			lastTopTime = System.currentTimeMillis();
		return true;

	}

	public Economy getEconomy() {
		return economy;
	}

	public void setEconomy(Economy economy) {
		this.economy = economy;
	}

	public SQLDatabases getDb() {
		return db;
	}

	public void setDb(SQLDatabases db) {
		this.db = db;
	}

	public AchievementRewards getReward() {
		return reward;
	}

	public void setReward(AchievementRewards reward) {
		this.reward = reward;
	}

	public AchievementDisplay getAchievementDisplay() {
		return achievementDisplay;
	}

	public void setAchievementDisplay(AchievementDisplay achievementDisplay) {
		this.achievementDisplay = achievementDisplay;
	}

	public boolean isUpdateNeeded() {
		return updateNeeded;
	}

	public void setUpdateNeeded(boolean updateNeeded) {
		this.updateNeeded = updateNeeded;
	}

	public AdvancedAchievementsUpdateChecker getUpdateChecker() {
		return updateChecker;
	}

	public void setUpdateChecker(AdvancedAchievementsUpdateChecker updateChecker) {
		this.updateChecker = updateChecker;
	}

	public boolean isRetroVault() {
		return retroVault;
	}

	public void setRetroVault(boolean retroVault) {
		this.retroVault = retroVault;
	}

	public boolean isFirework() {
		return firework;
	}

	public void setFirework(boolean firework) {
		this.firework = firework;
	}

	public int getDatabaseVersion() {
		return databaseVersion;
	}

	public void setDatabaseVersion(int databaseVersion) {
		this.databaseVersion = databaseVersion;
		this.getConfig().set("DatabaseVersion", databaseVersion);
		this.saveConfig();
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public boolean isChatMessage() {
		return chatNotify;
	}

	public void setChatMessage(boolean chatMessage) {
		this.chatNotify = chatMessage;
	}

	public boolean isRestrictCreative() {
		return restrictCreative;
	}

	public void setRestrictCreative(boolean restrictCreative) {
		this.restrictCreative = restrictCreative;
	}

	public boolean isMultiCommand() {
		return multiCommand;
	}

	public void setMultiCommand(boolean multiCommand) {
		this.multiCommand = multiCommand;
	}

	public boolean isRewardCommandNotif() {
		return rewardCommandNotif;
	}

	public void setRewardCommandNotif(boolean rewardCommandNotif) {
		this.rewardCommandNotif = rewardCommandNotif;
	}

	public YamlConfiguration getLang() {
		return LANG;
	}

	public File getLangFile() {
		return LANG_FILE;
	}

}