package com.hm.achievement;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import org.mcstats.MetricsLite;

import net.milkbowl.vault.economy.Economy;

import com.hm.achievement.command.*;
import com.hm.achievement.db.*;
import com.hm.achievement.listener.*;
import com.hm.achievement.runnable.*;
import com.hm.achievement.utils.*;

/**
 * Advanced Achievements enables unique and challenging achievements on your
 * server. Try to collect as many as you can, earn rewards, climb the rankings
 * and receive RP books!
 * 
 * Some minor parts of the code and ideas are based on Achievement plugin by
 * Death_marine and captainawesome7, under Federation of Lost Lawn Chairs
 * license (http://dev.bukkit.org/licenses/1332-federation-of-lost-lawn-chairs).
 * 
 * AdvancedAchievements is under GNU General Public License version 3. Please
 * visit the plugin's GitHub for more information :
 * https://github.com/PyvesB/AdvancedAchievements
 * 
 * Official plugin's server: hellominecraft.fr
 * 
 * Bukkit project page: dev.bukkit.org/bukkit-plugins/advanced-achievements
 * Spigot project page: spigotmc.org/resources/advanced-achievements.6239
 * 
 * @since April 2015
 * @version 2.6-Beta
 * @author DarkPyves
 */

public class AdvancedAchievements extends JavaPlugin {

	// Used for Vault plugin integration.
	private Economy economy;

	// Listeners, to monitor events and manage stats.
	private AchieveConnectionListener connectionListener;
	private AchieveDeathListener deathListener;
	private AchieveArrowListener arrowListener;
	private AchieveSnowballEggListener snowballEggListener;
	private AchieveFishListener fishListener;
	private AchieveItemBreakListener itemBreakListener;
	private AchieveConsumeListener consumeListener;
	private AchieveShearListener shearListener;
	private AchieveMilkListener milkListener;
	private AchieveTradeAnvilBrewListener inventoryClickListener;
	private AchieveEnchantListener enchantmentListener;
	private AchieveBedListener bedListener;
	private AchieveXPListener xpListener;
	private AchieveDropListener dropListener;
	private AchieveHoeFertiliseFireworkMusicListener hoeFertiliseFireworkMusicListener;
	private AchieveTameListener tameListener;
	private AchieveBlockPlaceListener blockPlaceListener;
	private AchieveBlockBreakListener blockBreakListener;
	private AchieveKillListener killListener;
	private AchieveCraftListener craftListener;
	private AchieveQuitListener quitListener;
	private AchieveTeleportRespawnListener teleportRespawnListener;

	private ListGUIListener listGUIListener;

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
	private UpdateChecker updateChecker;

	private YamlManager config;
	private YamlManager lang;
	private FileManager fileManager;

	// Database related.
	private SQLDatabaseManager db;
	private DatabasePoolsManager poolsManager;
	private int pooledRequestsTaskInterval;
	private boolean databaseBackup;
	private boolean asyncPooledRequestsSender;

	// Plugin options and various parameters.
	private String icon;
	private ChatColor color;
	private String chatHeader;
	private boolean restrictCreative;
	private Set<String> excludedWorldSet;
	private Set<String> disabledCategorySet;
	private boolean successfulLoad;
	private boolean overrideDisable;
	private int playtimeTaskInterval;
	private int distanceTaskInterval;

	// Achievement types string arrays; constants.
	public static final String[] NORMAL_ACHIEVEMENTS = { "Connections", "Deaths", "Arrows", "Snowballs", "Eggs", "Fish",
			"ItemBreaks", "EatenItems", "Shear", "Milk", "Trades", "AnvilsUsed", "Enchantments", "Beds", "MaxLevel",
			"ConsumedPotions", "PlayedTime", "ItemDrops", "HoePlowings", "Fertilising", "Taming", "Brewing",
			"Fireworks", "MusicDiscs", "EnderPearls", "DistanceFoot", "DistancePig", "DistanceHorse",
			"DistanceMinecart", "DistanceBoat", "DistanceGliding", "Commands" };
	public static final String[] MULTIPLE_ACHIEVEMENTS = { "Places", "Breaks", "Kills", "Crafts" };

