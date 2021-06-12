package com.hm.achievement.lifecycle;

import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitTask;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.Category;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.command.completer.CommandTabCompleter;
import com.hm.achievement.command.executable.ReloadCommand;
import com.hm.achievement.command.executor.PluginCommandExecutor;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.config.ConfigurationParser;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.AsyncCachedRequestsSender;
import com.hm.achievement.exception.PluginLoadError;
import com.hm.achievement.listener.FireworkListener;
import com.hm.achievement.listener.JoinListener;
import com.hm.achievement.listener.ListGUIListener;
import com.hm.achievement.listener.PlayerAdvancedAchievementListener;
import com.hm.achievement.listener.TeleportListener;
import com.hm.achievement.listener.UpdateChecker;
import com.hm.achievement.listener.statistics.AbstractListener;
import com.hm.achievement.placeholder.AchievementPlaceholderHook;
import com.hm.achievement.runnable.AchieveDistanceRunnable;
import com.hm.achievement.runnable.AchievePlayTimeRunnable;

import dagger.Lazy;

/**
 * Class in charge of loading/reloading the plugin. Orchestrates the different plugin components together.
 *
 * @author Pyves
 */
@Singleton
public class PluginLoader {

	private final AdvancedAchievements advancedAchievements;
	private final Logger logger;
	private final UpdateChecker updateChecker;
	private final ReloadCommand reloadCommand;
	private final Set<Reloadable> reloadables;
	private final AchievementMap achievementMap;

	// Listeners, to monitor various events.
	private final FireworkListener fireworkListener;
	private final JoinListener joinListener;
	private final ListGUIListener listGUIListener;
	private final PlayerAdvancedAchievementListener playerAdvancedAchievementListener;
	private final TeleportListener teleportListener;

	// Integration with PlaceholderAPI. Use lazy injection as it may or may not be used depending on runtime conditions.
	private final Lazy<AchievementPlaceholderHook> achievementPlaceholderHook;

	// Database related.
	private final AbstractDatabaseManager databaseManager;
	private final AsyncCachedRequestsSender asyncCachedRequestsSender;

	// Various other fields and parameters.
	private final PluginCommandExecutor pluginCommandExecutor;
	private final CommandTabCompleter commandTabCompleter;
	private final Set<Category> disabledCategories;
	private final YamlConfiguration mainConfig;
	private final ConfigurationParser configurationParser;

	// Plugin runnable classes.
	private final AchieveDistanceRunnable distanceRunnable;
	private final AchievePlayTimeRunnable playTimeRunnable;
	private final Cleaner cleaner;

	// Bukkit scheduler tasks.
	private BukkitTask asyncCachedRequestsSenderTask;
	private BukkitTask playedTimeTask;
	private BukkitTask distanceTask;
	private BukkitTask cleanerTask;

	@Inject
	public PluginLoader(AdvancedAchievements advancedAchievements, Logger logger, Set<Reloadable> reloadables,
			FireworkListener fireworkListener, JoinListener joinListener, ListGUIListener listGUIListener,
			PlayerAdvancedAchievementListener playerAdvancedAchievementListener, Cleaner cleaner,
			TeleportListener teleportListener, Lazy<AchievementPlaceholderHook> achievementPlaceholderHook,
			AbstractDatabaseManager databaseManager, AsyncCachedRequestsSender asyncCachedRequestsSender,
			PluginCommandExecutor pluginCommandExecutor, CommandTabCompleter commandTabCompleter,
			Set<Category> disabledCategories, @Named("main") YamlConfiguration mainConfig,
			ConfigurationParser configurationParser, AchieveDistanceRunnable distanceRunnable,
			AchievePlayTimeRunnable playTimeRunnable, UpdateChecker updateChecker, ReloadCommand reloadCommand,
			AchievementMap achievementMap) {
		this.advancedAchievements = advancedAchievements;
		this.logger = logger;
		this.reloadables = reloadables;
		this.fireworkListener = fireworkListener;
		this.joinListener = joinListener;
		this.listGUIListener = listGUIListener;
		this.playerAdvancedAchievementListener = playerAdvancedAchievementListener;
		this.cleaner = cleaner;
		this.teleportListener = teleportListener;
		this.achievementPlaceholderHook = achievementPlaceholderHook;
		this.databaseManager = databaseManager;
		this.asyncCachedRequestsSender = asyncCachedRequestsSender;
		this.pluginCommandExecutor = pluginCommandExecutor;
		this.commandTabCompleter = commandTabCompleter;
		this.disabledCategories = disabledCategories;
		this.mainConfig = mainConfig;
		this.configurationParser = configurationParser;
		this.distanceRunnable = distanceRunnable;
		this.playTimeRunnable = playTimeRunnable;
		this.updateChecker = updateChecker;
		this.reloadCommand = reloadCommand;
		this.achievementMap = achievementMap;
	}

