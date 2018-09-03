package com.hm.achievement.module;

import javax.inject.Singleton;

import com.hm.achievement.AdvancedAchievements;
import com.hm.mcshared.update.UpdateChecker;

import dagger.Module;
import dagger.Provides;

@Module
public class UpdateCheckerModule {

	@Provides
	@Singleton
	UpdateChecker provideUpdateChecker(AdvancedAchievements advancedAchievements, StringBuilder pluginHeader) {
		return new UpdateChecker(advancedAchievements,
				"https://raw.githubusercontent.com/PyvesB/AdvancedAchievements/master/pom.xml", "achievement.update",
				pluginHeader.toString(), "spigotmc.org/resources/advanced-achievements.6239");

	}
}
