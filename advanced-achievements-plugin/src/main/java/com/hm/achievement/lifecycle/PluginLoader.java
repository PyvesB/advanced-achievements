package com.hm.achievement.lifecycle;

import java.util.Arrays;
import java.util.Set;
import java.util.logging.Logger;

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
import com.hm.achievement.command.completer.CommandTabCompleter;
import com.hm.achievement.command.executable.ReloadCommand;
import com.hm.achievement.command.executor.PluginCommandExecutor;
import com.hm.achievement.config.ConfigurationParser;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.AsyncCachedRequestsSender;
import com.hm.achievement.exception.PluginLoadError;
import com.hm.achievement.listener.FireworkListener;
import com.hm.achievement.listener.ListGUIListener;
import com.hm.achievement.listener.PlayerAdvancedAchievementListener;
import com.hm.achievement.listener.QuitListener;
import com.hm.achievement.listener.statistics.ArrowsListener;
import com.hm.achievement.listener.statistics.BedsListener;
import com.hm.achievement.listener.statistics.BreaksListener;
import com.hm.achievement.listener.statistics.BreedingListener;
import com.hm.achievement.listener.statistics.CaughtFishTreasuresListener;
import com.hm.achievement.listener.statistics.ConnectionsListener;
import com.hm.achievement.listener.statistics.ConsumedPotionsEatenItemsListener;
import com.hm.achievement.listener.statistics.CraftsListener;
import com.hm.achievement.listener.statistics.DeathsListener;
import com.hm.achievement.listener.statistics.DropsListener;
import com.hm.achievement.listener.statistics.EnchantmentsListener;
import com.hm.achievement.listener.statistics.EnderPearlsDistancesListener;
import com.hm.achievement.listener.statistics.ItemBreaksListener;
import com.hm.achievement.listener.statistics.KillsListener;
import com.hm.achievement.listener.statistics.LevelsListener;
import com.hm.achievement.listener.statistics.MilksLavaWaterBucketsListener;
import com.hm.achievement.listener.statistics.PetMasterGiveReceiveListener;
import com.hm.achievement.listener.statistics.PickupsListener;
import com.hm.achievement.listener.statistics.PlacesListener;
import com.hm.achievement.listener.statistics.PlayerCommandsListener;
import com.hm.achievement.listener.statistics.PlowingFertilisingFireworksMusicDiscsListener;
import com.hm.achievement.listener.statistics.ShearsListener;
import com.hm.achievement.listener.statistics.SnowballsEggsListener;
import com.hm.achievement.listener.statistics.TamesListener;
import com.hm.achievement.listener.statistics.TradesAnvilsBrewingSmeltingListener;
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
	private final Lazy<UpdateChecker> updateChecker;
	private final ReloadCommand reloadCommand;

	// Listeners, to monitor events and manage stats.
	private final ConnectionsListener connectionsListener;
	private final DeathsListener deathsListener;
	private final ArrowsListener arrowsListener;
	private final SnowballsEggsListener snowballsEggsListener;
	private final CaughtFishTreasuresListener caughtFishTreasuresListener;
	private final ItemBreaksListener itemBreaksListener;
	private final ConsumedPotionsEatenItemsListener consumedPotionsEatenItemsListener;
	private final ShearsListener shearsListener;
	private final MilksLavaWaterBucketsListener milksLavaWaterBucketsListener;
	private final LevelsListener levelsListener;
	private final TradesAnvilsBrewingSmeltingListener tradesAnvilsBrewingSmeltingListener;
	private final BedsListener bedsListener;
	private final EnchantmentsListener enchantmentsListener;
	private final DropsListener dropsListener;
	private final PickupsListener pickupsListener;
	private final PlowingFertilisingFireworksMusicDiscsListener plowingFertilisingFireworksMusicDiscsListener;
	private final TamesListener tamesListener;
	private final BreedingListener breedingListener;
	private final PlacesListener placesListener;
	private final BreaksListener breaksListener;
	private final KillsListener killsListener;
	private final CraftsListener craftsListener;
	private final PlayerCommandsListener playerCommandsListener;
	private final EnderPearlsDistancesListener enderPearlsDistancesListener;
	private final PetMasterGiveReceiveListener petMasterGiveReceiveListener;
	private final FireworkListener fireworkListener;
	private final QuitListener quitListener;
	private final ListGUIListener listGUIListener;
	private final PlayerAdvancedAchievementListener playerAdvancedAchievementListener;

	// Integrations with other plugins. Use lazy injection as these may or may not be used depending on runtime
	// conditions.
	private final Lazy<AchievementPlaceholderHook> achievementPlaceholderHook;
	private final Lazy<AchievementCountBungeeTabListPlusVariable> achievementCountBungeeTabListPlusVariable;

	// Database related.
	private final AbstractDatabaseManager databaseManager;
	private final AsyncCachedRequestsSender asyncCachedRequestsSender;

	// Various other fields and parameters.
	private final PluginCommandExecutor pluginCommandExecutor;
	private final CommandTabCompleter commandTabCompleter;
	private final Set<Category> disabledCategories;
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
	public PluginLoader(AdvancedAchievements advancedAchievements, Logger logger, ConnectionsListener connectionsListener,
			DeathsListener deathsListener, ArrowsListener arrowsListener, SnowballsEggsListener snowballsEggsListener,
			CaughtFishTreasuresListener caughtFishTreasuresListener, ItemBreaksListener itemBreaksListener,
			ConsumedPotionsEatenItemsListener consumedPotionsEatenItemsListener, ShearsListener shearsListener,
			MilksLavaWaterBucketsListener milksLavaWaterBucketsListener, LevelsListener levelsListener,
			TradesAnvilsBrewingSmeltingListener tradesAnvilsBrewingSmeltingListener, BedsListener bedsListener,
			EnchantmentsListener enchantmentsListener, DropsListener dropsListener, PickupsListener pickupsListener,
			PlowingFertilisingFireworksMusicDiscsListener plowingFertilisingFireworksMusicDiscsListener,
			TamesListener tamesListener, BreedingListener breedingListener, PlacesListener placesListener,
			BreaksListener breaksListener, KillsListener killsListener, CraftsListener craftsListener,
			PlayerCommandsListener playerCommandsListener, EnderPearlsDistancesListener enderPearlsDistancesListener,
			PetMasterGiveReceiveListener petMasterGiveReceiveListener, FireworkListener fireworkListener,
			QuitListener quitListener, ListGUIListener listGUIListener,
			PlayerAdvancedAchievementListener playerAdvancedAchievementListener,
			Lazy<AchievementPlaceholderHook> achievementPlaceholderHook,
			Lazy<AchievementCountBungeeTabListPlusVariable> achievementCountBungeeTabListPlusVariable,
			AbstractDatabaseManager databaseManager, AsyncCachedRequestsSender asyncCachedRequestsSender,
			PluginCommandExecutor pluginCommandExecutor, CommandTabCompleter commandTabCompleter,
			Set<Category> disabledCategories, @Named("main") CommentedYamlConfiguration mainConfig,
			ConfigurationParser configurationParser, AchieveDistanceRunnable distanceRunnable,
			AchievePlayTimeRunnable playTimeRunnable, Lazy<UpdateChecker> updateChecker, ReloadCommand reloadCommand) {
		this.advancedAchievements = advancedAchievements;
		this.logger = logger;
		this.connectionsListener = connectionsListener;
		this.deathsListener = deathsListener;
		this.arrowsListener = arrowsListener;
		this.snowballsEggsListener = snowballsEggsListener;
		this.caughtFishTreasuresListener = caughtFishTreasuresListener;
		this.itemBreaksListener = itemBreaksListener;
		this.consumedPotionsEatenItemsListener = consumedPotionsEatenItemsListener;
		this.shearsListener = shearsListener;
		this.milksLavaWaterBucketsListener = milksLavaWaterBucketsListener;
		this.levelsListener = levelsListener;
		this.tradesAnvilsBrewingSmeltingListener = tradesAnvilsBrewingSmeltingListener;
		this.bedsListener = bedsListener;
		this.enchantmentsListener = enchantmentsListener;
		this.dropsListener = dropsListener;
		this.pickupsListener = pickupsListener;
		this.plowingFertilisingFireworksMusicDiscsListener = plowingFertilisingFireworksMusicDiscsListener;
		this.tamesListener = tamesListener;
		this.breedingListener = breedingListener;
		this.placesListener = placesListener;
		this.breaksListener = breaksListener;
		this.killsListener = killsListener;
		this.craftsListener = craftsListener;
		this.playerCommandsListener = playerCommandsListener;
		this.enderPearlsDistancesListener = enderPearlsDistancesListener;
		this.petMasterGiveReceiveListener = petMasterGiveReceiveListener;
		this.fireworkListener = fireworkListener;
		this.quitListener = quitListener;
		this.listGUIListener = listGUIListener;
		this.playerAdvancedAchievementListener = playerAdvancedAchievementListener;
		this.achievementPlaceholderHook = achievementPlaceholderHook;
		this.achievementCountBungeeTabListPlusVariable = achievementCountBungeeTabListPlusVariable;
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

		registerListener(placesListener, MultipleAchievements.PLACES);
		registerListener(breaksListener, MultipleAchievements.BREAKS);
		registerListener(killsListener, MultipleAchievements.KILLS);
		registerListener(craftsListener, MultipleAchievements.CRAFTS);
		registerListener(playerCommandsListener, MultipleAchievements.PLAYERCOMMANDS);
		registerListener(breedingListener, MultipleAchievements.BREEDING);
		registerListener(deathsListener, NormalAchievements.DEATHS);
		registerListener(arrowsListener, NormalAchievements.ARROWS);
		registerListener(snowballsEggsListener, NormalAchievements.SNOWBALLS, NormalAchievements.EGGS);
		registerListener(caughtFishTreasuresListener, NormalAchievements.FISH, NormalAchievements.TREASURES);
		registerListener(itemBreaksListener, NormalAchievements.ITEMBREAKS);
		registerListener(consumedPotionsEatenItemsListener, NormalAchievements.CONSUMEDPOTIONS,
				NormalAchievements.EATENITEMS);
		registerListener(shearsListener, NormalAchievements.SHEARS);
		registerListener(milksLavaWaterBucketsListener, NormalAchievements.MILKS, NormalAchievements.LAVABUCKETS,
				NormalAchievements.WATERBUCKETS);
		registerListener(tradesAnvilsBrewingSmeltingListener, NormalAchievements.TRADES, NormalAchievements.ANVILS,
				NormalAchievements.BREWING, NormalAchievements.SMELTING);
		registerListener(enchantmentsListener, NormalAchievements.ENCHANTMENTS);
		registerListener(levelsListener, NormalAchievements.LEVELS);
		registerListener(bedsListener, NormalAchievements.BEDS);
		registerListener(dropsListener, NormalAchievements.DROPS);
		registerListener(pickupsListener, NormalAchievements.PICKUPS);
		registerListener(tamesListener, NormalAchievements.TAMES);
		registerListener(plowingFertilisingFireworksMusicDiscsListener, NormalAchievements.HOEPLOWING,
				NormalAchievements.FERTILISING, NormalAchievements.FIREWORKS, NormalAchievements.MUSICDISCS);
		registerListener(enderPearlsDistancesListener, NormalAchievements.DISTANCEFOOT, NormalAchievements.DISTANCEPIG,
				NormalAchievements.DISTANCEHORSE, NormalAchievements.DISTANCEMINECART, NormalAchievements.DISTANCEBOAT,
				NormalAchievements.DISTANCEGLIDING, NormalAchievements.DISTANCELLAMA, NormalAchievements.ENDERPEARLS);
		registerListener(petMasterGiveReceiveListener, NormalAchievements.PETMASTERGIVE,
				NormalAchievements.PETMASTERRECEIVE);
		registerListener(connectionsListener);
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
		if (matchingCategories.length == 0 || !disabledCategories.containsAll(Arrays.asList(matchingCategories))) {
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
			asyncCachedRequestsSenderTask = Bukkit.getScheduler().runTaskTimerAsynchronously(advancedAchievements,
					asyncCachedRequestsSender, configPooledRequestsTaskInterval * 40L,
					configPooledRequestsTaskInterval * 20L);
		}

		// Schedule a repeating task to monitor played time for each player (not directly related to an event).
		if (playedTimeTask != null) {
			playedTimeTask.cancel();
		}
		if (!disabledCategories.contains(NormalAchievements.PLAYEDTIME)) {
			int configPlaytimeTaskInterval = mainConfig.getInt("PlaytimeTaskInterval", 60);
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
				|| !disabledCategories.contains(NormalAchievements.DISTANCELLAMA)) {
			int configDistanceTaskInterval = mainConfig.getInt("DistanceTaskInterval", 5);
			distanceTask = Bukkit.getScheduler().runTaskTimer(advancedAchievements, distanceRunnable,
					configDistanceTaskInterval * 40L, configDistanceTaskInterval * 20L);
		}
	}

	/**
	 * Launches an update check task. If updateChecker already registered (i.e. reload), does not check for update
	 * again. If CheckForUpdate switched to false unregisters listener.
	 */
	private void launchUpdateChecker() {
		if (!mainConfig.getBoolean("CheckForUpdate", true)) {
			PlayerJoinEvent.getHandlerList().unregister(updateChecker.get());
		} else {
			for (RegisteredListener registeredListener : PlayerJoinEvent.getHandlerList().getRegisteredListeners()) {
				if (registeredListener.getListener() == updateChecker) {
					return;
				}
			}
			advancedAchievements.getServer().getPluginManager().registerEvents(updateChecker.get(), advancedAchievements);
			updateChecker.get().launchUpdateCheckerTask();
		}

	}

	/**
	 * Registers permissions that depend on the user's configuration file (for MultipleAchievements; for instance for
	 * stone breaks, achievement.count.breaks.stone will be registered).
	 */
	private void registerPermissions() {
		logger.info("Registering permissions...");

		PluginManager pluginManager = Bukkit.getPluginManager();
		for (MultipleAchievements category : MultipleAchievements.values()) {
			for (String section : mainConfig.getShallowKeys(category.toString())) {
				// Permission ignores metadata (eg. sand:1) for Breaks, Places and Crafts categories and don't take
				// spaces into account.
				section = StringUtils.deleteWhitespace(StringUtils.substringBefore(section, ":"));

				// Bukkit only allows permissions to be set once, check to ensure they were not previously set when
				// performing /aach reload.
				for (String groupElement : StringUtils.split(section, '|')) {
					String permissionNode = category.toPermName() + "." + groupElement;
					if (pluginManager.getPermission(permissionNode) == null) {
						pluginManager.addPermission(new Permission(permissionNode, PermissionDefault.TRUE));
					}
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
			achievementPlaceholderHook.get().register();
		}
	}
}
