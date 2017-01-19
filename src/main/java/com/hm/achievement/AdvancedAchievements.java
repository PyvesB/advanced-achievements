package com.hm.achievement;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.mcstats.MetricsLite;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.command.BookCommand;
import com.hm.achievement.command.CheckCommand;
import com.hm.achievement.command.DeleteCommand;
import com.hm.achievement.command.GiveCommand;
import com.hm.achievement.command.HelpCommand;
import com.hm.achievement.command.InfoCommand;
import com.hm.achievement.command.ListCommand;
import com.hm.achievement.command.MonthCommand;
import com.hm.achievement.command.ReloadCommand;
import com.hm.achievement.command.StatsCommand;
import com.hm.achievement.command.TopCommand;
import com.hm.achievement.command.WeekCommand;
import com.hm.achievement.db.DatabasePoolsManager;
import com.hm.achievement.db.PooledRequestsSender;
import com.hm.achievement.db.SQLDatabaseManager;
import com.hm.achievement.listener.AchieveArrowListener;
import com.hm.achievement.listener.AchieveBedListener;
import com.hm.achievement.listener.AchieveBlockBreakListener;
import com.hm.achievement.listener.AchieveBlockPlaceListener;
import com.hm.achievement.listener.AchieveConnectionListener;
import com.hm.achievement.listener.AchieveConsumeListener;
import com.hm.achievement.listener.AchieveCraftListener;
import com.hm.achievement.listener.AchieveDeathListener;
import com.hm.achievement.listener.AchieveDropListener;
import com.hm.achievement.listener.AchieveEnchantListener;
import com.hm.achievement.listener.AchieveFishListener;
import com.hm.achievement.listener.AchieveHoeFertiliseFireworkMusicListener;
import com.hm.achievement.listener.AchieveItemBreakListener;
import com.hm.achievement.listener.AchieveKillListener;
import com.hm.achievement.listener.AchieveMilkLavaWaterListener;
import com.hm.achievement.listener.AchievePetMasterGiveReceiveListener;
import com.hm.achievement.listener.AchievePickupListener;
import com.hm.achievement.listener.AchievePlayerCommandListener;
import com.hm.achievement.listener.AchieveQuitListener;
import com.hm.achievement.listener.AchieveShearListener;
import com.hm.achievement.listener.AchieveSnowballEggListener;
import com.hm.achievement.listener.AchieveTameListener;
import com.hm.achievement.listener.AchieveTeleportRespawnListener;
import com.hm.achievement.listener.AchieveTradeAnvilBrewSmeltListener;
import com.hm.achievement.listener.AchieveXPListener;
import com.hm.achievement.listener.ListGUIListener;
import com.hm.achievement.runnable.AchieveDistanceRunnable;
import com.hm.achievement.runnable.AchievePlayTimeRunnable;
import com.hm.achievement.utils.AchievementCommentedYamlConfiguration;
import com.hm.achievement.utils.FileUpdater;
import com.hm.mcshared.file.FileManager;
import com.hm.mcshared.update.UpdateChecker;

import net.milkbowl.vault.economy.Economy;