	/**
	 * Loads the plugin.
	 *
	 * @throws PluginLoadError
	 */
	public void loadAdvancedAchievements() throws PluginLoadError {
		configurationParser.loadAndParseConfiguration();
		registerListeners();
		if (!databaseManager.isInitialised()) {
			databaseManager.initialise();
		}
		initialiseCommands();
		launchScheduledTasks();
		launchUpdateChecker();
		registerPermissions();
		reloadCommand.notifyObservers();
		linkPlaceholders();
	}

	/**
	 * Disables the plugin.
	 */
	public void disableAdvancedAchievements() {
		// Cancel scheduled tasks.
		if (asyncCachedRequestsSenderTask != null) {
			asyncCachedRequestsSenderTask.cancel();
		}
		if (cleanerTask != null) {
			cleanerTask.cancel();
		}
		if (playedTimeTask != null) {
			playedTimeTask.cancel();
		}
		if (distanceTask != null) {
			distanceTask.cancel();
		}

		// Send remaining statistics to the database and close DatabaseManager.
		asyncCachedRequestsSender.sendBatchedRequests();
		databaseManager.shutdown();

		logger.info("Remaining requests sent to the database, plugin successfully disabled.");
	}

	/**
	 * Registers the different event listeners so they can monitor server events. If relevant categories are disabled,
	 * listeners aren't registered.
	 */
	private void registerListeners() {
		logger.info("Registering event listeners...");
		HandlerList.unregisterAll(advancedAchievements);
		PluginManager pluginManager = advancedAchievements.getServer().getPluginManager();
		reloadables.forEach(r -> {
			if (r instanceof AbstractListener) {
				AbstractListener listener = (AbstractListener) r;
				if (!disabledCategories.contains(listener.getCategory())) {
					pluginManager.registerEvents(listener, advancedAchievements);
				}
			}
		});
		pluginManager.registerEvents(fireworkListener, advancedAchievements);
		pluginManager.registerEvents(joinListener, advancedAchievements);
		pluginManager.registerEvents(listGUIListener, advancedAchievements);
		pluginManager.registerEvents(playerAdvancedAchievementListener, advancedAchievements);
		pluginManager.registerEvents(teleportListener, advancedAchievements);
	}

	/**
	 * Links the plugin's custom command tab completer and command executor.
	 */
	private void initialiseCommands() {
		logger.info("Setting up command executor and custom tab completers...");

		PluginCommand pluginCommand = Bukkit.getPluginCommand("aach");
		pluginCommand.setTabCompleter(commandTabCompleter);
		pluginCommand.setExecutor(pluginCommandExecutor);
	}

