package com.hm.achievement;

import codecrafter47.bungeetablistplus.api.bukkit.BungeeTabListPlusBukkitAPI;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.command.*;
import com.hm.achievement.db.*;
import com.hm.achievement.exception.PluginLoadError;
import com.hm.achievement.gui.CategoryGUI;
import com.hm.achievement.gui.MainGUI;
import com.hm.achievement.listener.*;
import com.hm.achievement.runnable.AchieveDistanceRunnable;
import com.hm.achievement.runnable.AchievePlayTimeRunnable;
import com.hm.achievement.utils.*;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import com.hm.mcshared.particle.ReflectionUtils.PackageType;
import com.hm.mcshared.update.UpdateChecker;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

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
 * @version 5.5
 * @author Pyves
 */
public class AdvancedAchievements extends JavaPlugin implements Reloadable {

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
	private AchieveBreedListener breedListener;
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
	private AddCommand addCommand;
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
	private GenerateCommand generateCommand;
	private EasterEggCommand easterEggCommand;
	private CommandTabCompleter commandTabCompleter;
	private UpdateChecker updateChecker;

	// Language, configuration and GUI related.
	private CommentedYamlConfiguration config;
	private CommentedYamlConfiguration lang;
	private CommentedYamlConfiguration gui;

	// Database related.
	private final DatabaseCacheManager cacheManager;
	private AbstractSQLDatabaseManager databaseManager;
	private AsyncCachedRequestsSender asyncCachedRequestsSender;

	// Plugin runnable classes.
	private AchieveDistanceRunnable distanceRunnable;
	private AchievePlayTimeRunnable playTimeRunnable;

	// GUI classes.
	private MainGUI mainGUI;
	private CategoryGUI categoryGUI;

	// Bukkit scheduler tasks.
	private BukkitTask asyncCachedRequestsSenderTask;
	private BukkitTask playedTimeTask;
	private BukkitTask distanceTask;

	// Various other fields and parameters.
	private final Map<String, String> achievementsAndDisplayNames;
	private Map<String, List<Long>> sortedThresholds;
	private String chatHeader;
	private Set<String> disabledCategorySet;

	public AdvancedAchievements() {
		cacheManager = new DatabaseCacheManager(this);
		achievementsAndDisplayNames = new HashMap<>();
		sortedThresholds = new HashMap<>();
	}

	@Override
	public void onEnable() {
		// Start enabling plugin.
		long startTime = System.currentTimeMillis();

		try {
			config = loadAndBackupFile("config.yml");
			lang = loadAndBackupFile(getPluginConfig().getString("LanguageFileName", "lang.yml"));
			gui = loadAndBackupFile("gui.yml");

			// Update configurations from previous versions of the plugin; only if server reload or restart.
			FileUpdater fileUpdater = new FileUpdater(this);
			fileUpdater.updateOldConfiguration(config);
			fileUpdater.updateOldLanguage(lang);
			fileUpdater.updateOldGUI(gui);

			disabledCategorySet = extractDisabledCategories(config);
			logAchievementStats();
			initialiseCommands();
			registerListeners();
			initialiseTabCompleter();
			initialiseGUIs();
			selectAndInitialiseDatabaseManager();
		} catch (PluginLoadError e) {
			getLogger().log(Level.SEVERE,
					"A non recoverable error was encountered while loading the plugin, disabling it.", e);
			Bukkit.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		launchScheduledTasks();
		loadAndRegisterReloadables();
		registerCleanables();

		if (Bukkit.getPluginManager().isPluginEnabled("BungeeTabListPlus")) {
			BungeeTabListPlusBukkitAPI.registerVariable(this, new AchievementCountBungeeTabListPlusVariable(this));
		}

		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			new AchievementPlaceholderHook(this).hook();
		}

		getLogger().info("Plugin has finished loading and is ready to run! Took "
				+ (System.currentTimeMillis() - startTime) + "ms.");
	}