/**
 * Advanced Achievements enables unique and challenging achievements on your server. Try to collect as many as you can,
 * earn rewards, climb the rankings and receive RP books!
 * 
 * AdvancedAchievements is under GNU General Public License version 3. Please visit the plugin's GitHub for more
 * information : https://github.com/PyvesB/AdvancedAchievements
 * 
 * Official plugin's server: hellominecraft.fr
 * 
 * Bukkit project page: dev.bukkit.org/bukkit-plugins/advanced-achievements
 * 
 * Spigot project page: spigotmc.org/resources/advanced-achievements.6239
 * 
 * @since April 2015
 * @version 4.1.3
 * @author Pyves
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
	private AchieveMilkLavaWaterListener milkLavaWaterListener;
	private AchieveTradeAnvilBrewSmeltListener inventoryClickListener;
	private AchieveEnchantListener enchantmentListener;
	private AchieveBedListener bedListener;
	private AchieveXPListener xpListener;
	private AchieveDropListener dropListener;
	private AchievePickupListener pickupListener;
	private AchieveHoeFertiliseFireworkMusicListener hoeFertiliseFireworkMusicListener;
	private AchieveTameListener tameListener;
	private AchieveBlockPlaceListener blockPlaceListener;
	private AchieveBlockBreakListener blockBreakListener;
	private AchieveKillListener killListener;
	private AchieveCraftListener craftListener;
	private AchievePlayerCommandListener playerCommandListener;
	private AchieveQuitListener quitListener;
	private AchieveTeleportRespawnListener teleportRespawnListener;
	private AchievePetMasterGiveReceiveListener petMasterGiveReceiveListener;

	private ListGUIListener listGUIListener;

	// Additional classes related to plugin modules and commands.
	private AchievementRewards reward;
	private AchievementDisplay achievementDisplay;
	private GiveCommand giveCommand;
	private BookCommand bookCommand;
	private TopCommand topCommand;
	private WeekCommand weekCommand;
	private MonthCommand monthCommand;
	private ListCommand listCommand;
	private StatsCommand statsCommand;
	private InfoCommand infoCommand;
	private HelpCommand helpCommand;
	private CheckCommand checkCommand;
	private DeleteCommand deleteCommand;
	private ReloadCommand reloadCommand;
	private UpdateChecker updateChecker;

	// Language and configuration related.
	private AchievementCommentedYamlConfiguration config;
	private AchievementCommentedYamlConfiguration lang;
	private final FileUpdater fileUpdater;

	// Database related.
	private final SQLDatabaseManager db;
	private final DatabasePoolsManager poolsManager;
	private PooledRequestsSender pooledRequestsSender;
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

	// Plugin runnable classes.
	private AchieveDistanceRunnable achieveDistanceRunnable;
	private AchievePlayTimeRunnable achievePlayTimeRunnable;

	// Bukkit scheduler tasks.
	private BukkitTask pooledRequestsSenderTask;
	private BukkitTask playedTimeTask;
	private BukkitTask distanceTask;

	public AdvancedAchievements() {

		successfulLoad = true;
		overrideDisable = false;
		db = new SQLDatabaseManager(this);
		poolsManager = new DatabasePoolsManager(this);
		fileUpdater = new FileUpdater(this);
	}

	/**
	 * Called when server is launched or reloaded.
	 */
	@Override
	public void onEnable() {

		// Start enabling plugin.
		long startTime = System.currentTimeMillis();

		AchievementCommentedYamlConfiguration configFile = loadAndBackupFile("config.yml");
		AchievementCommentedYamlConfiguration langFile = loadAndBackupFile(
				configFile.getString("LanguageFileName", "lang.yml"));

		// Error while loading .yml files; do not do any further work.
		if (overrideDisable) {
			overrideDisable = false;
			return;
		}

		// Update configurations from previous versions of the plugin; only if server reload or restart.
		fileUpdater.updateOldConfiguration(configFile);
		fileUpdater.updateOldLanguage(langFile);

		configurationLoad(configFile, langFile);

		// Load Metrics Lite.
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			this.getLogger().severe("Error while sending Metrics statistics.");
			successfulLoad = false;
		}

		if (databaseBackup && (!"mysql".equalsIgnoreCase(config.getString("DatabaseType", "sqlite"))
				|| !"postgresql".equalsIgnoreCase(config.getString("DatabaseType", "sqlite")))) {
			File backup = new File(this.getDataFolder(), "achievements.db.bak");
			// Only do a daily backup for the .db file.
			if (System.currentTimeMillis() - backup.lastModified() > 86400000 || backup.length() == 0) {
				this.getLogger().info("Backing up database file...");
				try {
					FileManager fileManager = new FileManager("achievements.db", this);
					fileManager.backupFile();
				} catch (IOException e) {
					this.getLogger().log(Level.SEVERE, "Error while backing up database file: ", e);
					successfulLoad = false;
				}
			}
		}

		PluginManager pm = getServer().getPluginManager();
		this.getLogger().info("Registering listeners...");

		// Check for available plugin update.
		if (config.getBoolean("CheckForUpdate", true)) {
			updateChecker = new UpdateChecker(this,
					"https://raw.githubusercontent.com/PyvesB/AdvancedAchievements/master/pom.xml",
					new String[] { "dev.bukkit.org/bukkit-plugins/advanced-achievements/files",
							"spigotmc.org/resources/advanced-achievements.6239" },
					"achievement.update", chatHeader);
			pm.registerEvents(updateChecker, this);
			updateChecker.launchUpdateCheckerTask();
		}

		// Register listeners so they can monitor server events; if there are no config related achievements, listeners
		// aren't registered.
		if (!disabledCategorySet.contains(MultipleAchievements.PLACES.toString())) {
			blockPlaceListener = new AchieveBlockPlaceListener(this);
			pm.registerEvents(blockPlaceListener, this);
		}

		if (!disabledCategorySet.contains(MultipleAchievements.BREAKS.toString())) {
			blockBreakListener = new AchieveBlockBreakListener(this);
			pm.registerEvents(blockBreakListener, this);
		}

		if (!disabledCategorySet.contains(MultipleAchievements.KILLS.toString())) {
			killListener = new AchieveKillListener(this);
			pm.registerEvents(killListener, this);
		}

		if (!disabledCategorySet.contains(MultipleAchievements.CRAFTS.toString())) {
			craftListener = new AchieveCraftListener(this);
			pm.registerEvents(craftListener, this);
		}

		if (!disabledCategorySet.contains(MultipleAchievements.PLAYERCOMMANDS.toString())) {
			playerCommandListener = new AchievePlayerCommandListener(this);
			pm.registerEvents(playerCommandListener, this);
		}

		if (!disabledCategorySet.contains(NormalAchievements.DEATHS.toString())) {
			deathListener = new AchieveDeathListener(this);
			pm.registerEvents(deathListener, this);
		}

		if (!disabledCategorySet.contains(NormalAchievements.ARROWS.toString())) {
			arrowListener = new AchieveArrowListener(this);
			pm.registerEvents(arrowListener, this);
		}

		if (!disabledCategorySet.contains(NormalAchievements.SNOWBALLS.toString())
				|| !disabledCategorySet.contains(NormalAchievements.EGGS.toString())) {
			snowballEggListener = new AchieveSnowballEggListener(this);
			pm.registerEvents(snowballEggListener, this);
		}

		if (!disabledCategorySet.contains(NormalAchievements.FISH.toString())
				|| !disabledCategorySet.contains(NormalAchievements.TREASURES.toString())) {
			fishListener = new AchieveFishListener(this);
			pm.registerEvents(fishListener, this);
		}

		if (!disabledCategorySet.contains(NormalAchievements.ITEMBREAKS.toString())) {
			itemBreakListener = new AchieveItemBreakListener(this);
			pm.registerEvents(itemBreakListener, this);
		}

		if (!disabledCategorySet.contains(NormalAchievements.CONSUMEDPOTIONS.toString())
				|| !disabledCategorySet.contains(NormalAchievements.EATENITEMS.toString())) {
			consumeListener = new AchieveConsumeListener(this);
			pm.registerEvents(consumeListener, this);
		}

		if (!disabledCategorySet.contains(NormalAchievements.SHEARS.toString())) {
			shearListener = new AchieveShearListener(this);
			pm.registerEvents(shearListener, this);
		}

		if (!disabledCategorySet.contains(NormalAchievements.MILKS.toString())
				|| !disabledCategorySet.contains(NormalAchievements.LAVABUCKETS.toString())
				|| !disabledCategorySet.contains(NormalAchievements.WATERBUCKETS.toString())) {
			milkLavaWaterListener = new AchieveMilkLavaWaterListener(this);
			pm.registerEvents(milkLavaWaterListener, this);
		}

		if (!disabledCategorySet.contains(NormalAchievements.CONNECTIONS.toString())) {
			connectionListener = new AchieveConnectionListener(this);
			pm.registerEvents(connectionListener, this);
		}

		if (!disabledCategorySet.contains(NormalAchievements.TRADES.toString())
				|| !disabledCategorySet.contains(NormalAchievements.ANVILS.toString())
				|| !disabledCategorySet.contains(NormalAchievements.BREWING.toString())
				|| !disabledCategorySet.contains(NormalAchievements.SMELTING.toString())) {
			inventoryClickListener = new AchieveTradeAnvilBrewSmeltListener(this);
			pm.registerEvents(inventoryClickListener, this);
		}

		if (!disabledCategorySet.contains(NormalAchievements.ENCHANTMENTS.toString())) {
			enchantmentListener = new AchieveEnchantListener(this);
			pm.registerEvents(enchantmentListener, this);
		}

		if (!disabledCategorySet.contains(NormalAchievements.LEVELS.toString())) {
			xpListener = new AchieveXPListener(this);
			pm.registerEvents(xpListener, this);
		}

		if (!disabledCategorySet.contains(NormalAchievements.BEDS.toString())) {
			bedListener = new AchieveBedListener(this);
			pm.registerEvents(bedListener, this);
		}

		if (!disabledCategorySet.contains(NormalAchievements.DROPS.toString())) {
			dropListener = new AchieveDropListener(this);
			pm.registerEvents(dropListener, this);
		}

		if (!disabledCategorySet.contains(NormalAchievements.PICKUPS.toString())) {
			pickupListener = new AchievePickupListener(this);
			pm.registerEvents(pickupListener, this);
		}

		if (!disabledCategorySet.contains(NormalAchievements.TAMES.toString())) {
			tameListener = new AchieveTameListener(this);
			pm.registerEvents(tameListener, this);
		}

		if (!disabledCategorySet.contains(NormalAchievements.HOEPLOWING.toString())
				|| !disabledCategorySet.contains(NormalAchievements.FERTILISING.toString())
				|| !disabledCategorySet.contains(NormalAchievements.FIREWORKS.toString())
				|| !disabledCategorySet.contains(NormalAchievements.MUSICDISCS.toString())) {
			hoeFertiliseFireworkMusicListener = new AchieveHoeFertiliseFireworkMusicListener(this);
			pm.registerEvents(hoeFertiliseFireworkMusicListener, this);
		}

		if (!disabledCategorySet.contains(NormalAchievements.DISTANCEFOOT.toString())
				|| !disabledCategorySet.contains(NormalAchievements.DISTANCEPIG.toString())
				|| !disabledCategorySet.contains(NormalAchievements.DISTANCEHORSE.toString())
				|| !disabledCategorySet.contains(NormalAchievements.DISTANCEMINECART.toString())
				|| !disabledCategorySet.contains(NormalAchievements.DISTANCEBOAT.toString())
				|| !disabledCategorySet.contains(NormalAchievements.DISTANCEGLIDING.toString())
				|| !disabledCategorySet.contains(NormalAchievements.DISTANCELLAMA.toString())) {
			if (!disabledCategorySet.contains(NormalAchievements.LEVELS.toString())
					|| !disabledCategorySet.contains(NormalAchievements.PLAYEDTIME.toString())) {
				quitListener = new AchieveQuitListener(this);
				pm.registerEvents(quitListener, this);
			}
			if (!disabledCategorySet.contains(NormalAchievements.ENDERPEARLS.toString())) {
				teleportRespawnListener = new AchieveTeleportRespawnListener(this);
				pm.registerEvents(teleportRespawnListener, this);
			}
		}

		if (!disabledCategorySet.contains(NormalAchievements.PETMASTERGIVE.toString())
				|| !disabledCategorySet.contains(NormalAchievements.PETMASTERRECEIVE.toString())) {
			// Need PetMaster with a minimum version of 1.4.
			if (Bukkit.getPluginManager().isPluginEnabled("PetMaster") && Integer.parseInt(Character.toString(
					Bukkit.getPluginManager().getPlugin("PetMaster").getDescription().getVersion().charAt(2))) >= 4) {
				petMasterGiveReceiveListener = new AchievePetMasterGiveReceiveListener(this);
				pm.registerEvents(petMasterGiveReceiveListener, this);
			} else {
				this.getLogger().warning(
						"Failed to pair with Pet Master plugin; disabling PetMasterGive and PetMasterReceive categories.");
				this.getLogger().warning(
						"Ensure you have placed Pet Master with a minimum version of 1.4 in your plugins folder.");
				this.getLogger().warning(
						"If you do not wish to use these categories, you must add PetMasterGive and PetMasterReceive to the DisabledCategories list in your config.");
			}
		}

		listGUIListener = new ListGUIListener(this);
		pm.registerEvents(listGUIListener, this);

		this.getLogger().info("Initialising database and launching scheduled tasks...");

		// Initialise the SQLite/MySQL/PostgreSQL database.
		db.initialise();

		// Error while loading database do not do any further work.
		if (overrideDisable) {
			overrideDisable = false;
			return;
		}

		pooledRequestsSender = new PooledRequestsSender(this);
		// Schedule a repeating task to group database queries for some frequent
		// events.
		pooledRequestsSenderTask = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(
				Bukkit.getPluginManager().getPlugin(this.getDescription().getName()), pooledRequestsSender,
				pooledRequestsTaskInterval * 40L, pooledRequestsTaskInterval * 20L);

		// Schedule a repeating task to monitor played time for each player (not directly related to an event).
		if (!disabledCategorySet.contains(NormalAchievements.PLAYEDTIME.toString())) {
			achievePlayTimeRunnable = new AchievePlayTimeRunnable(this);
			playedTimeTask = Bukkit.getServer().getScheduler().runTaskTimer(
					Bukkit.getPluginManager().getPlugin(this.getDescription().getName()), achievePlayTimeRunnable,
					playtimeTaskInterval * 10L, playtimeTaskInterval * 20L);
		}

		// Schedule a repeating task to monitor distances travelled by each player (not directly related to an event).
		if (!disabledCategorySet.contains(NormalAchievements.DISTANCEFOOT.toString())
				|| !disabledCategorySet.contains(NormalAchievements.DISTANCEPIG.toString())
				|| !disabledCategorySet.contains(NormalAchievements.DISTANCEHORSE.toString())
				|| !disabledCategorySet.contains(NormalAchievements.DISTANCEMINECART.toString())
				|| !disabledCategorySet.contains(NormalAchievements.DISTANCEBOAT.toString())
				|| !disabledCategorySet.contains(NormalAchievements.DISTANCEGLIDING.toString())
				|| !disabledCategorySet.contains(NormalAchievements.DISTANCELLAMA.toString())) {
			achieveDistanceRunnable = new AchieveDistanceRunnable(this);
			distanceTask = Bukkit.getServer().getScheduler().runTaskTimer(
					Bukkit.getPluginManager().getPlugin(this.getDescription().getName()), achieveDistanceRunnable,
					distanceTaskInterval * 40L, distanceTaskInterval * 20L);
		}

		if (successfulLoad) {
			this.getLogger().info("Plugin successfully enabled and ready to run! Took "
					+ (System.currentTimeMillis() - startTime) + "ms.");
		} else {
			this.getLogger().severe("Error(s) while loading plugin. Please view previous logs for more information.");
		}
	}

	/**
	 * Loads the plugin configuration and sets values to different parameters; loads the language file and backs
	 * configuration files up. Register permissions. Initialises command modules.
	 * 
	 * @param config
	 * @param lang
	 */
	public void configurationLoad(AchievementCommentedYamlConfiguration config,
			AchievementCommentedYamlConfiguration lang) {

		this.config = config;
		this.lang = lang;

		this.getLogger().info("Loading parameters, registering permissions and initialising command modules...");

		extractParameters();

		registerPermissions();

		initialiseCommands();

		// Reload achievements in distance, max level and play time runnables on plugin reload (when objects are null).
		if (achieveDistanceRunnable != null) {
			achieveDistanceRunnable.extractAchievementsFromConfig();
		}

		// Unregister events if user changed the option and did an /aach reload. We do not recheck for update on /aach
		// reload.
		if (!config.getBoolean("CheckForUpdate", true)) {
			PlayerJoinEvent.getHandlerList().unregister(updateChecker);
		}

		logAchievementStats();
	}

	/**
	 * Loads and backs file with name fileName up.
	 * 
	 * @param fileName
	 * @return the loaded file YamlManager
	 */
	public AchievementCommentedYamlConfiguration loadAndBackupFile(String fileName) {

		AchievementCommentedYamlConfiguration configFile = null;
		this.getLogger().info("Loading and backing up " + fileName + " file...");
		try {
			configFile = new AchievementCommentedYamlConfiguration(fileName, this);
		} catch (IOException | InvalidConfigurationException e) {
			this.getLogger().severe("Error while loading " + fileName + " file, disabling plugin.");
			this.getLogger().log(Level.SEVERE,
					"Verify your syntax by visiting yaml-online-parser.appspot.com and using the following logs: ", e);
			successfulLoad = false;
			overrideDisable = true;
			Bukkit.getServer().getPluginManager().disablePlugin(this);
			return null;
		}

		try {
			configFile.backupConfiguration();
		} catch (IOException e) {
			this.getLogger().log(Level.SEVERE, "Error while backing up " + fileName + " file: ", e);
			successfulLoad = false;
		}
		return configFile;
	}

	/**
	 * Extracts plugin parameters from the configuration file.
	 */
	private void extractParameters() {

		icon = StringEscapeUtils.unescapeJava(config.getString("Icon", "\u2618"));
		color = ChatColor.getByChar(config.getString("Color", "5").charAt(0));
		chatHeader = ChatColor.GRAY + "[" + color + icon + ChatColor.GRAY + "] ";
		restrictCreative = config.getBoolean("RestrictCreative", false);
		databaseBackup = config.getBoolean("DatabaseBackup", true);
		excludedWorldSet = new HashSet<>(config.getList("ExcludedWorlds"));
		disabledCategorySet = new HashSet<>(config.getList("DisabledCategories"));
		playtimeTaskInterval = config.getInt("PlaytimeTaskInterval", 60);
		distanceTaskInterval = config.getInt("DistanceTaskInterval", 5);
		pooledRequestsTaskInterval = config.getInt("PooledRequestsTaskInterval", 60);
		asyncPooledRequestsSender = config.getBoolean("AsyncPooledRequestsSender", true);
	}

	/**
	 * Initialises the command modules.
	 */
	private void initialiseCommands() {

		reward = new AchievementRewards(this);
		achievementDisplay = new AchievementDisplay(this);
		giveCommand = new GiveCommand(this);
		bookCommand = new BookCommand(this);
		topCommand = new TopCommand(this);
		weekCommand = new WeekCommand(this);
		monthCommand = new MonthCommand(this);
		statsCommand = new StatsCommand(this);
		infoCommand = new InfoCommand(this);
		listCommand = new ListCommand(this);
		helpCommand = new HelpCommand(this);
		checkCommand = new CheckCommand(this);
		deleteCommand = new DeleteCommand(this);
		reloadCommand = new ReloadCommand(this);
	}

	/**
	 * Logs number of achievements and disabled categories.
	 */
	private void logAchievementStats() {

		int totalAchievements = 0;
		int categoriesInUse = 0;

		// Enumerate Commands achievements.
		if (!disabledCategorySet.contains("Commands")) {
			ConfigurationSection categoryConfig = config.getConfigurationSection("Commands");
			int keyCount = categoryConfig.getKeys(false).size();
			if (keyCount > 0) {
				categoriesInUse += 1;
				totalAchievements += keyCount;
			}
		}

		// Enumerate the normal achievements.
		for (NormalAchievements category : NormalAchievements.values()) {
			if (disabledCategorySet.contains(category.toString())) {
				continue;
			}

			ConfigurationSection categoryConfig = config.getConfigurationSection(category.toString());
			int keyCount = categoryConfig.getKeys(false).size();
			if (keyCount > 0) {
				categoriesInUse += 1;
				totalAchievements += keyCount;
			}
		}

		// Enumerate the achievements with multiple categories.
		for (MultipleAchievements category : MultipleAchievements.values()) {
			if (disabledCategorySet.contains(category.toString())) {
				continue;
			}

			ConfigurationSection categoryConfig = config.getConfigurationSection(category.toString());
			Set<String> categorySections = categoryConfig.getKeys(false);

			if (categorySections.isEmpty()) {
				continue;
			}

			categoriesInUse += 1;

			// Enumerate the sub-categories
			for (String section : categorySections) {
				ConfigurationSection subcategoryConfig = config.getConfigurationSection(category + "." + section);
				int achievementCount = subcategoryConfig.getKeys(false).size();
				if (achievementCount > 0) {
					totalAchievements += achievementCount;
				}
			}
		}
		this.getLogger().info("Loaded " + totalAchievements + " achievements in " + categoriesInUse + " categories.");

		if (!disabledCategorySet.isEmpty()) {
			String disabledCategories;

			if (disabledCategorySet.size() == 1) {
				disabledCategories = disabledCategorySet.size() + " disabled category: "
						+ disabledCategorySet.toString();
			} else {
				disabledCategories = disabledCategorySet.size() + " disabled categories: "
						+ disabledCategorySet.toString();
			}

			this.getLogger().info(disabledCategories);
		}
	}

	/**
	 * Registers permissions that depend on the user's configuration file (for MultipleAchievements; for instance for
	 * stone breaks, achievement.count.breaks.stone will be registered).
	 */
	private void registerPermissions() {

		PluginManager pluginManager = this.getServer().getPluginManager();
		for (MultipleAchievements category : MultipleAchievements.values()) {
			for (String section : config.getConfigurationSection(category.toString()).getKeys(false)) {
				// Bukkit only allows permissions to be set once, so must do additional check to make sure they are not
				// being set again during an /aach reload.
				if (pluginManager.getPermission(category.toPermName() + "." + section) == null) {
					pluginManager.addPermission(
							new Permission(category.toPermName() + "." + section, PermissionDefault.TRUE));
				}
			}
		}
	}

	/**
	 * Called when server is stopped or reloaded.
	 */
	@Override
	public void onDisable() {

		// Error while loading .yml files or database; do not do any further work.
		if (overrideDisable) {
			return;
		}

		// Cancel scheduled tasks.
		if (pooledRequestsSenderTask != null) {
			pooledRequestsSenderTask.cancel();
		}
		if (playedTimeTask != null) {
			playedTimeTask.cancel();
		}
		if (distanceTask != null) {
			distanceTask.cancel();
		}

		// Send played time stats to the database, forcing synchronous writes.
		if (achievePlayTimeRunnable != null) {
			for (Entry<String, Long> entry : poolsManager.getPlayedTimeHashMap().entrySet()) {
				db.updatePlaytime(entry.getKey(), entry.getValue());
			}
		}

		// Send traveled distance stats to the database, synchronous writes.
		if (achieveDistanceRunnable != null) {
			for (Entry<String, Integer> entry : poolsManager.getHashMap(NormalAchievements.DISTANCEFOOT).entrySet()) {
				db.updateDistance(entry.getKey(), entry.getValue(), NormalAchievements.DISTANCEFOOT.toDBName());
			}
			for (Entry<String, Integer> entry : poolsManager.getHashMap(NormalAchievements.DISTANCEPIG).entrySet()) {
				db.updateDistance(entry.getKey(), entry.getValue(), NormalAchievements.DISTANCEPIG.toDBName());
			}
			for (Entry<String, Integer> entry : poolsManager.getHashMap(NormalAchievements.DISTANCEHORSE).entrySet()) {
				db.updateDistance(entry.getKey(), entry.getValue(), NormalAchievements.DISTANCEHORSE.toDBName());
			}
			for (Entry<String, Integer> entry : poolsManager.getHashMap(NormalAchievements.DISTANCEBOAT).entrySet()) {
				db.updateDistance(entry.getKey(), entry.getValue(), NormalAchievements.DISTANCEBOAT.toDBName());
			}
			for (Entry<String, Integer> entry : poolsManager.getHashMap(NormalAchievements.DISTANCEMINECART)
					.entrySet()) {
				db.updateDistance(entry.getKey(), entry.getValue(), NormalAchievements.DISTANCEMINECART.toDBName());
			}
			for (Entry<String, Integer> entry : poolsManager.getHashMap(NormalAchievements.DISTANCEGLIDING)
					.entrySet()) {
				db.updateDistance(entry.getKey(), entry.getValue(), NormalAchievements.DISTANCEGLIDING.toDBName());
			}
			for (Entry<String, Integer> entry : poolsManager.getHashMap(NormalAchievements.DISTANCELLAMA).entrySet()) {
				db.updateDistance(entry.getKey(), entry.getValue(), NormalAchievements.DISTANCELLAMA.toDBName());
			}
		}

		// Send remaining stats for pooled events to the database; send via main thread with synchronous mode.
		asyncPooledRequestsSender = false;
		pooledRequestsSender.sendRequests();

		try {
			if (db.getSQLConnection() != null) {
				db.getSQLConnection().close();
			}
		} catch (SQLException e) {
			this.getLogger().log(Level.SEVERE, "Error while closing connection to database: ", e);
		}

		this.getLogger().info("Remaining requests sent to database, plugin disabled.");
	}

	/**
	 * Called when a player or the console enters a command. Handles command directly or dispatches to one of the
	 * command modules.
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		if (!"aach".equalsIgnoreCase(cmd.getName())) {
			return false;
		}

		if ((args.length == 1) && !"help".equalsIgnoreCase(args[0])) {
			if ("book".equalsIgnoreCase(args[0])) {
				bookCommand.executeCommand(sender, null, "book");
			} else if ("hcaa".equalsIgnoreCase(args[0]) && sender.hasPermission("achievement.easteregg")) {
				displayEasterEgg(sender);
			} else if ("reload".equalsIgnoreCase(args[0])) {
				reloadCommand.executeCommand(sender, null, "reload");
			} else if ("stats".equalsIgnoreCase(args[0])) {
				statsCommand.executeCommand(sender, null, "stats");
			} else if ("list".equalsIgnoreCase(args[0])) {
				listCommand.executeCommand(sender, null, "list");
			} else if ("top".equalsIgnoreCase(args[0])) {
				topCommand.executeCommand(sender, null, "top");
			} else if ("week".equalsIgnoreCase(args[0])) {
				weekCommand.executeCommand(sender, null, "week");
			} else if ("month".equalsIgnoreCase(args[0])) {
				monthCommand.executeCommand(sender, null, "month");
			} else if ("info".equalsIgnoreCase(args[0])) {
				infoCommand.executeCommand(sender, null, null);
			} else {
				helpCommand.executeCommand(sender, args, null);
			}
		} else if ((args.length == 3) && "give".equalsIgnoreCase(args[0])) {
			giveCommand.executeCommand(sender, args, "give");
		} else if ((args.length >= 3) && "check".equalsIgnoreCase(args[0])) {
			checkCommand.executeCommand(sender, args, "check");
		} else if ((args.length >= 3) && "delete".equalsIgnoreCase(args[0])) {
			deleteCommand.executeCommand(sender, args, "delete");
		} else {
			helpCommand.executeCommand(sender, args, null);
		}
		return true;
	}

	/**
	 * Easter egg; run it and you'll see what all this mess is about!
	 * 
	 * @param sender
	 */
	private void displayEasterEgg(CommandSender sender) {

		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§0\u2592§0\u2592§0\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§0\u2592§0\u2592§8\u2592§8\u2592§8\u2592§0\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§0\u2592§0\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§0\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§0\u2592§0\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§0\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§0\u2592§0\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§0\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§4\u2592§4\u2592§c\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§0\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§0\u2592§4\u2592§4\u2592§4\u2592§c\u2592§c\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§0\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§7\u2592§7\u2592§7\u2592§0\u2592§0\u2592§8\u2592§8\u2592§4\u2592§4\u2592§4\u2592§c\u2592§4\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§0\u2592§7\u2592§7\u2592§7\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§7\u2592§0\u2592§0\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§4\u2592§4\u2592§4\u2592§4\u2592§4\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§0\u2592§7\u2592§7\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§0\u2592§0\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§4\u2592§4\u2592§4\u2592§4\u2592§4\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§0\u2592§7\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§0\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§4\u2592§4\u2592§4\u2592§4\u2592§4\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§0\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§0\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§4\u2592§4\u2592§4\u2592§4\u2592§4\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§0\u2592§0\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§0\u2592§8\u2592§f\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§4\u2592§4\u2592§4\u2592§4\u2592§4\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§0\u2592§0\u2592§7\u2592§0\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§0\u2592§8\u2592§7\u2592§f\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§4\u2592§4\u2592§4\u2592§4\u2592§4\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§0\u2592§0\u2592§7\u2592§7\u2592§7\u2592§8\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§0\u2592§8\u2592§f\u2592§7\u2592§f\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§4\u2592§4\u2592§4\u2592§4\u2592§4\u2592§8\u2592§8\u2592§8\u2592§8\u2592§0\u2592§0\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§8\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§0\u2592§8\u2592§7\u2592§f\u2592§7\u2592§f\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§4\u2592§4\u2592§4\u2592§6\u2592§6\u2592§8\u2592§0\u2592§0\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§8\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§0\u2592§8\u2592§f\u2592§7\u2592§f\u2592§7\u2592§f\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§4\u2592§6\u2592§6\u2592§6\u2592§6\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§8\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§8\u2592§8\u2592§8\u2592§f\u2592§7\u2592§f\u2592§7\u2592§f\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§6\u2592§6\u2592§6\u2592§6\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§0\u2592§0\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§0\u2592§8\u2592§8\u2592§f\u2592§7\u2592§f\u2592§7\u2592§f\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§8\u2592§0\u2592§0\u2592§7\u2592§6\u2592§6\u2592§4\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§0\u2592§0\u2592§0\u2592§0\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§7\u2592§0\u2592§8\u2592§8\u2592§f\u2592§7\u2592§f\u2592§7\u2592§f\u2592§8\u2592§8\u2592§8\u2592§8\u2592§0\u2592§0\u2592§7\u2592§7\u2592§7\u2592§4\u2592§4\u2592§4\u2592§7\u2592§7\u2592§7\u2592§7\u2592§0\u2592§0\u2592§0\u2592§0\u2592§0\u2592§0\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§7\u2592§7\u2592§0\u2592§8\u2592§8\u2592§f\u2592§7\u2592§f\u2592§7\u2592§f\u2592§8\u2592§8\u2592§0\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§4\u2592§4\u2592§4\u2592§7\u2592§7\u2592§0\u2592§0\u2592§0\u2592§0\u2592§0\u2592§0\u2592§7\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§7\u2592§7\u2592§7\u2592§0\u2592§8\u2592§8\u2592§f\u2592§7\u2592§f\u2592§7\u2592§f\u2592§f\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§4\u2592§4\u2592§4\u2592§0\u2592§0\u2592§0\u2592§0\u2592§0\u2592§0\u2592§7\u2592§7\u2592§7\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§4\u2592§4\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§f\u2592§f\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§0\u2592§4\u2592§4\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§0\u2592§0\u2592§0\u2592§4\u2592§4\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§f\u2592§f\u2592§7\u2592§0\u2592§0\u2592§0\u2592§0\u2592§0\u2592§4\u2592§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§8\u2592§8\u2592§0\u2592§0\u2592§0\u2592§0\u2592§0\u2592§7\u2592§7\u2592§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§0\u2592§0\u2592§0\u2592§0\u2592§0\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§0\u2592§0\u2592§0\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§r"));
		sender.sendMessage(StringEscapeUtils.unescapeJava(
				"§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§7\u2592§5\u2592§5\u2592§5\u2592§5\u2592§7\u2592§r"));
	}

	/**
	 * Checks if a player is in a world in which achievements must not be received.
	 * 
	 * @param player
	 * @return true if player is in excluded world, false otherwise
	 */
	public boolean isInExludedWorld(Player player) {

		if (excludedWorldSet.isEmpty()) {
			return false;
		}

		return excludedWorldSet.contains(player.getWorld().getName());
	}

	/**
	 * Tries to hook up with Vault, and log if this is called on plugin initialisation.
	 * 
	 * @param log
	 * @return true if Vault available, false otherwise
	 */
	public boolean setUpEconomy(boolean log) {

		if (economy != null) {
			return true;
		}

		try {
			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
					.getRegistration(net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
			}
			return economy != null;
		} catch (NoClassDefFoundError e) {
			if (log) {
				this.getLogger().warning("Attempt to hook up with Vault failed. Money reward ignored.");
			}
			return false;
		}
	}

	/**
	 * Returns a map from achievement name (as stored in the database) to DisplayName. If multiple achievements have the
	 * same achievement name, only the first DisplayName will be tracked. If DisplayName for an achievement is empty or
	 * undefined, the value in the returned map will be an empty string.
	 * 
	 * @return Map from achievement name to user-friendly display name
	 */
	public Map<String, String> getAchievementsAndDisplayNames() {

		Map<String, String> achievementsAndDisplayNames = new HashMap<>();

		// Enumerate Commands achievements
		for (String ach : config.getConfigurationSection("Commands").getKeys(false)) {

			String achName = config.getString("Commands." + ach + ".Name", "");
			String displayName = config.getString("Commands." + ach + ".DisplayName", "");

			if (!achievementsAndDisplayNames.containsKey(achName)) {
				achievementsAndDisplayNames.put(achName, displayName);
			}
		}

		// Enumerate the normal achievements
		for (NormalAchievements category : NormalAchievements.values()) {
			ConfigurationSection categoryConfig = config.getConfigurationSection(category.toString());
			for (String ach : categoryConfig.getKeys(false)) {

				String achName = config.getString(category + "." + ach + ".Name", "");
				String displayName = config.getString(category + "." + ach + ".DisplayName", "");

				if (!achievementsAndDisplayNames.containsKey(achName)) {
					achievementsAndDisplayNames.put(achName, displayName);
				}
			}
		}

		// Enumerate the achievements with multiple categories
		for (MultipleAchievements category : MultipleAchievements.values()) {
			ConfigurationSection categoryConfig = config.getConfigurationSection(category.toString());
			for (String section : categoryConfig.getKeys(false)) {
				ConfigurationSection subcategoryConfig = config.getConfigurationSection(category + "." + section);
				for (String level : subcategoryConfig.getKeys(false)) {

					String achName = config.getString(category + "." + section + '.' + level + ".Name", "");
					String displayName = config.getString(category + "." + section + '.' + level + ".DisplayName", "");

					if (!achievementsAndDisplayNames.containsKey(achName)) {
						achievementsAndDisplayNames.put(achName, displayName);
					}
				}
			}
		}

		return achievementsAndDisplayNames;
	}

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

	public AchieveMilkLavaWaterListener getMilkLavaWaterListener() {

		return milkLavaWaterListener;
	}

	public AchieveTradeAnvilBrewSmeltListener getInventoryClickListener() {

		return inventoryClickListener;
	}

	public AchieveBedListener getBedListener() {

		return bedListener;
	}

	public AchieveHoeFertiliseFireworkMusicListener getHoeFertiliseFireworkMusicListener() {

		return hoeFertiliseFireworkMusicListener;
	}

	public AchievePetMasterGiveReceiveListener getPetMasterGiveReceiveListener() {

		return petMasterGiveReceiveListener;
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

	public AchievementCommentedYamlConfiguration getPluginConfig() {

		return config;
	}

	@Override
	@Deprecated
	public FileConfiguration getConfig() {

		return null;
	}

	public AchievementCommentedYamlConfiguration getPluginLang() {

		return lang;
	}
}
