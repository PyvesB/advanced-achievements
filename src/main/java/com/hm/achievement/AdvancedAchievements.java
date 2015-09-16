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

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import com.hm.achievement.db.SQLDatabases;
import com.hm.achievement.db.SendPooledRequests;
import com.hm.achievement.language.Lang;
import com.hm.achievement.listener.AchieveArrowListener;
import com.hm.achievement.listener.AchieveBedListener;
import com.hm.achievement.listener.AchieveBlockBreakListener;
import com.hm.achievement.listener.AchieveBlockPlaceListener;
import com.hm.achievement.listener.AchieveItemBreakListener;
import com.hm.achievement.listener.AchieveConnectionListener;
import com.hm.achievement.listener.AchieveConsumeListener;
import com.hm.achievement.listener.AchieveCraftListener;
import com.hm.achievement.listener.AchieveDeathListener;
import com.hm.achievement.listener.AchieveDropListener;
import com.hm.achievement.listener.AchieveEnchantListener;
import com.hm.achievement.listener.AchieveKillListener;
import com.hm.achievement.listener.AchieveFishListener;
import com.hm.achievement.listener.AchieveHoeFertiliseListener;
import com.hm.achievement.listener.AchieveTradeAnvilListener;
import com.hm.achievement.listener.AchieveMilkListener;
import com.hm.achievement.listener.AchieveQuitListener;
import com.hm.achievement.listener.AchieveShearListener;
import com.hm.achievement.listener.AchieveSnowballEggsListener;
import com.hm.achievement.listener.AchieveWorldTPListener;
import com.hm.achievement.listener.AchieveXPListener;
import com.hm.achievement.metrics.MetricsLite;
import com.hm.achievement.runnable.AchieveDistanceRunnable;
import com.hm.achievement.runnable.AchievePlayTimeRunnable;

/**
 * Advanced Achievements enables unique and challenging achievements on your
 * server. Try to collect as many as you can, earn rewards, climb the rankings
 * and receive RP books! Some minor parts of the code and ideas are based on
 * Achievement plugin by Death_marine and captainawesome7, under Federation of
 * Lost Lawn Chairs license
 * (http://dev.bukkit.org/licenses/1332-federation-of-lost-lawn-chairs)
 * AdvancedAchievements is under GNU General Public License version 3. Please
 * visit the plugin's GitHub for more information :
 * https://github.com/PyvesB/AdvancedAchievements
 * 
 * @since April 2015
 * @version 2.0
 * @author DarkPyves
 */

public class AdvancedAchievements extends JavaPlugin {

	// Used for Vault plugin integration.
	private Economy economy;

	// Listeners, to monitor events and manage stats.
	private AchieveBlockPlaceListener blockPlaceListener;
	private AchieveBlockBreakListener blockBreakListener;
	private AchieveKillListener entityListener;
	private AchieveCraftListener craftListener;
	private AchieveDeathListener deathListener;
	private AchieveArrowListener arrowListener;
	private AchieveSnowballEggsListener snowballListener;
	private AchieveFishListener fishListener;
	private AchieveItemBreakListener itemBreakListener;
	private AchieveConsumeListener eatenItemsListener;
	private AchieveShearListener shearListener;
	private AchieveMilkListener milkListener;
	private AchieveConnectionListener connectionListener;
	private AchieveTradeAnvilListener inventoryClickListener;
	private AchieveEnchantListener enchantmentListener;
	private AchieveXPListener xpListener;
	private AchieveBedListener bedListener;
	private AchieveQuitListener quitListener;
	private AchieveWorldTPListener worldTPListener;
	private AchieveDropListener dropListener;
	private AchieveHoeFertiliseListener hoeFertiliseListener;

	// Additional classes related to plugin modules.
	private AchievementRewards reward;
	private AchievementDisplay achievementDisplay;
	private AchievementCommandGiver achievementCommandGiver;
	private AchievementBookGiver achievementBookGiver;
	private AdvancedAchievementsUpdateChecker updateChecker;

	// Database related.
	private SQLDatabases db;
	private int databaseVersion;

	// Plugin options and various parameters.
	private HashMap<Player, Long> players;
	private int bookTime;
	private boolean retroVault;
	private boolean firework;
	private boolean sound;
	private boolean chatNotify;
	private String icon;
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
	private boolean updateNeeded;
	private boolean successfulLoad;

