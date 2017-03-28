package com.hm.achievement;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.base.Strings;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.command.BookCommand;
import com.hm.achievement.command.CheckCommand;
import com.hm.achievement.command.DeleteCommand;
import com.hm.achievement.command.EasterEggCommand;
import com.hm.achievement.command.GiveCommand;
import com.hm.achievement.command.HelpCommand;
import com.hm.achievement.command.InfoCommand;
import com.hm.achievement.command.ListCommand;
import com.hm.achievement.command.MonthCommand;
import com.hm.achievement.command.ReloadCommand;
import com.hm.achievement.command.ResetCommand;
import com.hm.achievement.command.StatsCommand;
import com.hm.achievement.command.ToggleCommand;
import com.hm.achievement.command.TopCommand;
import com.hm.achievement.command.WeekCommand;
import com.hm.achievement.db.DatabasePoolsManager;
import com.hm.achievement.db.DatabaseType;
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
import com.hm.achievement.listener.AchieveShearListener;
import com.hm.achievement.listener.AchieveSnowballEggListener;
import com.hm.achievement.listener.AchieveTameListener;
import com.hm.achievement.listener.AchieveTeleportRespawnListener;
import com.hm.achievement.listener.AchieveTradeAnvilBrewSmeltListener;
import com.hm.achievement.listener.AchieveXPListener;
import com.hm.achievement.listener.FireworkListener;
import com.hm.achievement.listener.ListGUIListener;
import com.hm.achievement.listener.PlayerAdvancedAchievementListener;
import com.hm.achievement.listener.QuitListener;
import com.hm.achievement.runnable.AchieveDistanceRunnable;
import com.hm.achievement.runnable.AchievePlayTimeRunnable;
import com.hm.achievement.utils.AchievementCommentedYamlConfiguration;
import com.hm.achievement.utils.AchievementCountBungeeTabListPlusVariable;
import com.hm.achievement.utils.FileUpdater;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.FileManager;
import com.hm.mcshared.particle.ReflectionUtils.PackageType;
import com.hm.mcshared.update.UpdateChecker;

import codecrafter47.bungeetablistplus.api.bukkit.BungeeTabListPlusBukkitAPI;

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
 * @version 4.2.1
 * @author Pyves
 */
public class AdvancedAchievements extends JavaPlugin {

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
	private AchieveTradeAnvilBrewSmeltListener tradeAnvilBrewSmeltListener;
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
	private AchieveTeleportRespawnListener teleportRespawnListener;
	private AchievePetMasterGiveReceiveListener petMasterGiveReceiveListener;

	private QuitListener quitListener;
	private ListGUIListener listGUIListener;
	private FireworkListener fireworkListener;
	private PlayerAdvancedAchievementListener playerAdvancedAchievementListener;

	// Additional classes related to plugin modules and commands.
	private RewardParser rewardParser;
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
	private ToggleCommand toggleCommand;
	private ResetCommand resetCommand;
	private EasterEggCommand easterEggCommand;
	private UpdateChecker updateChecker;

	// Language, configuration and GUI related.
	private AchievementCommentedYamlConfiguration config;
	private AchievementCommentedYamlConfiguration lang;
	private AchievementCommentedYamlConfiguration gui;

	// Database related.
	private final SQLDatabaseManager databaseManager;
	private final DatabasePoolsManager poolsManager;
	private PooledRequestsSender pooledRequestsSender;
	private int pooledRequestsTaskInterval;
	private boolean databaseBackup;

	// Plugin options and various parameters.
	private String icon;
	private ChatColor color;
	private String chatHeader;
	private boolean restrictCreative;
	private boolean restrictSpectator;
	private boolean chatNotify;
	private Set<String> excludedWorldSet;
	private Set<String> disabledCategorySet;
	private boolean successfulLoad;
	private boolean overrideDisable;
	private int playtimeTaskInterval;
	private int distanceTaskInterval;

	// Plugin runnable classes.
	private AchieveDistanceRunnable distanceRunnable;
	private AchievePlayTimeRunnable playTimeRunnable;

	// Bukkit scheduler tasks.
	private BukkitTask pooledRequestsSenderTask;
	private BukkitTask playedTimeTask;
	private BukkitTask distanceTask;