	/**
	 * Launches asynchronous scheduled tasks.
	 */
	private void launchScheduledTasks() {
		logger.info("Launching scheduled tasks...");

		// Schedule a repeating task to group database queries when statistics are modified.
		if (asyncCachedRequestsSenderTask == null) {
			long taskPeriod = mainConfig.getBoolean("BungeeMode") ? 40L : 1200L;
			asyncCachedRequestsSenderTask = Bukkit.getScheduler().runTaskTimerAsynchronously(advancedAchievements,
					asyncCachedRequestsSender, taskPeriod, taskPeriod);
		}

		if (cleanerTask == null) {
			long taskPeriod = mainConfig.getBoolean("BungeeMode") ? 50L : 20000L;
			cleanerTask = Bukkit.getScheduler().runTaskTimer(advancedAchievements, cleaner, taskPeriod, taskPeriod);
		}

		// Schedule a repeating task to monitor played time for each player (not directly related to an event).
		if (playedTimeTask != null) {
			playedTimeTask.cancel();
		}
		if (!disabledCategories.contains(NormalAchievements.PLAYEDTIME)) {
			int configPlaytimeTaskInterval = mainConfig.getInt("PlaytimeTaskInterval");
			playedTimeTask = Bukkit.getScheduler().runTaskTimer(advancedAchievements, playTimeRunnable,
					configPlaytimeTaskInterval * 10L, configPlaytimeTaskInterval * 20L);
		}

		// Schedule a repeating task to monitor distances travelled by each player (not directly related to an event).
		if (distanceTask != null) {
			distanceTask.cancel();
		}
		if (!disabledCategories.contains(NormalAchievements.DISTANCEFOOT)
				|| !disabledCategories.contains(NormalAchievements.DISTANCEPIG)
				|| !disabledCategories.contains(NormalAchievements.DISTANCEHORSE)
				|| !disabledCategories.contains(NormalAchievements.DISTANCEMINECART)
				|| !disabledCategories.contains(NormalAchievements.DISTANCEBOAT)
				|| !disabledCategories.contains(NormalAchievements.DISTANCEGLIDING)
				|| !disabledCategories.contains(NormalAchievements.DISTANCELLAMA)
				|| !disabledCategories.contains(NormalAchievements.DISTANCESNEAKING)) {
			int configDistanceTaskInterval = mainConfig.getInt("DistanceTaskInterval");
			distanceTask = Bukkit.getScheduler().runTaskTimer(advancedAchievements, distanceRunnable,
					configDistanceTaskInterval * 40L, configDistanceTaskInterval * 20L);
		}
	}

	/**
	 * Launches an update check task. If updateChecker already registered (i.e. reload), does not check for update
	 * again. If CheckForUpdate switched to false unregisters listener.
	 */
	private void launchUpdateChecker() {
		if (mainConfig.getBoolean("CheckForUpdate")) {
			advancedAchievements.getServer().getPluginManager().registerEvents(updateChecker, advancedAchievements);
			updateChecker.launchUpdateCheckerTask();
		}
	}

	/**
	 * Registers permissions that depend on the user's configuration file (for MultipleAchievements; for instance for
	 * stone breaks, achievement.count.breaks.stone will be registered).
	 * 
	 * Bukkit only allows permissions to be set once, check that the permission node is null to ensure it was not
	 * previously set, before an /aach reload for example.
	 */
	private void registerPermissions() {
		logger.info("Registering permissions...");

		PluginManager pluginManager = Bukkit.getPluginManager();
		for (MultipleAchievements category : MultipleAchievements.values()) {
			Permission categoryParent = new Permission(category.toPermName(), PermissionDefault.TRUE);
			for (String section : achievementMap.getSubcategoriesForCategory(category)) {
				// Permission ignores metadata (eg. sand:1) for Breaks, Places and Crafts categories and don't take
				// spaces into account.
				section = StringUtils.deleteWhitespace(StringUtils.substringBefore(section, ":"));

				for (String groupElement : StringUtils.split(section, '|')) {
					String permissionNode = category.toChildPermName(groupElement);
					if (pluginManager.getPermission(permissionNode) == null) {
						Permission perm = new Permission(permissionNode, PermissionDefault.TRUE);
						perm.addParent(categoryParent, true);
						pluginManager.addPermission(perm);
					}
				}
			}
		}

		Permission achievementParent = new Permission("achievement.*", PermissionDefault.OP);
		for (String name : achievementMap.getAllNames()) {
			String permissionNode = "achievement." + name;
			if (pluginManager.getPermission(permissionNode) == null) {
				Permission perm = new Permission(permissionNode, PermissionDefault.TRUE);
				perm.addParent(achievementParent, true);
				pluginManager.addPermission(perm);
			}
		}
	}

	/**
	 * Links the PlaceholderAPI plugin.
	 */
	private void linkPlaceholders() {
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")
				&& !achievementPlaceholderHook.get().isRegistered()) {
			achievementPlaceholderHook.get().register();
		}
	}
}