	/**
	 * Constructor.
	 */
	public AdvancedAchievements() {

		economy = null;

		blockPlaceListener = new AchieveBlockPlaceListener(this);
		blockBreakListener = new AchieveBlockBreakListener(this);
		entityListener = new AchieveKillListener(this);
		craftListener = new AchieveCraftListener(this);
		deathListener = new AchieveDeathListener(this);
		arrowListener = new AchieveArrowListener(this);
		snowballListener = new AchieveSnowballEggsListener(this);
		fishListener = new AchieveFishListener(this);
		itemBreakListener = new AchieveItemBreakListener(this);
		eatenItemsListener = new AchieveConsumeListener(this);
		shearListener = new AchieveShearListener(this);
		milkListener = new AchieveMilkListener(this);
		connectionListener = new AchieveConnectionListener(this);
		inventoryClickListener = new AchieveTradeAnvilListener(this);
		enchantmentListener = new AchieveEnchantListener(this);
		xpListener = new AchieveXPListener(this);
		bedListener = new AchieveBedListener(this);
		quitListener = new AchieveQuitListener(this);
		dropListener = new AchieveDropListener(this);
		hoeFertiliseListener = new AchieveHoeFertiliseListener(this);
		worldTPListener = new AchieveWorldTPListener();

		achievementDisplay = new AchievementDisplay(this);
		reward = new AchievementRewards(this);
		achievementCommandGiver = new AchievementCommandGiver(this);
		achievementBookGiver = new AchievementBookGiver(this);

		db = new SQLDatabases();

		players = new HashMap<Player, Long>();

	}

