package com.hm.achievement.module;

import java.util.Set;

import com.hm.achievement.advancement.AdvancementManager;
import com.hm.achievement.command.executable.AbstractCommand;
import com.hm.achievement.command.executor.PluginCommandExecutor;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.gui.CategoryGUI;
import com.hm.achievement.gui.GUIItems;
import com.hm.achievement.gui.MainGUI;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.achievement.listener.PlayerAdvancedAchievementListener;
import com.hm.achievement.listener.statistics.AdvancementsCompletedListener;
import com.hm.achievement.listener.statistics.AnvilsListener;
import com.hm.achievement.listener.statistics.ArrowsListener;
import com.hm.achievement.listener.statistics.BedsListener;
import com.hm.achievement.listener.statistics.RiptidesListener;
import com.hm.achievement.listener.statistics.BreaksListener;
import com.hm.achievement.listener.statistics.BreedingListener;
import com.hm.achievement.listener.statistics.BrewingListener;
import com.hm.achievement.listener.statistics.ConnectionsListener;
import com.hm.achievement.listener.statistics.ConsumedPotionsListener;
import com.hm.achievement.listener.statistics.CraftsListener;
import com.hm.achievement.listener.statistics.DeathsListener;
import com.hm.achievement.listener.statistics.DropsListener;
import com.hm.achievement.listener.statistics.EatenItemsListener;
import com.hm.achievement.listener.statistics.EggsListener;
import com.hm.achievement.listener.statistics.EnchantmentsListener;
import com.hm.achievement.listener.statistics.EnderPearlsListener;
import com.hm.achievement.listener.statistics.FertilisingLegacyListener;
import com.hm.achievement.listener.statistics.FertilisingListener;
import com.hm.achievement.listener.statistics.FireworksListener;
import com.hm.achievement.listener.statistics.FishListener;
import com.hm.achievement.listener.statistics.HoePlowingListener;
import com.hm.achievement.listener.statistics.ItemBreaksListener;
import com.hm.achievement.listener.statistics.KillsListener;
import com.hm.achievement.listener.statistics.LavaBucketsListener;
import com.hm.achievement.listener.statistics.LevelsListener;
import com.hm.achievement.listener.statistics.MilksListener;
import com.hm.achievement.listener.statistics.MusicDiscsListener;
import com.hm.achievement.listener.statistics.PetMasterGiveListener;
import com.hm.achievement.listener.statistics.PetMasterReceiveListener;
import com.hm.achievement.listener.statistics.PickupsListener;
import com.hm.achievement.listener.statistics.PlacesListener;
import com.hm.achievement.listener.statistics.PlayerCommandsListener;
import com.hm.achievement.listener.statistics.ShearsListener;
import com.hm.achievement.listener.statistics.SmeltingListener;
import com.hm.achievement.listener.statistics.SnowballsListener;
import com.hm.achievement.listener.statistics.TamesListener;
import com.hm.achievement.listener.statistics.TargetsShotListener;
import com.hm.achievement.listener.statistics.TradesListener;
import com.hm.achievement.listener.statistics.TreasuresListener;
import com.hm.achievement.listener.statistics.WaterBucketsListener;
import com.hm.achievement.listener.statistics.WinRaidListener;
import com.hm.achievement.runnable.AchieveDistanceRunnable;
import com.hm.achievement.runnable.AchievePlayTimeRunnable;
import com.hm.achievement.utils.RewardParser;
import com.hm.achievement.utils.StatisticIncreaseHandler;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoSet;

@Module
public abstract class ReloadableModule {

	@Provides
	@IntoSet
	static Reloadable provideFertilisingListener(FertilisingListener fertilisingListener,
			FertilisingLegacyListener fertilisingLegacyListener, int serverVersion) {
		return serverVersion >= 13 ? fertilisingListener : fertilisingLegacyListener;
	}

	@Binds
	@ElementsIntoSet
	abstract Set<Reloadable> bindCommands(Set<AbstractCommand> commands);

	@Binds
	@IntoSet
	abstract Reloadable bindAbstractDatabaseManager(AbstractDatabaseManager abstractDatabaseManager);

	@Binds
	@IntoSet
	abstract Reloadable bindAchieveDistanceRunnable(AchieveDistanceRunnable achieveDistanceRunnable);

