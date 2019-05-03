package com.hm.achievement.module;

import com.hm.achievement.command.executable.BookCommand;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.lifecycle.Cleanable;
import com.hm.achievement.listener.statistics.BedsListener;
import com.hm.achievement.listener.statistics.ConnectionsListener;
import com.hm.achievement.listener.statistics.MilksLavaWaterBucketsListener;
import com.hm.achievement.listener.statistics.PlowingFireworksMusicDiscsListener;
import com.hm.achievement.listener.statistics.TradesAnvilsBrewingSmeltingListener;
import com.hm.achievement.runnable.AchieveDistanceRunnable;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface CleanableModule {

	@Binds
	@IntoSet
	abstract Cleanable bindBedsListener(BedsListener bedsListener);

	@Binds
	@IntoSet
	abstract Cleanable bindMilksLavaWaterBucketsListener(MilksLavaWaterBucketsListener milksLavaWaterBucketsListener);

	@Binds
	@IntoSet
	abstract Cleanable bindPlowingFertilisingFireworksMusicDiscsListener(
			PlowingFireworksMusicDiscsListener plowingFireworksMusicDiscsListener);

	@Binds
	@IntoSet
	abstract Cleanable bindTradesAnvilsBrewingSmeltingListener(
			TradesAnvilsBrewingSmeltingListener tradesAnvilsBrewingSmeltingListener);

	@Binds
	@IntoSet
	abstract Cleanable bindConnectionsListener(ConnectionsListener connectionsListener);

	@Binds
	@IntoSet
	abstract Cleanable bindAchieveDistanceRunnable(AchieveDistanceRunnable achieveDistanceRunnable);

	@Binds
	@IntoSet
	abstract Cleanable bindBookCommand(BookCommand bookCommand);

	@Binds
	@IntoSet
	abstract Cleanable bindCacheManager(CacheManager cacheManager);

}