	// Plugin runnable classes.
	private AchieveDistanceRunnable achieveDistanceRunnable;
	private AchievePlayTimeRunnable achievePlayTimeRunnable;

	// Bukkit scheduler tasks.
	private BukkitTask pooledRequestsSenderTask;
	private BukkitTask playedTimeTask;
	private BukkitTask distanceTask;

	/**
	 * Constructor.
	 */
	public AdvancedAchievements() {

		overrideDisable = false;
		excludedWorldSet = new HashSet<String>();
		disabledCategorySet = new HashSet<String>();
		fileManager = new FileManager(this);
		db = new SQLDatabaseManager(this);
		poolsManager = new DatabasePoolsManager(this);

	}

	/**
	 * Called when server is launched or reloaded.
	 */
	@Override
	public void onEnable() {

		// Start enabling plugin.
		long startTime = System.currentTimeMillis();

		configurationLoad();

		// Error while loading .yml files; do not do any further work.
		if (overrideDisable) {
			overrideDisable = false;
			return;
		}

		this.getLogger().info("Registering listeners...");

		// Register listeners so they can monitor server events; if there are no
		// config related achievements, listeners aren't registered.
		PluginManager pm = getServer().getPluginManager();
		if (!disabledCategorySet.contains("Places")) {
			blockPlaceListener = new AchieveBlockPlaceListener(this);
			pm.registerEvents(blockPlaceListener, this);
		}

		if (!disabledCategorySet.contains("Breaks")) {
			blockBreakListener = new AchieveBlockBreakListener(this);
			pm.registerEvents(blockBreakListener, this);
		}

		if (!disabledCategorySet.contains("Kills")) {
			killListener = new AchieveKillListener(this);
			pm.registerEvents(killListener, this);
		}

		if (!disabledCategorySet.contains("Crafts")) {
			craftListener = new AchieveCraftListener(this);
			pm.registerEvents(craftListener, this);
		}

		if (!disabledCategorySet.contains("Deaths")) {
			deathListener = new AchieveDeathListener(this);
			pm.registerEvents(deathListener, this);
		}

		if (!disabledCategorySet.contains("Arrows")) {
			arrowListener = new AchieveArrowListener(this);
			pm.registerEvents(arrowListener, this);
		}

		if (!disabledCategorySet.contains("Snowballs") || !disabledCategorySet.contains("Eggs")) {
			snowballEggListener = new AchieveSnowballEggListener(this);
			pm.registerEvents(snowballEggListener, this);
		}

		if (!disabledCategorySet.contains("Fish")) {
			fishListener = new AchieveFishListener(this);
			pm.registerEvents(fishListener, this);
		}

		if (!disabledCategorySet.contains("ItemBreaks")) {
			itemBreakListener = new AchieveItemBreakListener(this);
			pm.registerEvents(itemBreakListener, this);
		}

		if (!disabledCategorySet.contains("ConsumedPotions") || !disabledCategorySet.contains("EatenItems")) {
			consumeListener = new AchieveConsumeListener(this);
			pm.registerEvents(consumeListener, this);
		}

		if (!disabledCategorySet.contains("Shear")) {
			shearListener = new AchieveShearListener(this);
			pm.registerEvents(shearListener, this);
		}

		if (!disabledCategorySet.contains("Milk")) {
			milkListener = new AchieveMilkListener(this);
			pm.registerEvents(milkListener, this);
		}

		if (config.getBoolean("CheckForUpdate", true) || !disabledCategorySet.contains("Connections")
				|| !disabledCategorySet.contains("PlayedTime")) {
			connectionListener = new AchieveConnectionListener(this);
			pm.registerEvents(connectionListener, this);
		}

		if (!disabledCategorySet.contains("Trades") || !disabledCategorySet.contains("AnvilsUsed")
				|| !disabledCategorySet.contains("Brewing")) {
			inventoryClickListener = new AchieveTradeAnvilBrewListener(this);
			pm.registerEvents(inventoryClickListener, this);
		}

		if (!disabledCategorySet.contains("Enchantments")) {
			enchantmentListener = new AchieveEnchantListener(this);
			pm.registerEvents(enchantmentListener, this);
		}

		if (!disabledCategorySet.contains("MaxLevel")) {
			xpListener = new AchieveXPListener(this);
			pm.registerEvents(xpListener, this);
		}

		if (!disabledCategorySet.contains("Beds")) {
			bedListener = new AchieveBedListener(this);
			pm.registerEvents(bedListener, this);
		}

		if (!disabledCategorySet.contains("ItemDrops")) {
			dropListener = new AchieveDropListener(this);
			pm.registerEvents(dropListener, this);
		}

		if (!disabledCategorySet.contains("Taming")) {
			tameListener = new AchieveTameListener(this);
			pm.registerEvents(tameListener, this);
		}

		if (!disabledCategorySet.contains("HoePlowings") || !disabledCategorySet.contains("Fertilising")
				|| !disabledCategorySet.contains("Fireworks") || !disabledCategorySet.contains("MusicDiscs")) {
			hoeFertiliseFireworkMusicListener = new AchieveHoeFertiliseFireworkMusicListener(this);
			pm.registerEvents(hoeFertiliseFireworkMusicListener, this);
		}

		if (!disabledCategorySet.contains("MaxLevel") || !disabledCategorySet.contains("PlayedTime")
				|| !disabledCategorySet.contains("DistanceFoot") || !disabledCategorySet.contains("DistancePig")
				|| !disabledCategorySet.contains("DistanceHorse") || !disabledCategorySet.contains("DistanceMinecart")
				|| !disabledCategorySet.contains("DistanceBoat") || !disabledCategorySet.contains("DistanceGliding")) {
			quitListener = new AchieveQuitListener(this);
			pm.registerEvents(quitListener, this);
		}

		if (!disabledCategorySet.contains("DistanceFoot") || !disabledCategorySet.contains("DistancePig")
				|| !disabledCategorySet.contains("DistanceHorse") || !disabledCategorySet.contains("DistanceMinecart")
				|| !disabledCategorySet.contains("DistanceBoat") || !disabledCategorySet.contains("DistanceGliding")
				|| !disabledCategorySet.contains("EnderPearls")) {
			teleportRespawnListener = new AchieveTeleportRespawnListener(this);
			pm.registerEvents(teleportRespawnListener, this);
		}

		listGUIListener = new ListGUIListener(this);
		pm.registerEvents(listGUIListener, this);

		this.getLogger().info("Initialising database and launching scheduled tasks...");

		// Initialise the SQLite/MySQL database.
		db.initialise();

		// Error while loading database do not do any further work.
		if (overrideDisable) {
			overrideDisable = false;
			return;
		}

		// Schedule a repeating task to group database queries for some frequent
		// events. Choose between asynchronous task and synchronous task.
		if (asyncPooledRequestsSender)
			pooledRequestsSenderTask = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(
					Bukkit.getPluginManager().getPlugin("AdvancedAchievements"),
					new PooledRequestsSenderAsync(this, true), pooledRequestsTaskInterval * 40,
					pooledRequestsTaskInterval * 20);
		else
			pooledRequestsSenderTask = Bukkit.getServer().getScheduler().runTaskTimer(
					Bukkit.getPluginManager().getPlugin("AdvancedAchievements"),
					new PooledRequestsSenderSync(this, true), pooledRequestsTaskInterval * 40,
					pooledRequestsTaskInterval * 20);

		// Schedule a repeating task to monitor played time for each player (not
		// directly related to an event).
		if (!disabledCategorySet.contains("PlayedTime")) {
			achievePlayTimeRunnable = new AchievePlayTimeRunnable(this);
			playedTimeTask = Bukkit.getServer().getScheduler().runTaskTimer(
					Bukkit.getPluginManager().getPlugin("AdvancedAchievements"), achievePlayTimeRunnable,
					playtimeTaskInterval * 10, playtimeTaskInterval * 20);
		}

		// Schedule a repeating task to monitor distances travelled by each
		// player (not directly related to an event).
		if (!disabledCategorySet.contains("DistanceFoot") || !disabledCategorySet.contains("DistancePig")
				|| !disabledCategorySet.contains("DistanceHorse") || !disabledCategorySet.contains("DistanceMinecart")
				|| !disabledCategorySet.contains("DistanceBoat") || !disabledCategorySet.contains("DistanceGliding")) {
			achieveDistanceRunnable = new AchieveDistanceRunnable(this);
			distanceTask = Bukkit.getServer().getScheduler().runTaskTimer(
					Bukkit.getPluginManager().getPlugin("AdvancedAchievements"), achieveDistanceRunnable,
					distanceTaskInterval * 40, distanceTaskInterval * 20);
		}

		if (successfulLoad)
			this.getLogger().info("Plugin successfully enabled and ready to run! Took "
					+ (System.currentTimeMillis() - startTime) + "ms.");
		else
			this.getLogger().severe("Error(s) while loading plugin. Please view previous logs for more information.");
	}

