package com.hm.achievement.module;

import com.hm.achievement.db.AchievementCache;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class CacheModule {

	@Provides
	@Singleton
	public AchievementCache providesAchievementCache(@Named("main") CommentedYamlConfiguration mainConfig) {
		return new AchievementCache(mainConfig);
	}
}