	/**
	 * Called when server is launched or reloaded.
	 */
	public void onEnable() {

		if (!this.getDataFolder().exists())
			this.getDataFolder().mkdir();

		configurationLoad();

		// Register listeners so they can monitor server events; if there are no
		// config related achievements, listeners aren't registered.
		PluginManager pm = getServer().getPluginManager();
		if (this.getConfig().getConfigurationSection("Places").getKeys(false).size() != 0)
			pm.registerEvents(blockPlaceListener, this);

		if (this.getConfig().getConfigurationSection("Breaks").getKeys(false).size() != 0)
			pm.registerEvents(blockBreakListener, this);

		if (this.getConfig().getConfigurationSection("Kills").getKeys(false).size() != 0)
			pm.registerEvents(entityListener, this);

		if (this.getConfig().getConfigurationSection("Crafts").getKeys(false).size() != 0)
			pm.registerEvents(craftListener, this);

		if (this.getConfig().getConfigurationSection("Deaths").getKeys(false).size() != 0)
			pm.registerEvents(deathListener, this);

		if (this.getConfig().getConfigurationSection("Arrows").getKeys(false).size() != 0)
			pm.registerEvents(arrowListener, this);

		if (this.getConfig().getConfigurationSection("Snowballs").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("Eggs").getKeys(false).size() != 0)
			pm.registerEvents(snowballListener, this);

		if (this.getConfig().getConfigurationSection("Fish").getKeys(false).size() != 0)
			pm.registerEvents(fishListener, this);

		if (this.getConfig().getConfigurationSection("ItemBreaks").getKeys(false).size() != 0)
			pm.registerEvents(itemBreakListener, this);

		if (this.getConfig().getConfigurationSection("ConsumedPotions").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("EatenItems").getKeys(false).size() != 0)
			pm.registerEvents(eatenItemsListener, this);

		if (this.getConfig().getConfigurationSection("Shear").getKeys(false).size() != 0)
			pm.registerEvents(shearListener, this);

		if (this.getConfig().getConfigurationSection("Milk").getKeys(false).size() != 0)
			pm.registerEvents(milkListener, this);

		if (this.getConfig().getConfigurationSection("Connections").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("PlayedTime").getKeys(false).size() != 0)
			pm.registerEvents(connectionListener, this);

		if (this.getConfig().getConfigurationSection("Trades").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("AnvilsUsed").getKeys(false).size() != 0)
			pm.registerEvents(inventoryClickListener, this);

		if (this.getConfig().getConfigurationSection("Enchantments").getKeys(false).size() != 0)
			pm.registerEvents(enchantmentListener, this);

		if (this.getConfig().getConfigurationSection("MaxLevel").getKeys(false).size() != 0)
			pm.registerEvents(xpListener, this);

		if (this.getConfig().getConfigurationSection("Beds").getKeys(false).size() != 0)
			pm.registerEvents(bedListener, this);

		if (this.getConfig().getConfigurationSection("ItemDrops").getKeys(false).size() != 0)
			pm.registerEvents(dropListener, this);

		if (this.getConfig().getConfigurationSection("HoePlowings").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("Fertilising").getKeys(false).size() != 0)
			pm.registerEvents(hoeFertiliseListener, this);

		if (this.getConfig().getConfigurationSection("PlayedTime").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistanceFoot").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistancePig").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistanceHorse").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistanceMinecart").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistanceBoat").getKeys(false).size() != 0) {
			pm.registerEvents(quitListener, this);
			pm.registerEvents(worldTPListener, this);
		}

		// Initialise the SQLite/MySQL database.
		db.initialise(this);

		// Schedule a repeating task to group database queries for some frequent
		// events.
		Bukkit.getServer()
				.getScheduler()
				.scheduleSyncRepeatingTask(Bukkit.getPluginManager().getPlugin("AdvancedAchievements"),
						new SendPooledRequests(this, true), 2400, 1200);

		// Schedule a repeating task to monitor played time for each player (not
		// directly related to an event).
		if (this.getConfig().getConfigurationSection("PlayedTime").getKeys(false).size() != 0)
			Bukkit.getServer()
					.getScheduler()
					.scheduleSyncRepeatingTask(Bukkit.getPluginManager().getPlugin("AdvancedAchievements"),
							new AchievePlayTimeRunnable(this), 6000, 6000);

		// Schedule a repeating task to monitor distances travelled by each
		// player (not
		// directly related to an event).
		if (this.getConfig().getConfigurationSection("DistanceFoot").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistancePig").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistanceHorse").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistanceMinecart").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistanceBoat").getKeys(false).size() != 0)
			Bukkit.getServer()
					.getScheduler()
					.scheduleSyncRepeatingTask(Bukkit.getPluginManager().getPlugin("AdvancedAchievements"),
							new AchieveDistanceRunnable(this), 100, 100);

		if (successfulLoad)
			this.getLogger().info(
					"AdvancedAchievements configurations, language file and database successfully loaded!");
		else
			this.getLogger().severe("Error(s) while loading plugin. Please view previous logs for more information.");
	}

	/**
	 * Load plugin configuration and set values to different parameters; load
	 * language file and backup configuration and database files.
	 */
	private void configurationLoad() {

		successfulLoad = true;

		backupConfigFile();
		backupLanguageFile();

		loadLang();
		this.saveDefaultConfig();

		// Workaround to keep configuration file comments (Bukkit issue).
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
			this.getLogger().severe("Saving comments in configuration file failed.");
			successfulLoad = false;
		}

		// Update configurations from older plugin versions by adding missing
		// parameters in config file.

		// Added in version 1.1:
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

		// Added in version 1.4:
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

		// Added in version 1.5:
		if (!this.getConfig().getKeys(false).contains("ChatNotify")) {
			this.getConfig().set("ChatNotify", false);
			this.saveConfig();

		}

		if (!this.getConfig().getKeys(false).contains("BookSeparator")) {
			this.getConfig().set("BookSeparator", "");
			this.saveConfig();

		}

		// Added in version 1.6:
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

		// End of configuration updates.

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
			this.getLogger().severe("Saving comments in configuration file failed.");
			successfulLoad = false;
		}

		// Load parameters.
		bookTime = this.getConfig().getInt("Time", 900) * 1000;
		retroVault = this.getConfig().getBoolean("RetroVault", false);
		firework = this.getConfig().getBoolean("Firework", true);
		sound = this.getConfig().getBoolean("Sound", true);
		databaseVersion = this.getConfig().getInt("DatabaseVersion", 1);
		icon = this.getConfig().getString("Icon", "\u2618");
		chatNotify = this.getConfig().getBoolean("ChatNotify", false);
		bookSeparator = this.getConfig().getString("BookSeparator", "");
		restrictCreative = this.getConfig().getBoolean("RestrictCreative", false);
		multiCommand = this.getConfig().getBoolean("MultiCommand", true);
		rewardCommandNotif = this.getConfig().getBoolean("RewardCommandNotif", true);
		databaseBackup = this.getConfig().getBoolean("DatabaseBackup", true);
		excludedWorldList = this.getConfig().getStringList("ExcludedWorlds");
		topList = this.getConfig().getInt("TopList", 5);

		lastTopTime = 0;

		// Check for available plugin update.
		if (this.getConfig().getBoolean("CheckForUpdate", true)) {
			updateChecker = new AdvancedAchievementsUpdateChecker(this,
					"http://dev.bukkit.org/bukkit-plugins/advanced-achievements/files.rss");
			updateNeeded = updateChecker.updateNeeded();
		}

		// Load Metrics Lite.
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			this.getLogger().severe("Error while sending Metrics statistics.");
			successfulLoad = false;
		}

		backupDBFile();

		// Calculate the total number of achievements in the config file.
		totalAchievements = 0;
		totalAchievements += this.getConfig().getConfigurationSection("Connections").getKeys(false).size();
		for (String item : this.getConfig().getConfigurationSection("Places").getKeys(false))
			totalAchievements += this.getConfig().getConfigurationSection("Places." + item).getKeys(false).size();
		for (String item : this.getConfig().getConfigurationSection("Breaks").getKeys(false))
			totalAchievements += this.getConfig().getConfigurationSection("Breaks." + item).getKeys(false).size();
		for (String item : this.getConfig().getConfigurationSection("Kills").getKeys(false))
			totalAchievements += this.getConfig().getConfigurationSection("Kills." + item).getKeys(false).size();
		for (String item : this.getConfig().getConfigurationSection("Crafts").getKeys(false))
			totalAchievements += this.getConfig().getConfigurationSection("Crafts." + item).getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("Deaths").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("Arrows").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("Snowballs").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("Eggs").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("Fish").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("ItemBreaks").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("EatenItems").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("Shear").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("Milk").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("Trades").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("AnvilsUsed").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("Enchantments").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("Beds").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("MaxLevel").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("ConsumedPotions").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("PlayedTime").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("DistanceFoot").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("DistancePig").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("DistanceHorse").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("DistanceMinecart").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("DistanceBoat").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("ItemDrops").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("HoePlowings").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("Fertilising").getKeys(false).size();
		totalAchievements += this.getConfig().getConfigurationSection("Commands").getKeys(false).size();
	}

	/**
	 * Backup configuration file (config.yml.bak).
	 */
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
				this.getLogger().info("Successfully backed up configuration file.");

			} catch (FileNotFoundException e) {

				this.getLogger().severe("Error while backing up configuration file.");
				e.printStackTrace();
				successfulLoad = false;
			} catch (IOException e) {

				this.getLogger().severe("Error while backing up configuration file.");
				e.printStackTrace();
				successfulLoad = false;
			}
		}

	}

	/**
	 * Backup database file (achievements.db.bak).
	 */
	private void backupDBFile() {

		if (!databaseBackup || !this.getConfig().getString("DatabaseType", "sqlite").equalsIgnoreCase("sqlite"))
			return;

		File original = new File(this.getDataFolder(), "achievements.db");
		File backup = new File(this.getDataFolder(), "achievements.db.bak");

		// Update only if previous file older than one day.
		if ((System.currentTimeMillis() - backup.lastModified() > 86400000 || backup.length() == 0)
				&& original.length() != 0) {
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

				this.getLogger().severe("Error while backing up database file.");
				e.printStackTrace();
				successfulLoad = false;
			} catch (IOException e) {

				this.getLogger().severe("Error while backing up database file.");
				e.printStackTrace();
				successfulLoad = false;
			}
		}

	}

	/**
	 * Load the lang.yml file.
	 */
	public void loadLang() {

		File lang = new File(getDataFolder(), "lang.yml");
		if (!lang.exists()) {
			try {
				getDataFolder().mkdir();
				lang.createNewFile();
				Reader defConfigStream = new InputStreamReader(this.getResource("lang.yml"), "UTF8");
				if (defConfigStream != null) {
					YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
					defConfig.save(lang);
					Lang.setFile(defConfig);
					return;
				}
			} catch (IOException e) {

				this.getLogger().severe("Error while creating language file.");
				e.printStackTrace();
				successfulLoad = false;
			}
		}
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
		for (Lang item : Lang.values()) {
			if (conf.getString(item.getPath()) == null) {
				conf.set(item.getPath(), item.getDefault());
			}
		}
		Lang.setFile(conf);
		try {
			conf.save(lang);
		} catch (IOException e) {

			this.getLogger().severe("Error while saving language file.");
			e.printStackTrace();
			successfulLoad = false;
		}
	}

	/**
	 * Backup language file (lang.yml.bak).
	 */
	private void backupLanguageFile() {

		File original = new File(this.getDataFolder(), "lang.yml");
		File backup = new File(this.getDataFolder(), "lang.yml.bak");
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
				this.getLogger().info("Successfully backed up language file.");

			} catch (FileNotFoundException e) {

				this.getLogger().severe("Error while backing up language file.");
				e.printStackTrace();
				successfulLoad = false;
			} catch (IOException e) {

				this.getLogger().severe("Error while backing up language file.");
				e.printStackTrace();
				successfulLoad = false;
			}
		}

	}

	/**
	 * Called when server is stopped or reloaded.
	 */
	public void onDisable() {

		// Send remaining stats for pooled events to the database.
		new SendPooledRequests(this, false).sendRequests();

		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			// Send played time stats to the database.
			if (this.getConfig().getConfigurationSection("PlayedTime").getKeys(false).size() != 0
					&& AchieveConnectionListener.getPlayTime().containsKey(player))
				this.getDb().registerPlaytime(
						player,
						AchieveConnectionListener.getPlayTime().get(player) + System.currentTimeMillis()
								- AchieveConnectionListener.getJoinTime().get(player));

			// Send travelled distance stats to the database.
			if ((this.getConfig().getConfigurationSection("DistanceFoot").getKeys(false).size() != 0
					|| this.getConfig().getConfigurationSection("DistancePig").getKeys(false).size() != 0
					|| this.getConfig().getConfigurationSection("DistanceHorse").getKeys(false).size() != 0
					|| this.getConfig().getConfigurationSection("DistanceMinecart").getKeys(false).size() != 0 || this
					.getConfig().getConfigurationSection("DistanceBoat").getKeys(false).size() != 0)
					&& AchieveDistanceRunnable.getAchievementLocations().containsKey(player)) {
				this.getDb().registerDistance(player,
						AchieveDistanceRunnable.getAchievementDistancesFoot().get(player), "distancefoot");

				this.getDb().registerDistance(player, AchieveDistanceRunnable.getAchievementDistancesPig().get(player),
						"distancepig");

				this.getDb().registerDistance(player,
						AchieveDistanceRunnable.getAchievementDistancesHorse().get(player), "distancehorse");

				this.getDb().registerDistance(player,
						AchieveDistanceRunnable.getAchievementDistancesBoat().get(player), "distanceboat");

				this.getDb().registerDistance(player,
						AchieveDistanceRunnable.getAchievementDistancesMinecart().get(player), "distanceminecart");
			}
		}

		this.getLogger().info("Remaining requests sent to database, plugin disabled.");

	}

	/**
	 * Check if player is in a world in which achievements must not be received.
	 */
	public boolean isInExludedWorld(Player player) {

		if (excludedWorldList.size() == 0)
			return false;

		for (int i = 0; i < excludedWorldList.size(); i++) {
			if (player.getWorld().getName().equalsIgnoreCase(excludedWorldList.get(i)))
				return true;
		}

		return false;
	}

	/**
	 * Try to hook up with Vault.
	 */
	public boolean setUpEconomy() {

		try {
			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(
					net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
			}

			return (economy != null);
		} catch (NoClassDefFoundError e) {
			this.getLogger().severe("Attempt to hook up with Vault failed.");
			return false;
		}
	}

	/**
	 * Called when a player or the console enters a command.
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]) {

		if (cmd.getName().equalsIgnoreCase("aach") && (args.length == 1) && !args[0].equalsIgnoreCase("help")) {

			if (args[0].equalsIgnoreCase("book") && sender.hasPermission("achievement.book")
					&& sender instanceof Player) {

				achievementBookGiver.giveBook(((Player) sender), ((Player) sender).getName());

			} else if (args[0].equalsIgnoreCase("reload")) {

				if (sender.hasPermission("achievement.reload")) {

					this.reloadConfig();
					configurationLoad();
					if (successfulLoad)
						sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] "
								+ Lang.CONFIGURATION_SUCCESSFULLY_RELOADED);
					else
						sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] "
								+ Lang.CONFIGURATION_RELOAD_FAILED);

				} else {

					sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] "
							+ Lang.NO_PERMS);

				}
			} else if (args[0].equalsIgnoreCase("stats") && sender instanceof Player) {

				getStats((Player) sender);

			} else if (args[0].equalsIgnoreCase("list") && sender instanceof Player) {

				if (sender.hasPermission("achievement.list"))
					getList((Player) sender);
				else {

					sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] "
							+ Lang.NO_PERMS);

				}

			} else if (args[0].equalsIgnoreCase("top")) {

				getTop(sender);

			} else if (args[0].equalsIgnoreCase("info")) {

				getInfo(sender);

			}
		} else if (cmd.getName().equalsIgnoreCase("aach") && (args.length == 3) && args[0].equalsIgnoreCase("give")) {

			if (sender.hasPermission("achievement.give")) {

				achievementCommandGiver.achievementGive(sender, args);

			} else {

				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] "
						+ Lang.NO_PERMS);

			}
		} else if (cmd.getName().equalsIgnoreCase("aach") || (args.length == 1) && !args[0].equalsIgnoreCase("help")) {

			sender.sendMessage((new StringBuilder()).append(ChatColor.DARK_PURPLE).append("-=-=-=-=-=-=-")
					.append(ChatColor.GRAY).append("[").append(ChatColor.DARK_PURPLE).append(icon)
					.append("§lAdvanced Achievements").append(ChatColor.DARK_PURPLE).append(icon)
					.append(ChatColor.GRAY).append("]").append(ChatColor.DARK_PURPLE).append("-=-=-=-=-=-=-")
					.toString());

			sender.sendMessage((new StringBuilder())
					.append(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] ")
					.append(ChatColor.DARK_PURPLE + "/aach book").append(ChatColor.GRAY)
					.append(" - " + Lang.AACH_COMMAND_BOOK).toString());

			sender.sendMessage((new StringBuilder())
					.append(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] ")
					.append(ChatColor.DARK_PURPLE + "/aach stats").append(ChatColor.GRAY)
					.append(" - " + Lang.AACH_COMMAND_STATS).toString());

			sender.sendMessage((new StringBuilder())
					.append(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] ")
					.append(ChatColor.DARK_PURPLE + "/aach list").append(ChatColor.GRAY)
					.append(" - " + Lang.AACH_COMMAND_LIST).toString());

			sender.sendMessage((new StringBuilder())
					.append(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] ")
					.append(ChatColor.DARK_PURPLE + "/aach top").append(ChatColor.GRAY)
					.append(" - " + Lang.AACH_COMMAND_TOP).toString());

			sender.sendMessage((new StringBuilder())
					.append(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] ")
					.append(ChatColor.DARK_PURPLE + "/aach give §oach name§r")
					.append(ChatColor.GRAY)
					.append(" - "
							+ ChatColor.translateAlternateColorCodes(
									'&',
									Lang.AACH_COMMAND_GIVE.toString().replace("ACH", "§oach§r&7")
											.replace("NAME", "§oname§r&7"))).toString());

			sender.sendMessage((new StringBuilder())
					.append(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] ")
					.append(ChatColor.DARK_PURPLE + "/aach reload").append(ChatColor.GRAY)
					.append(" - " + Lang.AACH_COMMAND_RELOAD).toString());

			sender.sendMessage((new StringBuilder())
					.append(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] ")
					.append(ChatColor.DARK_PURPLE + "/aach info").append(ChatColor.GRAY)
					.append(" - " + Lang.AACH_COMMAND_INFO).toString());

			sender.sendMessage((new StringBuilder()).append(ChatColor.DARK_PURPLE)
					.append("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-").toString());
		}

		return true;

	}

	/**
	 * Get list of players with the most achievements and diplay player's rank.
	 */
	private void getTop(CommandSender sender) {

		long currentTime = System.currentTimeMillis();
		if (currentTime - lastTopTime >= 60000) {

			achievementsTop = db.getTop(topList);
			lastTopTime = System.currentTimeMillis();
		}

		sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] "
				+ Lang.TOP_ACHIEVEMENT);

		for (int i = 0; i < achievementsTop.size(); i += 2) {
			try {
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + (i + 2) / 2 + ChatColor.GRAY + "] "
						+ Bukkit.getServer().getOfflinePlayer(UUID.fromString(achievementsTop.get(i))).getName()
						+ " - " + achievementsTop.get(i + 1));
			} catch (Exception ex) {
				this.getLogger().warning("Top command: name corresponding to UUID not found.");
			}
		}

		if (sender instanceof Player) {
			int rank = db.getRank(db.countAchievements((Player) sender));
			int totalPlayers = db.getTotalPlayers();
			sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] "
					+ Lang.PLAYER_RANK + " " + ChatColor.DARK_PURPLE + rank + ChatColor.GRAY + "/"
					+ ChatColor.DARK_PURPLE + totalPlayers);
		}
	}

	/**
	 * Get statistics of the player by displaying number of achievements
	 * received and total number of achievements.
	 */
	private void getStats(Player player) {

		int achievements = db.countAchievements(player);

		// Display number of achievements received and total achievements.
		player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] "
				+ Lang.NUMBER_ACHIEVEMENTS + " " + ChatColor.DARK_PURPLE + achievements + ChatColor.GRAY + "/"
				+ ChatColor.DARK_PURPLE + totalAchievements);

		// Display progress bar.
		String barDisplay = "";
		for (int i = 1; i <= 145; i++) {
			if (i < (145 * achievements) / totalAchievements)
				barDisplay = barDisplay + "&5|";
			else {
				barDisplay = barDisplay + "&8|";
			}
		}
		player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] " + "["
				+ ChatColor.translateAlternateColorCodes('&', barDisplay) + ChatColor.GRAY + "]");

	}

	/**
	 * Display name of received achievements and name of missing achievements
	 * (goes through the entire config file).
	 */
	public void getList(Player player) {

		String achievementsList = "";

		for (String ach : this.getConfig().getConfigurationSection("Connections").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("Connections." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("Connections." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("Connections." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String section : this.getConfig().getConfigurationSection("Places").getKeys(false))
			for (String ach : this.getConfig().getConfigurationSection("Places." + section).getKeys(false))
				if (db.hasAchievement(player, this.getConfig().getString("Places." + section + "." + ach + ".Name", "")))
					achievementsList += "&5"
							+ this.getConfig().getString("Places." + section + "." + ach + ".Name", "")
									.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
				else
					achievementsList += "&8§o"
							+ this.getConfig().getString("Places." + section + "." + ach + ".Name", "")
									.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String section : this.getConfig().getConfigurationSection("Breaks").getKeys(false))
			for (String ach : this.getConfig().getConfigurationSection("Breaks." + section).getKeys(false))
				if (db.hasAchievement(player, this.getConfig().getString("Breaks." + section + "." + ach + ".Name", "")))
					achievementsList += "&5"
							+ this.getConfig().getString("Breaks." + section + "." + ach + ".Name", "")
									.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
				else
					achievementsList += "&8§o"
							+ this.getConfig().getString("Breaks." + section + "." + ach + ".Name", "")
									.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String section : this.getConfig().getConfigurationSection("Kills").getKeys(false))
			for (String ach : this.getConfig().getConfigurationSection("Kills." + section).getKeys(false))
				if (db.hasAchievement(player, this.getConfig().getString("Kills." + section + "." + ach + ".Name", "")))
					achievementsList += "&5"
							+ this.getConfig().getString("Kills." + section + "." + ach + ".Name", "")
									.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
				else
					achievementsList += "&8§o"
							+ this.getConfig().getString("Kills." + section + "." + ach + ".Name", "")
									.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String section : this.getConfig().getConfigurationSection("Crafts").getKeys(false))
			for (String ach : this.getConfig().getConfigurationSection("Crafts." + section).getKeys(false))
				if (db.hasAchievement(player, this.getConfig().getString("Crafts." + section + "." + ach + ".Name", "")))
					achievementsList += "&5"
							+ this.getConfig().getString("Crafts." + section + "." + ach + ".Name", "")
									.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
				else
					achievementsList += "&8§o"
							+ this.getConfig().getString("Crafts." + section + "." + ach + ".Name", "")
									.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("Deaths").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("Deaths." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("Deaths." + ach + ".Name", "").replaceAll("&([a-f]|[0-9]){1}", "")
						+ " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("Deaths." + ach + ".Name", "").replaceAll("&([a-f]|[0-9]){1}", "")
						+ " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("Arrows").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("Arrows." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("Arrows." + ach + ".Name", "").replaceAll("&([a-f]|[0-9]){1}", "")
						+ " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("Arrows." + ach + ".Name", "").replaceAll("&([a-f]|[0-9]){1}", "")
						+ " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("Snowballs").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("Snowballs." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("Snowballs." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("Snowballs." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("Eggs").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("Eggs." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("Eggs." + ach + ".Name", "").replaceAll("&([a-f]|[0-9]){1}", "")
						+ " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("Eggs." + ach + ".Name", "").replaceAll("&([a-f]|[0-9]){1}", "")
						+ " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("Fish").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("Fish." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("Fish." + ach + ".Name", "").replaceAll("&([a-f]|[0-9]){1}", "")
						+ " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("Fish." + ach + ".Name", "").replaceAll("&([a-f]|[0-9]){1}", "")
						+ " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("ItemBreaks").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("ItemBreaks." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("ItemBreaks." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("ItemBreaks." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("EatenItems").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("EatenItems." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("EatenItems." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("EatenItems." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("Shear").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("Shear." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("Shear." + ach + ".Name", "").replaceAll("&([a-f]|[0-9]){1}", "")
						+ " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("Shear." + ach + ".Name", "").replaceAll("&([a-f]|[0-9]){1}", "")
						+ " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("Milk").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("Milk." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("Milk." + ach + ".Name", "").replaceAll("&([a-f]|[0-9]){1}", "")
						+ " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("Milk." + ach + ".Name", "").replaceAll("&([a-f]|[0-9]){1}", "")
						+ " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("Trades").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("Trades." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("Trades." + ach + ".Name", "").replaceAll("&([a-f]|[0-9]){1}", "")
						+ " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("Trades." + ach + ".Name", "").replaceAll("&([a-f]|[0-9]){1}", "")
						+ " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("AnvilsUsed").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("AnvilsUsed." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("AnvilsUsed." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("AnvilsUsed." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("Enchantments").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("Enchantments." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("Enchantments." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("Enchantments." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("Beds").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("Beds." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("Beds." + ach + ".Name", "").replaceAll("&([a-f]|[0-9]){1}", "")
						+ " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("Beds." + ach + ".Name", "").replaceAll("&([a-f]|[0-9]){1}", "")
						+ " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("MaxLevel").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("MaxLevel." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("MaxLevel." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("MaxLevel." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("ConsumedPotions").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("ConsumedPotions." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("ConsumedPotions." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("ConsumedPotions." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("PlayedTime").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("PlayedTime." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("PlayedTime." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("PlayedTime." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("DistanceFoot").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("DistanceFoot." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("DistanceFoot." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("DistanceFoot." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("DistancePig").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("DistancePig." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("DistancePig." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("DistancePig." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("DistanceHorse").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("DistanceHorse." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("DistanceHorse." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("DistanceHorse." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("DistanceMinecart").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("DistanceMinecart." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("DistanceMinecart." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("DistanceMinecart." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("DistanceBoat").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("DistanceBoat." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("DistanceBoat." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("DistanceBoat." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("ItemDrops").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("ItemDrops." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("ItemDrops." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("ItemDrops." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("HoePlowings").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("HoePlowings." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("HoePlowings." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("HoePlowings." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("Fertilising").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("Fertilising." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("Fertilising." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("Fertilising." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		for (String ach : this.getConfig().getConfigurationSection("Commands").getKeys(false))
			if (db.hasAchievement(player, this.getConfig().getString("Commands." + ach + ".Name", "")))
				achievementsList += "&5"
						+ this.getConfig().getString("Commands." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";
			else
				achievementsList += "&8§o"
						+ this.getConfig().getString("Commands." + ach + ".Name", "")
								.replaceAll("&([a-f]|[0-9]){1}", "") + " &7" + icon + " ";

		player.sendMessage(ChatColor.GRAY + " " + icon + " "
				+ ChatColor.translateAlternateColorCodes('&', achievementsList));
	}

	/**
	 * Display information about the plugin.
	 */
	private void getInfo(CommandSender sender) {

		sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] "
				+ ChatColor.DARK_PURPLE + Lang.VERSION_COMMAND_NAME + " " + ChatColor.GRAY
				+ this.getDescription().getName());
		sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] "
				+ ChatColor.DARK_PURPLE + Lang.VERSION_COMMAND_VERSION + " " + ChatColor.GRAY
				+ this.getDescription().getVersion());
		sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] "
				+ ChatColor.DARK_PURPLE + Lang.VERSION_COMMAND_WEBSITE + " " + ChatColor.GRAY
				+ this.getDescription().getWebsite());
		sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] "
				+ ChatColor.DARK_PURPLE + Lang.VERSION_COMMAND_AUTHOR + " " + ChatColor.GRAY
				+ this.getDescription().getAuthors().get(0));
		sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] "
				+ ChatColor.DARK_PURPLE + Lang.VERSION_COMMAND_DESCRIPTION + " " + ChatColor.GRAY
				+ Lang.VERSION_COMMAND_DESCRIPTION_DETAILS);
		if (setUpEconomy())
			sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] "
					+ ChatColor.DARK_PURPLE + Lang.VERSION_COMMAND_VAULT + " " + ChatColor.GRAY + "YES");
		else
			sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] "
					+ ChatColor.DARK_PURPLE + Lang.VERSION_COMMAND_VAULT + " " + ChatColor.GRAY + "NO");
		if (this.getConfig().getString("DatabaseType", "sqlite").equalsIgnoreCase("mysql"))
			sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] "
					+ ChatColor.DARK_PURPLE + Lang.VERSION_COMMAND_DATABASE + " " + ChatColor.GRAY + "MySQL");
		else
			sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + icon + ChatColor.GRAY + "] "
					+ ChatColor.DARK_PURPLE + Lang.VERSION_COMMAND_DATABASE + " " + ChatColor.GRAY + "SQLite");

	}

	/**
	 * Various getters and setters.
	 */

	public Economy getEconomy() {

		return economy;
	}

	public SQLDatabases getDb() {

		return db;
	}

	public AchievementRewards getReward() {

		return reward;
	}

	public AchievementDisplay getAchievementDisplay() {

		return achievementDisplay;
	}

	public boolean isUpdateNeeded() {

		return updateNeeded;
	}

	public AdvancedAchievementsUpdateChecker getUpdateChecker() {

		return updateChecker;
	}

	public boolean isRetroVault() {

		return retroVault;
	}

	public boolean isFirework() {

		return firework;
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

	public boolean isChatMessage() {

		return chatNotify;
	}

	public boolean isRestrictCreative() {

		return restrictCreative;
	}

	public boolean isMultiCommand() {

		return multiCommand;
	}

	public boolean isRewardCommandNotif() {

		return rewardCommandNotif;
	}

	public boolean isSound() {

		return sound;
	}

	public String getBookSeparator() {

		return bookSeparator;
	}

	public int getBookTime() {

		return bookTime;
	}

	public HashMap<Player, Long> getPlayers() {

		return players;
	}

	public boolean isSuccessfulLoad() {

		return successfulLoad;
	}

	public void setSuccessfulLoad(boolean successfulLoad) {

		this.successfulLoad = successfulLoad;
	}

}