	/**
	 * Load plugin configuration and set values to different parameters; load
	 * language file and backup configuration and database files. Register
	 * permissions. Initialise command modules.
	 */
	@SuppressWarnings("unchecked")
	private void configurationLoad() {

		successfulLoad = true;

		this.getLogger().info("Backing up and loading configuration files...");

		try {
			config = fileManager.getNewConfig("config.yml");
		} catch (IOException e) {
			this.getLogger().severe("Error while loading configuration file.");
			e.printStackTrace();
			successfulLoad = false;
		} catch (InvalidConfigurationException e) {
			this.getLogger().severe("Error while loading configuration file, disabling plugin.");
			this.getLogger().severe(
					"Verify your syntax using the following logs and by visiting yaml-online-parser.appspot.com");
			e.printStackTrace();
			successfulLoad = false;
			overrideDisable = true;
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		try {
			lang = fileManager.getNewConfig("lang.yml");
		} catch (IOException e) {
			this.getLogger().severe("Error while loading language file.");
			e.printStackTrace();
			successfulLoad = false;
		} catch (InvalidConfigurationException e) {
			this.getLogger().severe("Error while loading language file, disabling plugin.");
			this.getLogger().severe(
					"Verify your syntax using the following logs and by visiting yaml-online-parser.appspot.com");
			e.printStackTrace();
			successfulLoad = false;
			overrideDisable = true;
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		try {
			fileManager.backupFile("config.yml");
		} catch (IOException e) {
			this.getLogger().severe("Error while backing up configuration file.");
			e.printStackTrace();
			successfulLoad = false;
		}

		try {
			fileManager.backupFile("lang.yml");
		} catch (IOException e) {
			this.getLogger().severe("Error while backing up language file.");
			e.printStackTrace();
			successfulLoad = false;
		}

		// Update configurations from previous versions of the plugin.
		updateOldConfiguration();
		updateOldLanguage();

		this.getLogger().info("Loading configs, registering permissions and initialising command modules...");

		// Load parameters.
		icon = StringEscapeUtils.unescapeJava(config.getString("Icon", "\u2618"));
		color = ChatColor.getByChar(config.getString("Color", "5").toCharArray()[0]);
		chatHeader = ChatColor.GRAY + "[" + color + icon + ChatColor.GRAY + "] ";
		restrictCreative = config.getBoolean("RestrictCreative", false);
		databaseBackup = config.getBoolean("DatabaseBackup", true);
		for (String world : (List<String>) config.getList("ExcludedWorlds"))
			excludedWorldSet.add(world);
		for (String category : (List<String>) config.getList("DisabledCategories"))
			disabledCategorySet.add(category);
		playtimeTaskInterval = config.getInt("PlaytimeTaskInterval", 150);
		distanceTaskInterval = config.getInt("DistanceTaskInterval", 5);
		pooledRequestsTaskInterval = config.getInt("PooledRequestsTaskInterval", 60);
		asyncPooledRequestsSender = config.getBoolean("AsyncPooledRequestsSender", true);

		registerPermissions();

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

		// Reload achievements in distance, max level and play time runnables
		// only on plugin reload.
		if (achieveDistanceRunnable != null)
			achieveDistanceRunnable.extractAchievementsFromConfig(this);
		if (achievePlayTimeRunnable != null)
			achievePlayTimeRunnable.extractAchievementsFromConfig(this);
		if (xpListener != null)
			xpListener.extractAchievementsFromConfig(this);

		// Check for available plugin update.
		if (config.getBoolean("CheckForUpdate", true)) {
			updateChecker = new UpdateChecker(this);
		}

		// Load Metrics Lite.
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			this.getLogger().severe("Error while sending Metrics statistics.");
			successfulLoad = false;
		}

		if (databaseBackup && !config.getString("DatabaseType", "sqlite").equalsIgnoreCase("mysql")) {
			File backup = new File(this.getDataFolder(), "achievements.db.bak");
			// Only do a daily backup for the .db file.
			if (System.currentTimeMillis() - backup.lastModified() > 86400000 || backup.length() == 0) {
				this.getLogger().info("Backing up database file...");
				try {
					fileManager.backupFile("achievements.db");
				} catch (IOException e) {
					this.getLogger().severe("Error while backing up database file.");
					e.printStackTrace();
					successfulLoad = false;
				}
			}
		}
	}

	/**
	 * Update configuration file from older plugin versions by adding missing
	 * parameters. Upgrades from versions prior to 2.0 are not supported.
	 */
	@SuppressWarnings("unchecked")
	private void updateOldConfiguration() {

		boolean updateDone = false;

		// Added in version 2.5.2 (put first to enable adding elements to it):
		if (!config.getKeys(false).contains("DisabledCategories")) {
			List<String> list = new ArrayList<String>();
			config.set("DisabledCategories", list,
					new String[] {
							"Don't show these categories in the achievement GUI or in the stats output (delete the [] before using).",
							"Also prevent obtaining achievements for these categories and prevent stats from increasing.",
							"If changed, do a full server reload, and not just /aach reload." });
			updateDone = true;
		}

		// Added in version 2.1:
		if (!config.getKeys(false).contains("AdditionalEffects")) {
			config.set("AdditionalEffects", true,
					"Set to true to activate particle effects when receiving book and for players in top list.");
			updateDone = true;
		}

		if (!config.getKeys(false).contains("FireworkStyle")) {
			config.set("FireworkStyle", "BALL_LARGE", "Choose BALL_LARGE, BALL, BURST, CREEPER or STAR.");
			updateDone = true;
		}

		if (!config.getKeys(false).contains("ObfuscateNotReceived")) {
			config.set("ObfuscateNotReceived", true,
					"Obfuscate achievements that have not yet been received in /aach list.");
			updateDone = true;
		}

		if (!config.getKeys(false).contains("HideNotReceivedCategories")) {
			config.set("HideNotReceivedCategories", false,
					"Hide categories with no achievements yet received in /aach list.");
			updateDone = true;
		}

		// Added in version 2.2:
		if (!config.getKeys(false).contains("TitleScreen")) {
			config.set("TitleScreen", true, "Display achievement name and description as screen titles.");
			updateDone = true;
		}

		if (!config.getKeys(false).contains("Color")) {
			config.set("Color", "5", "Set the color of the plugin (default: 5, dark purple).");
			updateDone = true;
		}

		if (!config.getKeys(false).contains("TimeBook")) {
			config.set("TimeBook", 900, "Time in seconds between each /aach book.");
			updateDone = true;
		}

		if (!config.getKeys(false).contains("TimeList")) {
			config.set("TimeList", 0, "Time in seconds between each /aach list.");
			updateDone = true;
		}

		if (!config.getKeys(false).contains("Brewing")) {
			HashMap<Object, Object> emptyMap = new HashMap<Object, Object>();
			config.set("Brewing", emptyMap, "When a potion is brewed.");
			// As no achievements are set, we initially disable this new
			// category.
			List<String> disabledCategories = (List<String>) config.getList("DisabledCategories");
			disabledCategories.add("Brewing");
			config.set("DisabledCategories", disabledCategories);
			updateDone = true;
		}

		if (!config.getKeys(false).contains("Taming")) {
			HashMap<Object, Object> emptyMap = new HashMap<Object, Object>();
			config.set("Taming", emptyMap, "When an animal is tamed.");
			// As no achievements are set, we initially disable this new
			// category.
			List<String> disabledCategories = (List<String>) config.getList("DisabledCategories");
			disabledCategories.add("Taming");
			config.set("DisabledCategories", disabledCategories);
			updateDone = true;
		}

		// Added in version 2.3:
		if (!config.getKeys(false).contains("Fireworks")) {
			HashMap<Object, Object> emptyMap = new HashMap<Object, Object>();
			// As no achievements are set, we initially disable this new
			// category.
			List<String> disabledCategories = (List<String>) config.getList("DisabledCategories");
			disabledCategories.add("Fireworks");
			config.set("DisabledCategories", disabledCategories);
			config.set("Fireworks", emptyMap, "When a firework is launched.");
			updateDone = true;
		}

		// Added in version 2.3.2:
		if (!config.getKeys(false).contains("AsyncPooledRequestsSender")) {
			config.set("AsyncPooledRequestsSender", true, "Enable multithreading for database write operations.");
			updateDone = true;
		}

		// Added in version 2.5:
		if (!config.getKeys(false).contains("DistanceGliding")) {
			HashMap<Object, Object> emptyMap = new HashMap<Object, Object>();
			config.set("DistanceGliding", emptyMap, new String[] { "When a distance is traveled with elytra.",
					"(ignored on Minecraft versions prior to 1.9)" });
			// As no achievements are set, we initially disable this new
			// category.
			List<String> disabledCategories = (List<String>) config.getList("DisabledCategories");
			disabledCategories.add("DistanceGliding");
			config.set("DisabledCategories", disabledCategories);
			updateDone = true;
		}

		if (!config.getKeys(false).contains("MusicDiscs")) {
			HashMap<Object, Object> emptyMap = new HashMap<Object, Object>();
			config.set("MusicDiscs", emptyMap, "When a music disc is played.");
			// As no achievements are set, we initially disable this new
			// category.
			List<String> disabledCategories = (List<String>) config.getList("DisabledCategories");
			disabledCategories.add("MusicDiscs");
			config.set("DisabledCategories", disabledCategories);
			updateDone = true;
		}

		if (!config.getKeys(false).contains("EnderPearls")) {
			HashMap<Object, Object> emptyMap = new HashMap<Object, Object>();
			config.set("EnderPearls", emptyMap, "When a player teleports with an enderpearl.");
			// As no achievements are set, we initially disable this new
			// category.
			List<String> disabledCategories = (List<String>) config.getList("DisabledCategories");
			disabledCategories.add("EnderPearls");
			config.set("DisabledCategories", disabledCategories);
			updateDone = true;
		}

		// Added in version 2.5.2:
		if (!config.getKeys(false).contains("ListAchievementFormat")) {
			config.set("ListAchievementFormat", "%ICON% %NAME% %ICON%",
					"Set the format of the achievement name in /aach list.");
			updateDone = true;
		}

		if (!config.getKeys(false).contains("HideRewardDisplayInList")) {
			config.set("HideRewardDisplayInList", false, "Hide the reward display in /aach list.");
			updateDone = true;
		}

		if (!config.getKeys(false).contains("IgnoreVerticalDistance")) {
			config.set("IgnoreVerticalDistance", false,
					"Ignore vertical dimension (Y axis) when calculating distance statistics.");
			updateDone = true;
		}

		if (!config.getKeys(false).contains("TablePrefix")) {
			config.set("TablePrefix", "", new String[] {
					"Prefix added to the tables in the database. If you switch from the default tables names (no prefix),",
					"the plugin will attempt an automatic renaming. Otherwise you have to rename your tables manually." });
			updateDone = true;
		}

		if (updateDone) {
			// Changes in the configuration: save and do a fresh load.
			try {
				config.saveConfig();
				config.reloadConfig();
			} catch (IOException e) {
				this.getLogger().severe("Error while saving changes to the configuration file.");
				e.printStackTrace();
				successfulLoad = false;
			}
		}
	}

	/**
	 * Update language file from older plugin versions by adding missing
	 * parameters. Upgrades from versions prior to 2.3 are not supported.
	 */
	private void updateOldLanguage() {

		boolean updateDone = false;

		// Added in version 2.5:
		if (!lang.getKeys(false).contains("list-distance-gliding")) {
			lang.set("list-distance-gliding", "Distance Travelled with Elytra");
			updateDone = true;
		}

		if (!lang.getKeys(false).contains("list-musicdiscs")) {
			lang.set("list-musicdiscs", "Music Discs Played");
			updateDone = true;
		}

		if (!lang.getKeys(false).contains("list-enderpearls")) {
			lang.set("list-enderpearls", "Teleportations with Ender Pearls");
			updateDone = true;
		}

		// Added in version 2.5.2:
		if (!lang.getKeys(false).contains("list-achievement-received")) {
			lang.set("list-achievement-received", "&a\u2713&f ");
			updateDone = true;
		}

		if (!lang.getKeys(false).contains("list-achievement-not-received")) {
			lang.set("list-achievement-not-received", "&4\u2717&8 ");
			updateDone = true;
		}

		if (updateDone) {
			// Changes in the language file: save and do a fresh load.
			try {
				lang.saveConfig();
				lang.reloadConfig();
			} catch (IOException e) {
				this.getLogger().severe("Error while saving changes to the language file.");
				e.printStackTrace();
				successfulLoad = false;
			}
		}
	}

	/**
	 * Register permissions that depend on the user's configuration file (based
	 * on multiple type achievements; for instance for stone breaks,
	 * achievement.count.breaks.stone will be registered).
	 */
	private void registerPermissions() {

		for (int i = 0; i < MULTIPLE_ACHIEVEMENTS.length; i++)
			for (String section : config.getConfigurationSection(MULTIPLE_ACHIEVEMENTS[i]).getKeys(false)) {
				// Bukkit only allows permissions to be set once, so must do
				// additional check for /aach reload correctness.
				if (this.getServer().getPluginManager().getPermission(
						"achievement.count." + MULTIPLE_ACHIEVEMENTS[i].toLowerCase() + "." + section) == null)
					this.getServer().getPluginManager()
							.addPermission(new Permission(
									"achievement.count." + MULTIPLE_ACHIEVEMENTS[i].toLowerCase() + "." + section,
									PermissionDefault.TRUE));
			}
	}

	/**
	 * Called when server is stopped or reloaded.
	 */
	@Override
	public void onDisable() {

		// Error while loading .yml files or database; do not do any further
		// work.
		if (overrideDisable)
			return;

		// Cancel scheduled tasks.
		if (pooledRequestsSenderTask != null)
			pooledRequestsSenderTask.cancel();
		if (playedTimeTask != null)
			playedTimeTask.cancel();
		if (distanceTask != null)
			distanceTask.cancel();

		// Send remaining stats for pooled events to the database.
		new PooledRequestsSenderSync(this, false).sendRequests();

		// Send played time stats to the database, forcing synchronous writes.
		if (achievePlayTimeRunnable != null)
			for (Entry<String, Long> entry : connectionListener.getPlayTime().entrySet())
				this.getDb().updateAndGetPlaytime(entry.getKey(), entry.getValue() + System.currentTimeMillis()
						- connectionListener.getJoinTime().get(entry.getKey()));

		// Send traveled distance stats to the database, forcing synchronous
		// writes.
		if (achieveDistanceRunnable != null) {
			for (Entry<String, Integer> entry : achieveDistanceRunnable.getAchievementDistancesFoot().entrySet())
				this.getDb().updateAndGetDistance(entry.getKey(), entry.getValue(), "distancefoot");

			for (Entry<String, Integer> entry : achieveDistanceRunnable.getAchievementDistancesPig().entrySet())
				this.getDb().updateAndGetDistance(entry.getKey(), entry.getValue(), "distancepig");

			for (Entry<String, Integer> entry : achieveDistanceRunnable.getAchievementDistancesHorse().entrySet())
				this.getDb().updateAndGetDistance(entry.getKey(), entry.getValue(), "distancehorse");

			for (Entry<String, Integer> entry : achieveDistanceRunnable.getAchievementDistancesBoat().entrySet())
				this.getDb().updateAndGetDistance(entry.getKey(), entry.getValue(), "distanceboat");

			for (Entry<String, Integer> entry : achieveDistanceRunnable.getAchievementDistancesMinecart().entrySet())
				this.getDb().updateAndGetDistance(entry.getKey(), entry.getValue(), "distanceminecart");

			for (Entry<String, Integer> entry : achieveDistanceRunnable.getAchievementDistancesGliding().entrySet())
				this.getDb().updateAndGetDistance(entry.getKey(), entry.getValue(), "distancegliding");
		}

		try {
			if (this.getDb().getSQLConnection() != null)
				this.getDb().getSQLConnection().close();
		} catch (SQLException e) {
			this.getLogger().severe("Error while closing connection to database.");
			e.printStackTrace();
		}

		this.getLogger().info("Remaining requests sent to database, plugin disabled.");

	}

	/**
	 * Check if player is in a world in which achievements must not be received.
	 */
	public boolean isInExludedWorld(Player player) {

		if (excludedWorldSet.isEmpty())
			return false;

		return excludedWorldSet.contains(player.getWorld().getName());
	}

	/**
	 * Try to hook up with Vault.
	 */
	public boolean setUpEconomy(boolean log) {

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
			if (log)
				this.getLogger().warning("Attempt to hook up with Vault failed. Money reward ignored.");
			return false;
		}
	}

