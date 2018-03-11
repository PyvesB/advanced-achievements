package com.hm.achievement.lifecycle;

import java.util.Arrays;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitTask;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.Category;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.command.CommandTabCompleter;
import com.hm.achievement.command.PluginCommandExecutor;
import com.hm.achievement.command.ReloadCommand;
import com.hm.achievement.config.ConfigurationParser;
import com.hm.achievement.db.AbstractSQLDatabaseManager;
import com.hm.achievement.db.AsyncCachedRequestsSender;
import com.hm.achievement.exception.PluginLoadError;
import com.hm.achievement.listener.AchieveArrowListener;
import com.hm.achievement.listener.AchieveBedListener;
import com.hm.achievement.listener.AchieveBlockBreakListener;
import com.hm.achievement.listener.AchieveBlockPlaceListener;
import com.hm.achievement.listener.AchieveBreedListener;
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
import com.hm.achievement.placeholder.AchievementCountBungeeTabListPlusVariable;
import com.hm.achievement.placeholder.AchievementPlaceholderHook;
import com.hm.achievement.runnable.AchieveDistanceRunnable;
import com.hm.achievement.runnable.AchievePlayTimeRunnable;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import com.hm.mcshared.update.UpdateChecker;

import codecrafter47.bungeetablistplus.api.bukkit.BungeeTabListPlusBukkitAPI;
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

	// Listeners, to monitor events and manage stats.
	private final AchieveConnectionListener connectionListener;
	private final AchieveDeathListener deathListener;
	private final AchieveArrowListener arrowListener;
	private final AchieveSnowballEggListener snowballEggListener;
	private final AchieveFishListener fishListener;
	private final AchieveItemBreakListener itemBreakListener;
	private final AchieveConsumeListener consumeListener;
	private final AchieveShearListener shearListener;
	private final AchieveMilkLavaWaterListener milkLavaWaterListener;
	private final AchieveTradeAnvilBrewSmeltListener tradeAnvilBrewSmeltListener;
	private final AchieveEnchantListener enchantmentListener;
	private final AchieveBedListener bedListener;
	private final AchieveXPListener xpListener;
	private final AchieveDropListener dropListener;
	private final AchievePickupListener pickupListener;
	private final AchieveHoeFertiliseFireworkMusicListener hoeFertiliseFireworkMusicListener;
	private final AchieveTameListener tameListener;
	private final AchieveBreedListener breedListener;
	private final AchieveBlockPlaceListener blockPlaceListener;
	private final AchieveBlockBreakListener blockBreakListener;
	private final AchieveKillListener killListener;
	private final AchieveCraftListener craftListener;
	private final AchievePlayerCommandListener playerCommandListener;
	private final AchieveTeleportRespawnListener teleportRespawnListener;
	private final AchievePetMasterGiveReceiveListener petMasterGiveReceiveListener;
	private final QuitListener quitListener;
	private final ListGUIListener listGUIListener;
	private final FireworkListener fireworkListener;
	private final PlayerAdvancedAchievementListener playerAdvancedAchievementListener;

	// Integrations with other plugins. Use lazy injection as these may or may not be used depending on runtime
	// conditions.
	private final Lazy<AchievementPlaceholderHook> achievementPlaceholderHook;
	private final Lazy<AchievementCountBungeeTabListPlusVariable> achievementCountBungeeTabListPlusVariable;

	// Database related.
	private final AbstractSQLDatabaseManager databaseManager;
	private final AsyncCachedRequestsSender asyncCachedRequestsSender;

	// Various other fields and parameters.
	private final PluginCommandExecutor pluginCommandExecutor;
	private final CommandTabCompleter commandTabCompleter;
	private final Set<String> disabledCategorySet;
	private final CommentedYamlConfiguration mainConfig;
	private final ConfigurationParser configurationParser;

	// Plugin runnable classes.
	private final AchieveDistanceRunnable distanceRunnable;
	private final AchievePlayTimeRunnable playTimeRunnable;

	// Bukkit scheduler tasks.
	private BukkitTask asyncCachedRequestsSenderTask;
	private BukkitTask playedTimeTask;
	private BukkitTask distanceTask;

	@Inject
	public PluginLoader(AdvancedAchievements advancedAchievements, Logger logger,
			AchieveConnectionListener connectionListener, AchieveDeathListener deathListener,
			AchieveArrowListener arrowListener, AchieveSnowballEggListener snowballEggListener,
			AchieveFishListener fishListener, AchieveItemBreakListener itemBreakListener,
			AchieveConsumeListener consumeListener, AchieveShearListener shearListener,
			AchieveMilkLavaWaterListener milkLavaWaterListener,
			AchieveTradeAnvilBrewSmeltListener tradeAnvilBrewSmeltListener, AchieveEnchantListener enchantmentListener,
			AchieveBedListener bedListener, AchieveXPListener xpListener, AchieveDropListener dropListener,
			AchievePickupListener pickupListener, AchieveHoeFertiliseFireworkMusicListener hoeFertiliseFireworkMusicListener,
			AchieveTameListener tameListener, AchieveBreedListener breedListener,
			AchieveBlockPlaceListener blockPlaceListener, AchieveBlockBreakListener blockBreakListener,
			AchieveKillListener killListener, AchieveCraftListener craftListener,
			AchievePlayerCommandListener playerCommandListener, AchieveTeleportRespawnListener teleportRespawnListener,
			AchievePetMasterGiveReceiveListener petMasterGiveReceiveListener, QuitListener quitListener,
			ListGUIListener listGUIListener, FireworkListener fireworkListener,
			PlayerAdvancedAchievementListener playerAdvancedAchievementListener,
			Lazy<AchievementPlaceholderHook> achievementPlaceholderHook,
			Lazy<AchievementCountBungeeTabListPlusVariable> achievementCountBungeeTabListPlusVariable,
			AbstractSQLDatabaseManager databaseManager, AsyncCachedRequestsSender asyncCachedRequestsSender,
			PluginCommandExecutor pluginCommandExecutor, CommandTabCompleter commandTabCompleter,
			Set<String> disabledCategorySet, @Named("main") CommentedYamlConfiguration mainConfig,
			ConfigurationParser configurationParser, AchieveDistanceRunnable distanceRunnable,
			AchievePlayTimeRunnable playTimeRunnable, UpdateChecker updateChecker, ReloadCommand reloadCommand) {
		this.advancedAchievements = advancedAchievements;
		this.logger = logger;
		this.connectionListener = connectionListener;
		this.deathListener = deathListener;
		this.arrowListener = arrowListener;
		this.snowballEggListener = snowballEggListener;
		this.fishListener = fishListener;
		this.itemBreakListener = itemBreakListener;
		this.consumeListener = consumeListener;
		this.shearListener = shearListener;
		this.milkLavaWaterListener = milkLavaWaterListener;
		this.tradeAnvilBrewSmeltListener = tradeAnvilBrewSmeltListener;
		this.enchantmentListener = enchantmentListener;
		this.bedListener = bedListener;
		this.xpListener = xpListener;
		this.dropListener = dropListener;
		this.pickupListener = pickupListener;
		this.hoeFertiliseFireworkMusicListener = hoeFertiliseFireworkMusicListener;
		this.tameListener = tameListener;
		this.breedListener = breedListener;
		this.blockPlaceListener = blockPlaceListener;
		this.blockBreakListener = blockBreakListener;
		this.killListener = killListener;
		this.craftListener = craftListener;
		this.playerCommandListener = playerCommandListener;
		this.teleportRespawnListener = teleportRespawnListener;
		this.petMasterGiveReceiveListener = petMasterGiveReceiveListener;
		this.quitListener = quitListener;
		this.listGUIListener = listGUIListener;
		this.fireworkListener = fireworkListener;
		this.playerAdvancedAchievementListener = playerAdvancedAchievementListener;
		this.achievementPlaceholderHook = achievementPlaceholderHook;
		this.achievementCountBungeeTabListPlusVariable = achievementCountBungeeTabListPlusVariable;
		this.databaseManager = databaseManager;
		this.asyncCachedRequestsSender = asyncCachedRequestsSender;
		this.pluginCommandExecutor = pluginCommandExecutor;
		this.commandTabCompleter = commandTabCompleter;
		this.disabledCategorySet = disabledCategorySet;
		this.mainConfig = mainConfig;
		this.configurationParser = configurationParser;
		this.distanceRunnable = distanceRunnable;
		this.playTimeRunnable = playTimeRunnable;
		this.updateChecker = updateChecker;
		this.reloadCommand = reloadCommand;
	}

	/**
	 * Loads the plugin.
	 * 
	 * @param firstLoad
	 * @throws PluginLoadError
	 */
	public void loadAdvancedAchievements(boolean firstLoad) throws PluginLoadError {
		configurationParser.loadAndParseConfiguration();
		registerListeners();
		if (firstLoad) {
			databaseManager.initialise();
			initialiseCommands();
		}
		launchScheduledTasks();
		launchUpdateChecker();
		registerPermissions();
		reloadCommand.notifyObservers();
		if (firstLoad) {
			linkPlaceholders();
		}
	}

	/**
	 * Disables the plugin.
	 */
	public void disableAdvancedAchievements() {
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

		registerListener(blockPlaceListener, MultipleAchievements.PLACES);
		registerListener(blockBreakListener, MultipleAchievements.BREAKS);
		registerListener(killListener, MultipleAchievements.KILLS);
		registerListener(craftListener, MultipleAchievements.CRAFTS);
		registerListener(playerCommandListener, MultipleAchievements.PLAYERCOMMANDS);
		registerListener(breedListener, MultipleAchievements.BREEDING);
		registerListener(deathListener, NormalAchievements.DEATHS);
		registerListener(arrowListener, NormalAchievements.ARROWS);
		registerListener(snowballEggListener, NormalAchievements.SNOWBALLS, NormalAchievements.EGGS);
		registerListener(fishListener, NormalAchievements.FISH, NormalAchievements.TREASURES);
		registerListener(itemBreakListener, NormalAchievements.ITEMBREAKS);
		registerListener(consumeListener, NormalAchievements.CONSUMEDPOTIONS, NormalAchievements.EATENITEMS);
		registerListener(shearListener, NormalAchievements.SHEARS);
		registerListener(milkLavaWaterListener, NormalAchievements.MILKS, NormalAchievements.LAVABUCKETS,
				NormalAchievements.WATERBUCKETS);
		registerListener(tradeAnvilBrewSmeltListener, NormalAchievements.TRADES, NormalAchievements.ANVILS,
				NormalAchievements.BREWING, NormalAchievements.SMELTING);
		registerListener(enchantmentListener, NormalAchievements.ENCHANTMENTS);
		registerListener(xpListener, NormalAchievements.LEVELS);
		registerListener(bedListener, NormalAchievements.BEDS);
		registerListener(dropListener, NormalAchievements.DROPS);
		registerListener(pickupListener, NormalAchievements.PICKUPS);
		registerListener(tameListener, NormalAchievements.TAMES);
		registerListener(hoeFertiliseFireworkMusicListener, NormalAchievements.HOEPLOWING, NormalAchievements.FERTILISING,
				NormalAchievements.FIREWORKS, NormalAchievements.MUSICDISCS);
		registerListener(teleportRespawnListener, NormalAchievements.DISTANCEFOOT, NormalAchievements.DISTANCEPIG,
				NormalAchievements.DISTANCEHORSE, NormalAchievements.DISTANCEMINECART, NormalAchievements.DISTANCEBOAT,
				NormalAchievements.DISTANCEGLIDING, NormalAchievements.DISTANCELLAMA, NormalAchievements.ENDERPEARLS);
		registerListener(petMasterGiveReceiveListener, NormalAchievements.PETMASTERGIVE,
				NormalAchievements.PETMASTERRECEIVE);
		registerListener(connectionListener);
		registerListener(quitListener);
		registerListener(listGUIListener);
		registerListener(fireworkListener);
		registerListener(playerAdvancedAchievementListener);
	}

	/**
	 * Registers a listener class, unless all matchingCatgories are disabled.
	 * 
	 * @param listener
	 * @param matchingCategories
	 */
	private void registerListener(Listener listener, Category... matchingCategories) {
		HandlerList.unregisterAll(listener);
		Set<String> matchingCategoryStrings = Arrays.stream(matchingCategories).map(Category::toString)
				.collect(Collectors.toSet());
		if (matchingCategoryStrings.isEmpty() || !disabledCategorySet.containsAll(matchingCategoryStrings)) {
			advancedAchievements.getServer().getPluginManager().registerEvents(listener, advancedAchievements);
		}
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
			int configPooledRequestsTaskInterval = mainConfig.getInt("PooledRequestsTaskInterval", 10);
			asyncCachedRequestsSenderTask = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(
					advancedAchievements, asyncCachedRequestsSender, configPooledRequestsTaskInterval * 40L,
					configPooledRequestsTaskInterval * 20L);
		}

		// Schedule a repeating task to monitor played time for each player (not directly related to an event).
		if (playedTimeTask != null) {
			playedTimeTask.cancel();
		}
		if (!disabledCategorySet.contains(NormalAchievements.PLAYEDTIME.toString())) {
			int configPlaytimeTaskInterval = mainConfig.getInt("PlaytimeTaskInterval", 60);
			playedTimeTask = Bukkit.getServer().getScheduler().runTaskTimer(advancedAchievements, playTimeRunnable,
					configPlaytimeTaskInterval * 10L, configPlaytimeTaskInterval * 20L);
		}

		// Schedule a repeating task to monitor distances travelled by each player (not directly related to an event).
		if (distanceTask != null) {
			distanceTask.cancel();
		}
		if (!disabledCategorySet.contains(NormalAchievements.DISTANCEFOOT.toString())
				|| !disabledCategorySet.contains(NormalAchievements.DISTANCEPIG.toString())
				|| !disabledCategorySet.contains(NormalAchievements.DISTANCEHORSE.toString())
				|| !disabledCategorySet.contains(NormalAchievements.DISTANCEMINECART.toString())
				|| !disabledCategorySet.contains(NormalAchievements.DISTANCEBOAT.toString())
				|| !disabledCategorySet.contains(NormalAchievements.DISTANCEGLIDING.toString())
				|| !disabledCategorySet.contains(NormalAchievements.DISTANCELLAMA.toString())) {
			int configDistanceTaskInterval = mainConfig.getInt("DistanceTaskInterval", 5);
			distanceTask = Bukkit.getServer().getScheduler().runTaskTimer(advancedAchievements, distanceRunnable,
					configDistanceTaskInterval * 40L, configDistanceTaskInterval * 20L);
		}
	}

	/**
	 * Launches an update check task. If updateChecker already registered (i.e. reload), does not check for update
	 * again. If CheckForUpdate switched to false unregisters listener.
	 */
	private void launchUpdateChecker() {
		if (!mainConfig.getBoolean("CheckForUpdate", true)) {
			PlayerJoinEvent.getHandlerList().unregister(updateChecker);
		} else {
			for (RegisteredListener registeredListener : PlayerJoinEvent.getHandlerList().getRegisteredListeners()) {
				if (registeredListener.getListener() == updateChecker) {
					return;
				}
			}
			advancedAchievements.getServer().getPluginManager().registerEvents(updateChecker, advancedAchievements);
			updateChecker.launchUpdateCheckerTask();
		}

	}

	/**
	 * Registers permissions that depend on the user's configuration file (for MultipleAchievements; for instance for
	 * stone breaks, achievement.count.breaks.stone will be registered).
	 */
	private void registerPermissions() {
		logger.info("Registering permissions...");

		PluginManager pluginManager = Bukkit.getServer().getPluginManager();
		for (MultipleAchievements category : MultipleAchievements.values()) {
			for (String section : mainConfig.getShallowKeys(category.toString())) {
				int startOfMetadata = section.indexOf(':');
				if (startOfMetadata > -1) {
					// Permission ignores metadata (eg. sand:1) for Breaks, Places and Crafts categories.
					section = section.substring(0, startOfMetadata);
				}
				if (category == MultipleAchievements.PLAYERCOMMANDS) {
					// Permissions don't take spaces into account for this category.
					section = StringUtils.deleteWhitespace(section);
				}

				// Bukkit only allows permissions to be set once, check to ensure they were not previously set when
				// performing /aach reload.
				if (pluginManager.getPermission(category.toPermName() + "." + section) == null) {
					pluginManager
							.addPermission(new Permission(category.toPermName() + "." + section, PermissionDefault.TRUE));
				}
			}
		}
	}

	/**
	 * Links third-party placeholder plugins (PlaceholderAPI and BungeeTabListPlus currently supported).
	 */
	private void linkPlaceholders() {
		if (Bukkit.getPluginManager().isPluginEnabled("BungeeTabListPlus")) {
			BungeeTabListPlusBukkitAPI.registerVariable(advancedAchievements,
					achievementCountBungeeTabListPlusVariable.get());
		}

		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			achievementPlaceholderHook.get().hook();
		}
	}
}