	@Override
	public void onDisable() {
		// Cancel scheduled tasks.
		if (asyncCachedRequestsSenderTask != null) {
			asyncCachedRequestsSenderTask.cancel();
		}
		if (playedTimeTask != null) {
			playedTimeTask.cancel();
		}
		if (distanceTask != null) {
			distanceTask.cancel();
		}

		// Send remaining statistics to the database and close DatabaseManager.
		if (asyncCachedRequestsSender != null) {
			asyncCachedRequestsSender.sendBatchedRequests();
		}

		if (databaseManager != null) {
			databaseManager.shutdown();
		}

		getLogger().info("Remaining requests sent to database, plugin disabled.");
	}

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
			} else if ("generate".equalsIgnoreCase(args[0])) {
				generateCommand.executeCommand(sender, null, "generate");
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
		} else if ((args.length == 4) && "add".equalsIgnoreCase(args[0])) {
			addCommand.executeCommand(sender, args, "add");
		} else {
			helpCommand.executeCommand(sender, args, null);
		}
		return true;
	}

	@Override
	public void extractConfigurationParameters() {
		parseHeader();
		parseAchievementsAndDisplayNames();
		parseAchievementThresholds();
		registerPermissions();

		// If user switched config parameter to false, unregister UpdateChecker; if user switched config pameter to
		// false, launch an UpdateChecker task. Otherwise do not recheck for update on /aach reload.
		if (!config.getBoolean("CheckForUpdate", true)) {
			PlayerJoinEvent.getHandlerList().unregister(updateChecker);
		} else if (updateChecker == null) {
			initialiseUpdateChecker();
		}
	}

	/**
	 * Loads and backs up file fileName.
	 * 
	 * @param fileName
	 * @return the loaded file CommentedYamlConfiguration
	 * @throws PluginLoadError
	 */
	public CommentedYamlConfiguration loadAndBackupFile(String fileName) throws PluginLoadError {
		getLogger().info("Loading and backing up " + fileName + " file...");

		CommentedYamlConfiguration configFile;
		try {
			configFile = new CommentedYamlConfiguration(fileName, this);
		} catch (IOException | InvalidConfigurationException e) {
			throw new PluginLoadError("Error while loading " + fileName
					+ ".Verify its syntax on yaml-online-parser.appspot.com and use the following logs", e);
		}
		try {
			configFile.backupConfiguration();
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Error while backing up " + configFile.getName() + ".", e);
		}
		return configFile;
	}

	/**
	 * Extracts disabled categories from the configuration file.
	 * 
	 * @param config
	 * 
	 * @return the set containing the names of the disabled categories
	 */
	public Set<String> extractDisabledCategories(CommentedYamlConfiguration config) {
		// Simple parsing of game version. Might need to be updated in the future depending on how the Minecraft
		// versions change in the future.
		int minecraftVersion = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);

		Set<String> disabledCategorySet = new HashSet<>(config.getList("DisabledCategories"));
		// Need PetMaster with a minimum version of 1.4 for PetMasterGive and PetMasterReceive categories.
		if ((!disabledCategorySet.contains(NormalAchievements.PETMASTERGIVE.toString())
				|| !disabledCategorySet.contains(NormalAchievements.PETMASTERRECEIVE.toString()))
				&& (!Bukkit.getPluginManager().isPluginEnabled("PetMaster")
						|| Integer.parseInt(Character.toString(Bukkit.getPluginManager().getPlugin("PetMaster")
								.getDescription().getVersion().charAt(2))) < 4)) {
			disabledCategorySet.add(NormalAchievements.PETMASTERGIVE.toString());
			disabledCategorySet.add(NormalAchievements.PETMASTERRECEIVE.toString());
			getLogger().warning("Overriding configuration: disabling PetMasterGive and PetMasterReceive categories.");
			getLogger().warning(
					"Ensure you have placed Pet Master with a minimum version of 1.4 in your plugins folder or add PetMasterGive and PetMasterReceive to the DisabledCategories list in your config.");
		}
		// Elytras introduced in Minecraft 1.9.
		if (!disabledCategorySet.contains(NormalAchievements.DISTANCEGLIDING.toString()) && minecraftVersion < 9) {
			disabledCategorySet.add(NormalAchievements.DISTANCEGLIDING.toString());
			getLogger().warning("Overriding configuration: disabling DistanceGliding category.");
			getLogger().warning(
					"Elytra are not available in your Minecraft version, please add DistanceGliding to the DisabledCategories list in your config.");
		}
		// Llamas introduced in Minecraft 1.11.
		if (!disabledCategorySet.contains(NormalAchievements.DISTANCELLAMA.toString()) && minecraftVersion < 11) {
			disabledCategorySet.add(NormalAchievements.DISTANCELLAMA.toString());
			getLogger().warning("Overriding configuration: disabling DistanceLlama category.");
			getLogger().warning(
					"Llamas not available in your Minecraft version, please add DistanceLlama to the DisabledCategories list in your config.");
		}
		// Breeding event introduced in Spigot 1319 (Minecraft 1.10.2).
		if (!disabledCategorySet.contains(MultipleAchievements.BREEDING.toString()) && minecraftVersion < 10) {
			disabledCategorySet.add(MultipleAchievements.BREEDING.toString());
			getLogger().warning("Overriding configuration: disabling Breeding category.");
			getLogger().warning(
					"The breeding event is not available in your server version, please add Breeding to the DisabledCategories list in your config.");
		}

		return disabledCategorySet;
	}

	/**
	 * Logs number of achievements and disabled categories.
	 */
	private void logAchievementStats() {
		int totalAchievements = 0;
		int categoriesInUse = 0;

		// Enumerate Commands achievements.
		if (!disabledCategorySet.contains("Commands")) {
			int keyCount = config.getConfigurationSection("Commands").getKeys(false).size();
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

			int keyCount = config.getConfigurationSection(category.toString()).getKeys(false).size();
			if (keyCount > 0) {
				++categoriesInUse;
				totalAchievements += keyCount;
			}
		}

		// Enumerate the achievements with multiple categories.
		for (MultipleAchievements category : MultipleAchievements.values()) {
			if (disabledCategorySet.contains(category.toString())) {
				continue;
			}

			Set<String> categorySections = config.getConfigurationSection(category.toString()).getKeys(false);

			if (categorySections.isEmpty()) {
				continue;
			}

			++categoriesInUse;

			// Enumerate the subcategories.
			for (String section : categorySections) {
				int achievementCount = config.getConfigurationSection(category + "." + section).getKeys(false).size();
				if (achievementCount > 0) {
					totalAchievements += achievementCount;
				}
			}
		}
		getLogger().info("Loaded " + totalAchievements + " achievements in " + categoriesInUse + " categories.");

		if (!disabledCategorySet.isEmpty()) {
			String disabledCategories;

			if (disabledCategorySet.size() == 1) {
				disabledCategories = disabledCategorySet.size() + " disabled category: "
						+ disabledCategorySet.toString();
			} else {
				disabledCategories = disabledCategorySet.size() + " disabled categories: "
						+ disabledCategorySet.toString();
			}

			getLogger().info(disabledCategories);
		}
	}

	/**
	 * Initialises the command modules.
	 */
	private void initialiseCommands() {
		getLogger().info("Initialising command modules...");

		rewardParser = new RewardParser(this);
		giveCommand = new GiveCommand(this);
		addCommand = new AddCommand(this);
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
		generateCommand = new GenerateCommand(this);
		easterEggCommand = new EasterEggCommand(this);
	}

	/**
	 * Registers the different event listeners so they can monitor server events. If relevant categories are disabled,
	 * listeners aren't registered.
	 */
	private void registerListeners() {
		getLogger().info("Registering event listeners...");
		PluginManager pm = getServer().getPluginManager();

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

		if (!disabledCategorySet.contains(MultipleAchievements.BREEDING.toString())) {
			breedListener = new AchieveBreedListener(this);
			pm.registerEvents(breedListener, this);
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
				|| !disabledCategorySet.contains(NormalAchievements.DISTANCELLAMA.toString())
				|| !disabledCategorySet.contains(NormalAchievements.ENDERPEARLS.toString())) {
			teleportRespawnListener = new AchieveTeleportRespawnListener(this);
			pm.registerEvents(teleportRespawnListener, this);
		}

		if (!disabledCategorySet.contains(NormalAchievements.PETMASTERGIVE.toString())
				|| !disabledCategorySet.contains(NormalAchievements.PETMASTERRECEIVE.toString())) {
			petMasterGiveReceiveListener = new AchievePetMasterGiveReceiveListener(this);
			pm.registerEvents(petMasterGiveReceiveListener, this);
		}

		connectionListener = new AchieveConnectionListener(this);
		pm.registerEvents(connectionListener, this);

		quitListener = new QuitListener(this);
		pm.registerEvents(quitListener, this);

		listGUIListener = new ListGUIListener(this);
		pm.registerEvents(listGUIListener, this);

		fireworkListener = new FireworkListener(this);
		pm.registerEvents(fireworkListener, this);

		playerAdvancedAchievementListener = new PlayerAdvancedAchievementListener(this);
		pm.registerEvents(playerAdvancedAchievementListener, this);
	}

	/**
	 * Initialises the plugin's custom command tab completer.
	 */
	private void initialiseTabCompleter() {
		getLogger().info("Setting up custom tab completers...");
		commandTabCompleter = new CommandTabCompleter(this);
		getCommand("aach").setTabCompleter(commandTabCompleter);
	}

	/**
	 * Initialises the plugin's main and category GUIs.
	 */
	private void initialiseGUIs() {
		getLogger().info("Setting up graphical interfaces...");
		mainGUI = new MainGUI(this);
		categoryGUI = new CategoryGUI(this);
	}

	/**
	 * Selects a database manager implementation (SQLite, MySQL or PostgreSQL) and initialises it.
	 * 
	 * @throws PluginLoadError
	 */
	private void selectAndInitialiseDatabaseManager() throws PluginLoadError {
		String dataHandler = config.getString("DatabaseType", "sqlite");
		if ("mysql".equalsIgnoreCase(dataHandler)) {
			databaseManager = new MySQLDatabaseManager(this);
		} else if ("postgresql".equalsIgnoreCase(dataHandler)) {
			databaseManager = new PostgreSQLDatabaseManager(this);
		} else {
			// User has specified "sqlite" or an invalid type.
			databaseManager = new SQLiteDatabaseManager(this);
		}
		databaseManager.initialise();
	}

	/**
	 * Launches asynchronous scheduled tasks.
	 */
	private void launchScheduledTasks() {
		getLogger().info("Launching scheduled tasks...");

		asyncCachedRequestsSender = new AsyncCachedRequestsSender(this);
		// Schedule a repeating task to group database queries when statistics are modified.
		int configPooledRequestsTaskInterval = config.getInt("PooledRequestsTaskInterval", 10);
		asyncCachedRequestsSenderTask = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this,
				asyncCachedRequestsSender, configPooledRequestsTaskInterval * 40L,
				configPooledRequestsTaskInterval * 20L);

		// Schedule a repeating task to monitor played time for each player (not directly related to an event).
		if (!disabledCategorySet.contains(NormalAchievements.PLAYEDTIME.toString())) {
			playTimeRunnable = new AchievePlayTimeRunnable(this);
			int configPlaytimeTaskInterval = config.getInt("PlaytimeTaskInterval", 60);
			playedTimeTask = Bukkit.getServer().getScheduler().runTaskTimer(this, playTimeRunnable,
					configPlaytimeTaskInterval * 10L, configPlaytimeTaskInterval * 20L);
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
			int configDistanceTaskInterval = config.getInt("DistanceTaskInterval", 5);
			distanceTask = Bukkit.getServer().getScheduler().runTaskTimer(this, distanceRunnable,
					configDistanceTaskInterval * 40L, configDistanceTaskInterval * 20L);
		}
	}

	/**
	 * Performs an initial load of all the Reloadable fields. Registers them as observers of the ReloadCommand.
	 */
	private void loadAndRegisterReloadables() {
		extractConfigurationParameters();
		reloadCommand.registerReloadable(this);
		try {
			for (Field field : AdvancedAchievements.class.getDeclaredFields()) {
				Object fieldObject = field.get(this);
				if (fieldObject instanceof Reloadable) {
					Reloadable reloadable = (Reloadable) fieldObject;
					reloadable.extractConfigurationParameters();
					reloadCommand.registerReloadable(reloadable);
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			getLogger().severe("Unexpected error while registering Reloadables.");
		}
	}

	/**
	 * Registers all the Cleanable fields as observers of the QuitListener.
	 */
	private void registerCleanables() {
		try {
			for (Field field : AdvancedAchievements.class.getDeclaredFields()) {
				Object fieldObject = field.get(this);
				if (fieldObject instanceof Cleanable) {
					quitListener.registerCleanable((Cleanable) fieldObject);
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			getLogger().severe("Unexpected error while registering Cleanables.");
		}
	}

	/**
	 * Launches an update check task.
	 */
	private void initialiseUpdateChecker() {
		PluginManager pm = getServer().getPluginManager();
		updateChecker = new UpdateChecker(this,
				"https://raw.githubusercontent.com/PyvesB/AdvancedAchievements/master/pom.xml", "achievement.update",
				chatHeader, "spigotmc.org/resources/advanced-achievements.6239",
				"dev.bukkit.org/bukkit-plugins/advanced-achievements/files");
		pm.registerEvents(updateChecker, this);
		updateChecker.launchUpdateCheckerTask();
	}

	/**
	 * Parses the plugin's header, used throughout the project.
	 */
	private void parseHeader() {
		String icon = StringEscapeUtils.unescapeJava(config.getString("Icon", "\u2618"));
		if (StringUtils.isNotBlank(icon)) {
			String coloredIcon = ChatColor.getByChar(config.getString("Color", "5").charAt(0)) + icon;
			chatHeader = ChatColor.translateAlternateColorCodes('&',
					StringUtils.replace(config.getString("ChatHeader", "&7[%ICON%&7]"), "%ICON%", coloredIcon)) + " ";
		} else {
			chatHeader = "";
		}
	}

	/**
	 * Creates a map from achievement name (as stored in the database) to DisplayName. If multiple achievements have the
	 * same achievement name, only the first DisplayName will be tracked. If DisplayName for an achievement is empty or
	 * undefined, the value in the returned map will be an empty string.
	 */
	private void parseAchievementsAndDisplayNames() {
		achievementsAndDisplayNames.clear();

		// Enumerate Commands achievements.
		for (String ach : config.getConfigurationSection("Commands").getKeys(false)) {
			String achName = config.getString("Commands." + ach + ".Name", "");
			String displayName = config.getString("Commands." + ach + ".DisplayName", "");

			if (!achievementsAndDisplayNames.containsKey(achName)) {
				achievementsAndDisplayNames.put(achName, displayName);
			} else {
				getLogger()
						.warning("Duplicate achievement Name (" + achName + "), this may lead to undefined behaviour.");
			}
		}

		// Enumerate the normal achievements.
		for (NormalAchievements category : NormalAchievements.values()) {
			for (String ach : config.getConfigurationSection(category.toString()).getKeys(false)) {
				String achName = config.getString(category + "." + ach + ".Name", "");
				String displayName = config.getString(category + "." + ach + ".DisplayName", "");

				if (!achievementsAndDisplayNames.containsKey(achName)) {
					achievementsAndDisplayNames.put(achName, displayName);
				} else {
					getLogger().warning(
							"Duplicate achievement Name (" + achName + "), this may lead to undefined behaviour.");
				}
			}
		}

		// Enumerate the achievements with multiple categories.
		for (MultipleAchievements category : MultipleAchievements.values()) {
			for (String section : config.getConfigurationSection(category.toString()).getKeys(false)) {
				ConfigurationSection subcategoryConfig = config.getConfigurationSection(category + "." + section);
				for (String level : subcategoryConfig.getKeys(false)) {
					String achName = config.getString(category + "." + section + '.' + level + ".Name", "");
					String displayName = config.getString(category + "." + section + '.' + level + ".DisplayName", "");

					if (!achievementsAndDisplayNames.containsKey(achName)) {
						achievementsAndDisplayNames.put(achName, displayName);
					} else {
						getLogger().warning(
								"Duplicate achievement Name (" + achName + "), this may lead to undefined behaviour.");
					}
				}
			}
		}
	}

	/**
	 * Creates a map from categories (or categories + subcategories) to lists of sorted thresholds.
	 */
	private void parseAchievementThresholds() {
		sortedThresholds.clear();

		// Enumerate the normal achievements.
		for (NormalAchievements category : NormalAchievements.values()) {
			List<Long> thresholds = new ArrayList<>();
			String categoryName = category.toString();
			for (String threshold : config.getConfigurationSection(categoryName).getKeys(false)) {
				thresholds.add(Long.valueOf(threshold));
			}
			thresholds.sort(null);
			sortedThresholds.put(categoryName, thresholds);
		}

		// Enumerate the achievements with multiple categories.
		for (MultipleAchievements category : MultipleAchievements.values()) {
			String categoryName = category.toString();
			for (String section : config.getConfigurationSection(categoryName).getKeys(false)) {
				List<Long> thresholds = new ArrayList<>();
				for (String threshold : config.getConfigurationSection(categoryName + "." + section).getKeys(false)) {
					thresholds.add(Long.valueOf(threshold));
				}
				thresholds.sort(null);
				sortedThresholds.put(categoryName + "." + section, thresholds);
			}
		}
	}

	/**
	 * Registers permissions that depend on the user's configuration file (for MultipleAchievements; for instance for
	 * stone breaks, achievement.count.breaks.stone will be registered).
	 */
	private void registerPermissions() {
		getLogger().info("Registering permissions...");

		PluginManager pluginManager = getServer().getPluginManager();
		for (MultipleAchievements category : MultipleAchievements.values()) {
			for (String section : config.getConfigurationSection(category.toString()).getKeys(false)) {
				int startOfMetadata = section.indexOf(':');
				if (startOfMetadata > -1) {
					// Permission ignores metadata (eg. sand:1) for Breaks, Places and Crafts categories.
					section = section.substring(0, startOfMetadata);
				}
				if (category == MultipleAchievements.PLAYERCOMMANDS) {
					// Permissions don't take spaces into account for this category.
					section = StringUtils.replace(section, " ", "");
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

	public Map<String, String> getAchievementsAndDisplayNames() {
		return achievementsAndDisplayNames;
	}

	public Map<String, List<Long>> getSortedThresholds() {
		return sortedThresholds;
	}

	public AbstractSQLDatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public DatabaseCacheManager getCacheManager() {
		return cacheManager;
	}

	public RewardParser getRewardParser() {
		return rewardParser;
	}

	public String getChatHeader() {
		return chatHeader;
	}

	public Set<String> getDisabledCategorySet() {
		return disabledCategorySet;
	}

	public MainGUI getMainGUI() {
		return mainGUI;
	}

	public CategoryGUI getCategoryGUI() {
		return categoryGUI;
	}

	public ToggleCommand getToggleCommand() {
		return toggleCommand;
	}

	public AchieveDistanceRunnable getDistanceRunnable() {
		return distanceRunnable;
	}

	public AchievePetMasterGiveReceiveListener getPetMasterGiveReceiveListener() {
		return petMasterGiveReceiveListener;
	}

	public FireworkListener getFireworkListener() {
		return fireworkListener;
	}

	public CommentedYamlConfiguration getPluginConfig() {
		return config;
	}

	public void setPluginConfig(CommentedYamlConfiguration config) {
		this.config = config;
	}

	@Override
	@Deprecated
	public FileConfiguration getConfig() {
		return null;
	}

	public CommentedYamlConfiguration getPluginLang() {
		return lang;
	}

	public void setPluginLang(CommentedYamlConfiguration lang) {
		this.lang = lang;
	}

	public CommentedYamlConfiguration getPluginGui() {
		return gui;
	}

	public void setGui(CommentedYamlConfiguration gui) {
		this.gui = gui;
	}
}