	/**
	 * Called when a player or the console enters a command. Handles command
	 * directly or dispatches to one of the command modules.
	 */
	@Override
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
					if (successfulLoad) {
						if (sender instanceof Player)
							sender.sendMessage(chatHeader + lang.getString("configuration-successfully-reloaded",
									"Configuration successfully reloaded."));
						this.getLogger().info("Configuration successfully reloaded.");
					} else {
						sender.sendMessage(chatHeader + lang.getString("configuration-reload-failed",
								"Errors while reloading configuration. Please view logs for more details."));
						this.getLogger()
								.severe("Errors while reloading configuration. Please view logs for more details.");
					}

				} else {

					sender.sendMessage(chatHeader
							+ lang.getString("no-permissions", "You do not have the permission to do this."));
				}
			} else if (args[0].equalsIgnoreCase("stats") && sender instanceof Player) {

				statsCommand.getStats((Player) sender);

			} else if (args[0].equalsIgnoreCase("list") && sender instanceof Player) {

				if (sender.hasPermission("achievement.list")) {
					listCommand.createMainGUI((Player) sender);
				} else {

					sender.sendMessage(chatHeader
							+ lang.getString("no-permissions", "You do not have the permission to do this."));
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

				sender.sendMessage(
						chatHeader + lang.getString("no-permissions", "You do not have the permission to do this."));
			}

		} else if ((args.length >= 3) && args[0].equalsIgnoreCase("check")) {

			if (sender.hasPermission("achievement.check")) {

				checkCommand.achievementCheck(sender, args);

			} else {

				sender.sendMessage(
						chatHeader + lang.getString("no-permissions", "You do not have the permission to do this."));
			}

		} else if ((args.length >= 3) && args[0].equalsIgnoreCase("delete")) {

			if (sender.hasPermission("achievement.delete")) {

				deleteCommand.achievementDelete(sender, args);

			} else {

				sender.sendMessage(
						chatHeader + lang.getString("no-permissions", "You do not have the permission to do this."));
			}
		} else {

			helpCommand.getHelp(sender);
		}

		return true;

	}

	// Various getters and setters. Names are self-explanatory.

	public Economy getEconomy() {

		return economy;
	}

	public SQLDatabaseManager getDb() {

		return db;
	}

	public DatabasePoolsManager getPoolsManager() {

		return poolsManager;
	}

	public AchievementRewards getReward() {

		return reward;
	}

	public AchievementDisplay getAchievementDisplay() {

		return achievementDisplay;
	}

	public UpdateChecker getUpdateChecker() {

		return updateChecker;
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

	public Set<String> getDisabledCategorySet() {

		return disabledCategorySet;
	}

	public void setOverrideDisable(boolean overrideDisable) {

		this.overrideDisable = overrideDisable;
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

	public AchieveXPListener getXpListener() {

		return xpListener;
	}

	public String getIcon() {

		return icon;
	}

	public ChatColor getColor() {

		return color;
	}

	public boolean isAsyncPooledRequestsSender() {

		return asyncPooledRequestsSender;
	}

	public YamlManager getPluginConfig() {

		return config;
	}

	public YamlManager getPluginLang() {

		return lang;
	}
}