	public AdvancedAchievements() {
		successfulLoad = true;
		overrideDisable = false;
		databaseManager = new SQLDatabaseManager(this);
		poolsManager = new DatabasePoolsManager(this);
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
		AchievementCommentedYamlConfiguration guiFile = loadAndBackupFile("gui.yml");

		// Error while loading .yml files; do not do any further work.
		if (overrideDisable) {
			overrideDisable = false;
			return;
		}

		// Update configurations from previous versions of the plugin; only if server reload or restart.
		FileUpdater fileUpdater = new FileUpdater(this);
		fileUpdater.updateOldConfiguration(configFile);
		fileUpdater.updateOldLanguage(langFile);

		configurationLoad(configFile, langFile, guiFile);

		this.getLogger().info("Registering listeners...");
		PluginManager pm = getServer().getPluginManager();

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
			tradeAnvilBrewSmeltListener = new AchieveTradeAnvilBrewSmeltListener(this);
			pm.registerEvents(tradeAnvilBrewSmeltListener, this);
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
				quitListener = new QuitListener(this);
				pm.registerEvents(quitListener, this);
			}
			if (!disabledCategorySet.contains(NormalAchievements.ENDERPEARLS.toString())) {
				teleportRespawnListener = new AchieveTeleportRespawnListener(this);
				pm.registerEvents(teleportRespawnListener, this);
			}
		}

		if (!disabledCategorySet.contains(NormalAchievements.PETMASTERGIVE.toString())
				|| !disabledCategorySet.contains(NormalAchievements.PETMASTERRECEIVE.toString())) {
			petMasterGiveReceiveListener = new AchievePetMasterGiveReceiveListener(this);
			pm.registerEvents(petMasterGiveReceiveListener, this);
		}

		listGUIListener = new ListGUIListener(this);
		pm.registerEvents(listGUIListener, this);

		fireworkListener = new FireworkListener(this);
		pm.registerEvents(fireworkListener, this);

		playerAdvancedAchievementListener = new PlayerAdvancedAchievementListener(this);
		pm.registerEvents(playerAdvancedAchievementListener, this);

		this.getLogger().info("Initialising database and launching scheduled tasks...");

		// Initialise the SQLite/MySQL/PostgreSQL database.
		databaseManager.initialise();

		if (databaseBackup && databaseManager.getDatabaseType() == DatabaseType.SQLITE) {
			File backup = new File(this.getDataFolder(), "achievements.db.bak");
			// Only do a daily backup for the .db file.
			if (System.currentTimeMillis() - backup.lastModified() > 86400000L || backup.length() == 0L) {
				this.getLogger().info("Backing up database file...");
				try {
					FileManager fileManager = new FileManager("achievements.db", this);
					fileManager.backupFile();
				} catch (IOException e) {
					this.getLogger().log(Level.SEVERE, "Error while backing up database file:", e);
					successfulLoad = false;
				}
			}
		}

		// Error while loading database do not do any further work.
		if (overrideDisable) {
			overrideDisable = false;
			return;
		}

		pooledRequestsSender = new PooledRequestsSender(this);
		// Schedule a repeating task to group database queries for some frequent events.
		pooledRequestsSenderTask = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this,
				pooledRequestsSender, pooledRequestsTaskInterval * 40L, pooledRequestsTaskInterval * 20L);

		// Schedule a repeating task to monitor played time for each player (not directly related to an event).
		if (!disabledCategorySet.contains(NormalAchievements.PLAYEDTIME.toString())) {
			playTimeRunnable = new AchievePlayTimeRunnable(this);
			playedTimeTask = Bukkit.getServer().getScheduler().runTaskTimer(this, playTimeRunnable,
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
			distanceRunnable = new AchieveDistanceRunnable(this);
			distanceTask = Bukkit.getServer().getScheduler().runTaskTimer(this, distanceRunnable,
					distanceTaskInterval * 40L, distanceTaskInterval * 20L);
		}

		if (Bukkit.getPluginManager().isPluginEnabled("BungeeTabListPlus")) {
			BungeeTabListPlusBukkitAPI.registerVariable(this, new AchievementCountBungeeTabListPlusVariable(this));
		}

		if (successfulLoad) {
			this.getLogger().info("Plugin successfully enabled and ready to run! Took "
					+ (System.currentTimeMillis() - startTime) + "ms.");
		} else {
			this.getLogger().severe("Error(s) while loading plugin. Please view previous logs for more information.");
		}
	}

	/**
	 * Loads the plugin configuration and sets values to different parameters; loads the language and GUI files and
	 * backs configuration files up. Registers permissions. Initialises command modules.
	 * 
	 * @param config
	 * @param lang
	 */
	public void configurationLoad(AchievementCommentedYamlConfiguration config,
			AchievementCommentedYamlConfiguration lang, AchievementCommentedYamlConfiguration gui) {
		this.config = config;
		this.lang = lang;
		this.gui = gui;

		this.getLogger().info("Loading parameters, registering permissions and initialising command modules...");
		extractParameters();
		registerPermissions();
		initialiseCommands();

		// Reload some parameters.
		if (playerAdvancedAchievementListener != null) {
			playerAdvancedAchievementListener.extractParameters();
		}
		if (distanceRunnable != null) {
			distanceRunnable.extractParameter();
		}

		// Unregister events if user changed the option and did an /aach reload. We do not recheck for update on /aach
		// reload.
		if (!config.getBoolean("CheckForUpdate", true)) {
			PlayerJoinEvent.getHandlerList().unregister(updateChecker);
		}

		logAchievementStats();
	}

	/**
	 * Loads and backs up file fileName.
	 * 
	 * @param fileName
	 * @return the loaded file AchievementCommentedYamlConfiguration
	 */
	public AchievementCommentedYamlConfiguration loadAndBackupFile(String fileName) {
		AchievementCommentedYamlConfiguration configFile = null;
		this.getLogger().info("Loading and backing up " + fileName + " file...");
		try {
			configFile = new AchievementCommentedYamlConfiguration(fileName, this);
		} catch (IOException | InvalidConfigurationException e) {
			this.getLogger().severe("Error while loading " + fileName + " file, disabling plugin.");
			this.getLogger().log(Level.SEVERE,
					"Verify your syntax by visiting yaml-online-parser.appspot.com and using the following logs:", e);
			successfulLoad = false;
			overrideDisable = true;
			Bukkit.getServer().getPluginManager().disablePlugin(this);
			return null;
		}

		try {
			configFile.backupConfiguration();
		} catch (IOException e) {
			this.getLogger().log(Level.SEVERE, "Error while backing up " + fileName + " file:", e);
			successfulLoad = false;
		}
		return configFile;
	}

	/**
	 * Extracts plugin parameters from the configuration file.
	 */
	private void extractParameters() {
		// Simple parsing of game version. Might need to be updated in the future depending on how the Minecraft
		// versions change in the future.
		int minecraftVersion = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);

		icon = StringEscapeUtils.unescapeJava(config.getString("Icon", "\u2618"));
		color = ChatColor.getByChar(config.getString("Color", "5").charAt(0));
		if (Strings.isNullOrEmpty(icon)) {
			chatHeader = "";
		} else {
			chatHeader = ChatColor.GRAY + "[" + color + icon + ChatColor.GRAY + "] ";
		}
		chatNotify = config.getBoolean("ChatNotify", false);

		restrictCreative = config.getBoolean("RestrictCreative", false);
		restrictSpectator = config.getBoolean("RestrictSpectator", true);
		// Spectator mode introduced in Minecraft 1.8.
		if (restrictSpectator && minecraftVersion < 8) {
			restrictSpectator = false;
			this.getLogger().warning(
					"Overriding configuration: disabling RestrictSpectator. Please set it to false in your config.");
		}
		excludedWorldSet = new HashSet<>(config.getList("ExcludedWorlds"));

		databaseBackup = config.getBoolean("DatabaseBackup", true);

		disabledCategorySet = new HashSet<>(config.getList("DisabledCategories"));
		// Need PetMaster with a minimum version of 1.4 for PetMasterGive and PetMasterReceive categories.
		if ((!disabledCategorySet.contains(NormalAchievements.PETMASTERGIVE.toString())
				|| !disabledCategorySet.contains(NormalAchievements.PETMASTERRECEIVE.toString()))
				&& (!Bukkit.getPluginManager().isPluginEnabled("PetMaster")
						|| Integer.parseInt(Character.toString(Bukkit.getPluginManager().getPlugin("PetMaster")
								.getDescription().getVersion().charAt(2))) < 4)) {
			disabledCategorySet.add(NormalAchievements.PETMASTERGIVE.toString());
			disabledCategorySet.add(NormalAchievements.PETMASTERRECEIVE.toString());
			this.getLogger()
					.warning("Overriding configuration: disabling PetMasterGive and PetMasterReceive categories.");
			this.getLogger().warning(
					"Ensure you have placed Pet Master with a minimum version of 1.4 in your plugins folder or add PetMasterGive and PetMasterReceive to the DisabledCategories list in your config.");
		}
		// Elytras introduced in Minecraft 1.9.
		if (!disabledCategorySet.contains(NormalAchievements.DISTANCEGLIDING.toString()) && minecraftVersion < 9) {
			disabledCategorySet.add(NormalAchievements.DISTANCEGLIDING.toString());
			this.getLogger().warning("Overriding configuration: disabling DistanceGliding category.");
			this.getLogger().warning(
					"Elytra are not available in your Minecraft version, please add DistanceGliding to the DisabledCategories list in your config.");
		}
		// Llamas introduced in Minecraft 1.11.
		if (!disabledCategorySet.contains(NormalAchievements.DISTANCELLAMA.toString()) && minecraftVersion < 11) {
			disabledCategorySet.add(NormalAchievements.DISTANCELLAMA.toString());
			this.getLogger().warning("Overriding configuration: disabling DistanceLlama category.");
			this.getLogger().warning(
					"Llamas not available in your Minecraft version, please add DistanceLlama to the DisabledCategories list in your config.");
		}

		playtimeTaskInterval = config.getInt("PlaytimeTaskInterval", 60);
		distanceTaskInterval = config.getInt("DistanceTaskInterval", 5);
		pooledRequestsTaskInterval = config.getInt("PooledRequestsTaskInterval", 60);
	}

	/**
	 * Initialises the command modules.
	 */
	private void initialiseCommands() {
		rewardParser = new RewardParser(this);
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
		toggleCommand = new ToggleCommand(this);
		resetCommand = new ResetCommand(this);
		easterEggCommand = new EasterEggCommand(this);
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

			// Enumerate the subcategories.
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
				int startOfMetadata = section.indexOf(':');
				if (startOfMetadata > -1) {
					// Permission ignores metadata (eg. sand:1) for Breaks, Places and Crafts categories.
					section = section.substring(0, startOfMetadata);
				}

				// Bukkit only allows permissions to be set once, check to ensure they were not previously set when
				// performing /aach reload.
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

		// Send remaining statistics to the database and close DatabaseManager.
		pooledRequestsSender.sendBatchedRequests();

		databaseManager.shutdown();

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
		// Map to an Advanced Achievements command.
		if ((args.length == 1) && !"help".equalsIgnoreCase(args[0])) {
			if ("book".equalsIgnoreCase(args[0])) {
				bookCommand.executeCommand(sender, null, "book");
			} else if ("hcaa".equalsIgnoreCase(args[0])) {
				easterEggCommand.executeCommand(sender, null, "easteregg");
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
			} else if ("toggle".equalsIgnoreCase(args[0])) {
				toggleCommand.executeCommand(sender, null, "toggle");
			} else {
				helpCommand.executeCommand(sender, args, null);
			}
		} else if ((args.length == 3) && "reset".equalsIgnoreCase(args[0])) {
			resetCommand.executeCommand(sender, args, "reset");
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
	 * Returns a map from achievement name (as stored in the database) to DisplayName. If multiple achievements have the
	 * same achievement name, only the first DisplayName will be tracked. If DisplayName for an achievement is empty or
	 * undefined, the value in the returned map will be an empty string.
	 * 
	 * @return Map from achievement name to user-friendly display name
	 */
	public Map<String, String> getAchievementsAndDisplayNames() {
		Map<String, String> achievementsAndDisplayNames = new HashMap<>();

		// Enumerate Commands achievements.
		for (String ach : config.getConfigurationSection("Commands").getKeys(false)) {
			String achName = config.getString("Commands." + ach + ".Name", "");
			String displayName = config.getString("Commands." + ach + ".DisplayName", "");

			if (!achievementsAndDisplayNames.containsKey(achName)) {
				achievementsAndDisplayNames.put(achName, displayName);
			}
		}

		// Enumerate the normal achievements.
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

		// Enumerate the achievements with multiple categories.
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

	public SQLDatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public DatabasePoolsManager getPoolsManager() {
		return poolsManager;
	}

	public RewardParser getRewardParser() {
		return rewardParser;
	}

	public String getChatHeader() {
		return chatHeader;
	}

	public boolean isRestrictCreative() {
		return restrictCreative;
	}

	public boolean isRestrictSpectator() {
		return restrictSpectator;
	}

	public boolean isChatNotify() {
		return chatNotify;
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

	public ToggleCommand getToggleCommand() {
		return toggleCommand;
	}

	public AchieveDistanceRunnable getDistanceRunnable() {
		return distanceRunnable;
	}

	public AchievePlayTimeRunnable getPlayTimeRunnable() {
		return playTimeRunnable;
	}

	public AchieveConnectionListener getConnectionListener() {
		return connectionListener;
	}

	public AchieveMilkLavaWaterListener getMilkLavaWaterListener() {
		return milkLavaWaterListener;
	}

	public AchieveTradeAnvilBrewSmeltListener getTradeAnvilBrewSmeltListener() {
		return tradeAnvilBrewSmeltListener;
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

	public FireworkListener getFireworkListener() {
		return fireworkListener;
	}

	public String getIcon() {
		return icon;
	}

	public ChatColor getColor() {
		return color;
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

	public AchievementCommentedYamlConfiguration getPluginGui() {
		return gui;
	}
}
