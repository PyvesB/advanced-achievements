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
import java.util.List;
import java.util.Map.Entry;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.hm.achievement.command.*;
import com.hm.achievement.listener.*;
import com.hm.achievement.db.SQLDatabaseManager;
import com.hm.achievement.db.PooledRequestsSender;
import com.hm.achievement.language.Lang;
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
 * @version 2.2.2
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
	private AchieveTradeAnvilBrewListener inventoryClickListener;
	private AchieveEnchantListener enchantmentListener;
	private AchieveXPListener xpListener;
	private AchieveBedListener bedListener;
	private AchieveQuitListener quitListener;
	private AchieveWorldTPListener worldTPListener;
	private AchieveDropListener dropListener;
	private AchieveHoeFertiliseFireworkListener hoeFertiliseListener;
	private AchieveTameListener tameListener;

	// Additional classes related to plugin modules and commands.
	private AchievementRewards reward;
	private AchievementDisplay achievementDisplay;
	private GiveCommand giveCommand;
	private BookCommand bookCommand;
	private TopCommand topCommand;
	private ListCommand listCommand;
	private StatsCommand statsCommand;
	private InfoCommand infoCommand;
	private HelpCommand helpCommand;
	private CheckCommand checkCommand;
	private DeleteCommand deleteCommand;
	private AdvancedAchievementsUpdateChecker updateChecker;

	// Database related.
	private SQLDatabaseManager db;
	private int databaseVersion;

	// Plugin options and various parameters.
	private String icon;
	private ChatColor color;
	private String chatHeader;
	private boolean restrictCreative;
	private boolean databaseBackup;
	private List<String> excludedWorldList;
	private boolean successfulLoad;
	private int playtimeTaskInterval;
	private int distanceTaskInterval;
	private int pooledRequestsTaskInterval;

	// Achievement types string arrays; constants.
	public static final String[] NORMAL_ACHIEVEMENTS = { "Connections", "Deaths", "Arrows", "Snowballs", "Eggs", "Fish",
			"ItemBreaks", "EatenItems", "Shear", "Milk", "Trades", "AnvilsUsed", "Enchantments", "Beds", "MaxLevel",
			"ConsumedPotions", "PlayedTime", "ItemDrops", "HoePlowings", "Fertilising", "Taming", "Brewing",
			"Fireworks", "DistanceFoot", "DistancePig", "DistanceHorse", "DistanceMinecart", "DistanceBoat",
			"Commands" };
	public static final String[] MULTIPLE_ACHIEVEMENTS = { "Places", "Breaks", "Kills", "Crafts" };

	// Plugin runnable classes.
	private AchieveDistanceRunnable achieveDistanceRunnable = null;
	private AchievePlayTimeRunnable achievePlayTimeRunnable = null;

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
		inventoryClickListener = new AchieveTradeAnvilBrewListener(this);
		enchantmentListener = new AchieveEnchantListener(this);
		xpListener = new AchieveXPListener(this);
		bedListener = new AchieveBedListener(this);
		quitListener = new AchieveQuitListener(this);
		dropListener = new AchieveDropListener(this);
		hoeFertiliseListener = new AchieveHoeFertiliseFireworkListener(this);
		tameListener = new AchieveTameListener(this);
		worldTPListener = new AchieveWorldTPListener(this);

		db = new SQLDatabaseManager(this);

	}

	/**
	 * Called when server is launched or reloaded.
	 */
	@SuppressWarnings("deprecation")
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
				|| this.getConfig().getConfigurationSection("AnvilsUsed").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("Brewing").getKeys(false).size() != 0)
			pm.registerEvents(inventoryClickListener, this);

		if (this.getConfig().getConfigurationSection("Enchantments").getKeys(false).size() != 0)
			pm.registerEvents(enchantmentListener, this);

		if (this.getConfig().getConfigurationSection("MaxLevel").getKeys(false).size() != 0)
			pm.registerEvents(xpListener, this);

		if (this.getConfig().getConfigurationSection("Beds").getKeys(false).size() != 0)
			pm.registerEvents(bedListener, this);

		if (this.getConfig().getConfigurationSection("ItemDrops").getKeys(false).size() != 0)
			pm.registerEvents(dropListener, this);

		if (this.getConfig().getConfigurationSection("Taming").getKeys(false).size() != 0)
			pm.registerEvents(tameListener, this);

		if (this.getConfig().getConfigurationSection("HoePlowings").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("Fertilising").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("Fireworks").getKeys(false).size() != 0)
			pm.registerEvents(hoeFertiliseListener, this);

		if (this.getConfig().getConfigurationSection("PlayedTime").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistanceFoot").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistancePig").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistanceHorse").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistanceMinecart").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistanceBoat").getKeys(false).size() != 0)
			pm.registerEvents(quitListener, this);

		if (this.getConfig().getConfigurationSection("DistanceFoot").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistancePig").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistanceHorse").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistanceMinecart").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistanceBoat").getKeys(false).size() != 0)
			pm.registerEvents(worldTPListener, this);

		// Initialise the SQLite/MySQL database.
		db.initialise(this);

		// Schedule a repeating task to group database queries for some frequent
		// events.
		Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(
				Bukkit.getPluginManager().getPlugin("AdvancedAchievements"), new PooledRequestsSender(this, true),
				pooledRequestsTaskInterval * 40, pooledRequestsTaskInterval * 20);

		// Schedule a repeating task to monitor played time for each player (not
		// directly related to an event).
		if (this.getConfig().getConfigurationSection("PlayedTime").getKeys(false).size() != 0) {
			achievePlayTimeRunnable = new AchievePlayTimeRunnable(this);
			Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(
					Bukkit.getPluginManager().getPlugin("AdvancedAchievements"), achievePlayTimeRunnable,
					playtimeTaskInterval * 10, playtimeTaskInterval * 20);
		}

		// Schedule a repeating task to monitor distances travelled by each
		// player (not directly related to an event).
		if (this.getConfig().getConfigurationSection("DistanceFoot").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistancePig").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistanceHorse").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistanceMinecart").getKeys(false).size() != 0
				|| this.getConfig().getConfigurationSection("DistanceBoat").getKeys(false).size() != 0) {
			achieveDistanceRunnable = new AchieveDistanceRunnable(this);
			Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(
					Bukkit.getPluginManager().getPlugin("AdvancedAchievements"), achieveDistanceRunnable,
					distanceTaskInterval * 40, distanceTaskInterval * 20);
		}

		if (successfulLoad)
			this.getLogger()
					.info("AdvancedAchievements configurations, language file and database successfully loaded!");
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

		registerPermissions();

		// Workaround to keep configuration file comments (Bukkit issue).
		File config = new File(this.getDataFolder(), "config.yml");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(config));

			StringBuilder configString = new StringBuilder("");

			String currentLine;

			while ((currentLine = reader.readLine()) != null) {
				if (currentLine.length() > 0 && currentLine.charAt(0) == '#') {

					currentLine = "COMMENT" + currentLine.replace(" ", "$").replace(":", "&").replace(".", "%")
							+ ": true";
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

		// Added in version 2.1:
		if (!this.getConfig().getKeys(false).contains("AdditionalEffects")) {
			this.getConfig().set("AdditionalEffects", true);
			this.saveConfig();
		}

		if (!this.getConfig().getKeys(false).contains("FireworkStyle")) {
			this.getConfig().set("FireworkStyle", "BALL_LARGE");
			this.saveConfig();
		}

		if (!this.getConfig().getKeys(false).contains("ObfuscateNotReceived")) {
			this.getConfig().set("ObfuscateNotReceived", true);
			this.saveConfig();
		}

		if (!this.getConfig().getKeys(false).contains("HideNotReceivedCategories")) {
			this.getConfig().set("HideNotReceivedCategories", false);
			this.saveConfig();
		}

		// Added in version 2.2:
		if (!this.getConfig().getKeys(false).contains("TitleScreen")) {
			this.getConfig().set("TitleScreen", true);
			this.saveConfig();
		}

		if (!this.getConfig().getKeys(false).contains("Color")) {
			this.getConfig().set("Color", "5");
			this.saveConfig();
		}

		if (!this.getConfig().getKeys(false).contains("TimeBook")) {
			this.getConfig().set("TimeBook", this.getConfig().getInt("Time", 900));
			this.saveConfig();
		}

		if (!this.getConfig().getKeys(false).contains("TimeList")) {
			this.getConfig().set("TimeList", 0);
			this.saveConfig();
		}

		// End of configuration updates.

		try {
			reader = new BufferedReader(new FileReader(config));
			StringBuilder configString = new StringBuilder("");

			String currentLine;

			while ((currentLine = reader.readLine()) != null) {
				if (currentLine.startsWith("COMMENT")) {
					currentLine = currentLine.replace("COMMENT", "").replace(": true", "").replace("$", " ")
							.replace("&", ":").replace("%", ".");
					configString.append(currentLine + "\n");

				} else if (currentLine.length() > 0 && currentLine.charAt(0) != '#' || currentLine.length() == 0)
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
		databaseVersion = this.getConfig().getInt("DatabaseVersion", 1);
		icon = this.getConfig().getString("Icon", "\u2618");
		color = ChatColor.getByChar(this.getConfig().getString("Color", "5").toCharArray()[0]);
		chatHeader = ChatColor.GRAY + "[" + color + icon + ChatColor.GRAY + "] ";
		restrictCreative = this.getConfig().getBoolean("RestrictCreative", false);
		databaseBackup = this.getConfig().getBoolean("DatabaseBackup", true);
		excludedWorldList = this.getConfig().getStringList("ExcludedWorlds");
		playtimeTaskInterval = this.getConfig().getInt("PlaytimeTaskInterval", 150);
		distanceTaskInterval = this.getConfig().getInt("DistanceTaskInterval", 5);
		pooledRequestsTaskInterval = this.getConfig().getInt("PooledRequestsTaskInterval", 60);

		// Initialise command modules.
		reward = new AchievementRewards(this);
		achievementDisplay = new AchievementDisplay(this);
		giveCommand = new GiveCommand(this);
		bookCommand = new BookCommand(this);
		topCommand = new TopCommand(this);
		statsCommand = new StatsCommand(this);
		infoCommand = new InfoCommand(this);
		listCommand = new ListCommand(this);
		helpCommand = new HelpCommand(this);
		checkCommand = new CheckCommand(this);
		deleteCommand = new DeleteCommand(this);

		// Reload achievements in distance and play time runnables only on
		// reload.
		if (achieveDistanceRunnable != null)
			achieveDistanceRunnable.extractAchievementsFromConfig(this);
		if (achievePlayTimeRunnable != null)
			achievePlayTimeRunnable.extractAchievementsFromConfig(this);

		// Check for available plugin update.
		if (this.getConfig().getBoolean("CheckForUpdate", true)) {
			updateChecker = new AdvancedAchievementsUpdateChecker(this);
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
	}

	/**
	 * Register permissions that depend on the user's configuration file (based
	 * on multiple type achievements; for instance for stone breaks,
	 * achievement.count.breaks.stone will be registered).
	 */
	private void registerPermissions() {

		for (int i = 0; i < MULTIPLE_ACHIEVEMENTS.length; i++)
			for (String section : this.getConfig().getConfigurationSection(MULTIPLE_ACHIEVEMENTS[i]).getKeys(false))
				this.getServer().getPluginManager()
						.addPermission(new Permission(
								"achievement.count." + MULTIPLE_ACHIEVEMENTS[i].toLowerCase() + "." + section,
								PermissionDefault.TRUE));
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
		new PooledRequestsSender(this, false).sendRequests();

		if (achievePlayTimeRunnable != null)
			for (Entry<Player, Long> entry : connectionListener.getPlayTime().entrySet())
				this.getDb().updateAndGetPlaytime(entry.getKey(), entry.getValue() + System.currentTimeMillis()
						- connectionListener.getJoinTime().get(entry.getKey()));

		// Send traveled distance stats to the database.
		if (achieveDistanceRunnable != null) {
			for (Entry<Player, Integer> entry : achieveDistanceRunnable.getAchievementDistancesFoot().entrySet())
				this.getDb().updateAndGetDistance(entry.getKey(), entry.getValue(), "distancefoot");

			for (Entry<Player, Integer> entry : achieveDistanceRunnable.getAchievementDistancesPig().entrySet())
				this.getDb().updateAndGetDistance(entry.getKey(), entry.getValue(), "distancepig");

			for (Entry<Player, Integer> entry : achieveDistanceRunnable.getAchievementDistancesHorse().entrySet())
				this.getDb().updateAndGetDistance(entry.getKey(), entry.getValue(), "distancehorse");

			for (Entry<Player, Integer> entry : achieveDistanceRunnable.getAchievementDistancesBoat().entrySet())
				this.getDb().updateAndGetDistance(entry.getKey(), entry.getValue(), "distanceboat");

			for (Entry<Player, Integer> entry : achieveDistanceRunnable.getAchievementDistancesMinecart().entrySet())
				this.getDb().updateAndGetDistance(entry.getKey(), entry.getValue(), "distanceminecart");
		}

		this.getLogger().info("Remaining requests sent to database, plugin disabled.");

	}

	/**
	 * Check if player is in a world in which achievements must not be received.
	 */
	public boolean isInExludedWorld(Player player) {

		if (excludedWorldList.size() == 0)
			return false;

		for (String world : excludedWorldList)
			if (player.getWorld().getName().equals(world))
				return true;

		return false;
	}

	/**
	 * Try to hook up with Vault.
	 */
	public boolean setUpEconomy() {

		if (economy != null)
			return true;

		try {
			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
					.getRegistration(net.milkbowl.vault.economy.Economy.class);
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

		if (!cmd.getName().equalsIgnoreCase("aach"))
			return false;

		if ((args.length == 1) && !args[0].equalsIgnoreCase("help")) {

			if (args[0].equalsIgnoreCase("book") && sender.hasPermission("achievement.book")
					&& sender instanceof Player) {

				bookCommand.giveBook(((Player) sender));

			} else if (args[0].equalsIgnoreCase("reload")) {

				if (sender.hasPermission("achievement.reload")) {

					this.reloadConfig();
					configurationLoad();
					if (successfulLoad)
						sender.sendMessage(chatHeader + Lang.CONFIGURATION_SUCCESSFULLY_RELOADED);
					else
						sender.sendMessage(chatHeader + Lang.CONFIGURATION_RELOAD_FAILED);

				} else {

					sender.sendMessage(chatHeader + Lang.NO_PERMS);
				}
			} else if (args[0].equalsIgnoreCase("stats") && sender instanceof Player) {

				statsCommand.getStats((Player) sender);

			} else if (args[0].equalsIgnoreCase("list") && sender instanceof Player) {

				if (sender.hasPermission("achievement.list")) {
					listCommand.getList((Player) sender);
				} else {

					sender.sendMessage(chatHeader + Lang.NO_PERMS);
				}
			} else if (args[0].equalsIgnoreCase("top")) {

				topCommand.getTop(sender);

			} else if (args[0].equalsIgnoreCase("info")) {

				infoCommand.getInfo(sender);
			} else {

				helpCommand.getHelp(sender);
			}
		} else if ((args.length == 3) && args[0].equalsIgnoreCase("give")) {

			if (sender.hasPermission("achievement.give")) {

				giveCommand.achievementGive(sender, args);

			} else {

				sender.sendMessage(chatHeader + Lang.NO_PERMS);
			}

		} else if ((args.length >= 3) && args[0].equalsIgnoreCase("check")) {

			if (sender.hasPermission("achievement.check")) {

				checkCommand.achievementCheck(sender, args);

			} else {

				sender.sendMessage(chatHeader + Lang.NO_PERMS);
			}

		} else if ((args.length >= 3) && args[0].equalsIgnoreCase("delete")) {

			if (sender.hasPermission("achievement.delete")) {

				deleteCommand.achievementDelete(sender, args);

			} else {

				sender.sendMessage(chatHeader + Lang.NO_PERMS);
			}
		} else {

			helpCommand.getHelp(sender);
		}

		return true;

	}

	/**
	 * Various getters and setters.
	 */

	public Economy getEconomy() {

		return economy;
	}

	public SQLDatabaseManager getDb() {

		return db;
	}

	public AchievementRewards getReward() {

		return reward;
	}

	public AchievementDisplay getAchievementDisplay() {

		return achievementDisplay;
	}

	public AdvancedAchievementsUpdateChecker getUpdateChecker() {

		return updateChecker;
	}

	public int getDatabaseVersion() {

		return databaseVersion;
	}

	public void setDatabaseVersion(int databaseVersion) {

		this.databaseVersion = databaseVersion;
		this.getConfig().set("DatabaseVersion", databaseVersion);
		this.saveConfig();
	}

	public String getChatHeader() {

		return chatHeader;
	}

	public boolean isRestrictCreative() {

		return restrictCreative;
	}

	public boolean isSuccessfulLoad() {

		return successfulLoad;
	}

	public void setSuccessfulLoad(boolean successfulLoad) {

		this.successfulLoad = successfulLoad;
	}

	public BookCommand getAchievementBookCommand() {

		return bookCommand;
	}

	public ListCommand getAchievementListCommand() {

		return listCommand;
	}

	public AchieveDistanceRunnable getAchieveDistanceRunnable() {

		return achieveDistanceRunnable;
	}

	public AchievePlayTimeRunnable getAchievePlayTimeRunnable() {

		return achievePlayTimeRunnable;
	}

	public AchieveConnectionListener getConnectionListener() {

		return connectionListener;
	}

	public String getIcon() {

		return icon;
	}

	public ChatColor getColor() {

		return color;
	}

}
