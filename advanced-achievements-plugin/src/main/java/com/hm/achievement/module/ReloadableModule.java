package com.hm.achievement.module;

import java.util.Set;

import com.hm.achievement.advancement.AdvancementManager;
import com.hm.achievement.command.executable.AbstractCommand;
import com.hm.achievement.command.executor.PluginCommandExecutor;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.gui.CategoryGUI;
import com.hm.achievement.gui.MainGUI;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.achievement.listener.PlayerAdvancedAchievementListener;
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
import com.hm.achievement.runnable.AchieveDistanceRunnable;
import com.hm.achievement.runnable.AchievePlayTimeRunnable;
import com.hm.achievement.utils.RewardParser;
import com.hm.achievement.utils.StatisticIncreaseHandler;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoSet;

@Module
public interface ReloadableModule {

	@Binds
	@ElementsIntoSet
	abstract Set<Reloadable> bindCommands(Set<AbstractCommand> commands);

	@Binds
	@IntoSet
	abstract Reloadable bindAbstractDatabaseManager(AbstractDatabaseManager abstractDatabaseManager);

	@Binds
	@IntoSet
	abstract Reloadable bindCategoryGUI(CategoryGUI categoryGUI);

	@Binds
	@IntoSet
	abstract Reloadable bindMainGUI(MainGUI mainGUI);

	@Binds
	@IntoSet
	abstract Reloadable bindAdvancementManager(AdvancementManager advancementManager);

	@Binds
	@IntoSet
	abstract Reloadable bindPlayerAdvancedAchievementListener(
			PlayerAdvancedAchievementListener playerAdvancedAchievementListener);

	@Binds
	@IntoSet
	abstract Reloadable bindRewardParser(RewardParser rewardParser);

	@Binds
	@IntoSet
	abstract Reloadable bindStatisticIncreaseHandler(StatisticIncreaseHandler statisticIncreaseHandler);

	@Binds
	@IntoSet
	abstract Reloadable bindArrowsListener(ArrowsListener arrowsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindBedsListener(BedsListener bedsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindBreaksListener(BreaksListener breaksListener);

	@Binds
	@IntoSet
	abstract Reloadable bindBreedingListener(BreedingListener breedingListener);

	@Binds
	@IntoSet
	abstract Reloadable bindCaughtFishTreasuresListener(CaughtFishTreasuresListener caughtFishTreasuresListener);

	@Binds
	@IntoSet
	abstract Reloadable bindConnectionsListener(ConnectionsListener connectionsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindConsumedPotionsEatenItemsListener(
			ConsumedPotionsEatenItemsListener consumedPotionsEatenItemsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindCraftsListener(CraftsListener craftsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindDeathsListener(DeathsListener deathsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindDropsListener(DropsListener dropsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindEnchantmentsListener(EnchantmentsListener enchantmentsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindEnderPearlsDistancesListener(EnderPearlsDistancesListener enderPearlsDistancesListener);

	@Binds
	@IntoSet
	abstract Reloadable bindItemBreaksListener(ItemBreaksListener itemBreaksListener);

	@Binds
	@IntoSet
	abstract Reloadable bindKillsListener(KillsListener killsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindLevelsListener(LevelsListener levelsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindMilksLavaWaterBucketsListener(MilksLavaWaterBucketsListener milksLavaWaterBucketsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindPetMasterGiveReceiveListener(PetMasterGiveReceiveListener petMasterGiveReceiveListener);

	@Binds
	@IntoSet
	abstract Reloadable bindPickupsListener(PickupsListener pickupsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindPlacesListener(PlacesListener placesListener);

	@Binds
	@IntoSet
	abstract Reloadable bindPlowingFertilisingFireworksMusicDiscsListener(
			PlowingFertilisingFireworksMusicDiscsListener plowingFertilisingFireworksMusicDiscsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindPlayerCommandsListener(PlayerCommandsListener playerCommandsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindShearsListener(ShearsListener shearsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindSnowballsEggsListener(SnowballsEggsListener snowballsEggsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindTamesListener(TamesListener tamesListener);

	@Binds
	@IntoSet
	abstract Reloadable bindTradesAnvilsBrewingSmeltingListener(
			TradesAnvilsBrewingSmeltingListener tradesAnvilsBrewingSmeltingListener);

	@Binds
	@IntoSet
	abstract Reloadable bindAchieveDistanceRunnable(AchieveDistanceRunnable achieveDistanceRunnable);

	@Binds
	@IntoSet
	abstract Reloadable bindAchievePlayTimeRunnable(AchievePlayTimeRunnable achievePlayTimeRunnable);

	@Binds
	@IntoSet
	abstract Reloadable bindPluginCommandExecutor(PluginCommandExecutor pluginCommandExecutor);
}