	@Binds
	@IntoSet
	abstract Reloadable bindAchievePlayTimeRunnable(AchievePlayTimeRunnable achievePlayTimeRunnable);

	@Binds
	@IntoSet
	abstract Reloadable bindAdvancementManager(AdvancementManager advancementManager);

	@Binds
	@IntoSet
	abstract Reloadable bindAnvilsListener(AnvilsListener anvilsListener);

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
	abstract Reloadable bindBrewingListener(BrewingListener brewingListener);

	@Binds
	@IntoSet
	abstract Reloadable bindCategoryGUI(CategoryGUI categoryGUI);

	@Binds
	@IntoSet
	abstract Reloadable bindConnectionsListener(ConnectionsListener connectionsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindConsumedPotionsListener(ConsumedPotionsListener consumedPotionsListener);

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
	abstract Reloadable bindEatenItemsListener(EatenItemsListener eatenItemsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindEggsListener(EggsListener eggsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindEnchantmentsListener(EnchantmentsListener enchantmentsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindEnderPearlsListener(EnderPearlsListener enderPearlsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindFireworksListener(FireworksListener fireworksListener);

	@Binds
	@IntoSet
	abstract Reloadable bindFishListener(FishListener fishListener);

	@Binds
	@IntoSet
	abstract Reloadable bindGUIItems(GUIItems guiItems);

	@Binds
	@IntoSet
	abstract Reloadable bindHoePlowingListener(HoePlowingListener hoePlowingListener);

	@Binds
	@IntoSet
	abstract Reloadable bindItemBreaksListener(ItemBreaksListener itemBreaksListener);

	@Binds
	@IntoSet
	abstract Reloadable bindKillsListener(KillsListener killsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindLavaBucketsListener(LavaBucketsListener lavaBucketsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindLevelsListener(LevelsListener levelsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindMainGUI(MainGUI mainGUI);

	@Binds
	@IntoSet
	abstract Reloadable bindMilksListener(MilksListener milksListener);

	@Binds
	@IntoSet
	abstract Reloadable bindMusicDiscsListener(MusicDiscsListener musicDiscsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindPetMasterGiveListener(PetMasterReceiveListener petMasterReceiveListener);

	@Binds
	@IntoSet
	abstract Reloadable bindPetMasterReceiveListener(PetMasterGiveListener petMasterGiveListener);

	@Binds
	@IntoSet
	abstract Reloadable bindPickupsListener(PickupsListener pickupsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindPlacesListener(PlacesListener placesListener);

	@Binds
	@IntoSet
	abstract Reloadable bindPlayerAdvancedAchievementListener(
			PlayerAdvancedAchievementListener playerAdvancedAchievementListener);

	@Binds
	@IntoSet
	abstract Reloadable bindPlayerCommandsListener(PlayerCommandsListener playerCommandsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindPluginCommandExecutor(PluginCommandExecutor pluginCommandExecutor);

	@Binds
	@IntoSet
	abstract Reloadable bindRewardParser(RewardParser rewardParser);

	@Binds
	@IntoSet
	abstract Reloadable bindShearsListener(ShearsListener shearsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindSmeltingListener(SmeltingListener smeltingListener);

	@Binds
	@IntoSet
	abstract Reloadable bindSnowballsListener(SnowballsListener snowballsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindStatisticIncreaseHandler(StatisticIncreaseHandler statisticIncreaseHandler);

	@Binds
	@IntoSet
	abstract Reloadable bindTamesListener(TamesListener tamesListener);

	@Binds
	@IntoSet
	abstract Reloadable bindTargetsShotListener(TargetsShotListener targetsShotListener);

	@Binds
	@IntoSet
	abstract Reloadable bindTradesListener(TradesListener tradesListener);

	@Binds
	@IntoSet
	abstract Reloadable bindTreasuresListener(TreasuresListener treasuresListener);

	@Binds
	@IntoSet
	abstract Reloadable bindWaterBucketsListener(WaterBucketsListener waterBucketsListener);

	@Binds
	@IntoSet
	abstract Reloadable bindsWinRaidListener(WinRaidListener winRaidListener);

	@Binds
	@IntoSet
	abstract Reloadable bindsRiptidesListener(RiptidesListener riptidesListener);

	@Binds
	@IntoSet
	abstract Reloadable bindAdvancementsCompletedListener(AdvancementsCompletedListener advancementsCompletedListener);
}
