package com.hm.achievement.module;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.Category;
import com.hm.mcshared.file.CommentedYamlConfiguration;

import dagger.Module;
import dagger.Provides;

@Module
public class ConfigModule {

	@Provides
	@Singleton
	Map<String, List<Long>> provideSortedThresholds() {
		return new HashMap<>();
	}

	@Provides
	@Singleton
	@Named("ntd")
	Map<String, String> provideNamesToDisplayNames() {
		return new HashMap<>();
	}

	@Provides
	@Singleton
	@Named("dtn")
	Map<String, String> provideDisplayNamesToNames() {
		return new HashMap<>();
	}

	@Provides
	@Singleton
	Set<Category> provideDisabledCategories() {
		return new HashSet<>();
	}

	@Provides
	@Singleton
	Set<String> provideEnabledCategoriesWithSubcategories() {
		return new HashSet<>();
	}

	@Provides
	@Singleton
	StringBuilder providePluginHeader() {
		return new StringBuilder();
	}

	@Provides
	@Singleton
	@Named("main")
	CommentedYamlConfiguration providesMainConfig(AdvancedAchievements advancedAchievements) {
		return new CommentedYamlConfiguration("config.yml", advancedAchievements);
	}

	@Provides
	@Singleton
	@Named("lang")
	CommentedYamlConfiguration providesLangConfig(AdvancedAchievements advancedAchievements) {
		String languageFileName = advancedAchievements.getConfig().getString("LanguageFileName", "lang.yml");
		return new CommentedYamlConfiguration(languageFileName, advancedAchievements);
	}

	@Provides
	@Singleton
	@Named("gui")
	CommentedYamlConfiguration providesGuiConfig(AdvancedAchievements advancedAchievements, int serverVersion) {
		String pluginResourceName = serverVersion < 13 ? "gui-legacy.yml" : "gui.yml";
		return new CommentedYamlConfiguration("gui.yml", pluginResourceName, advancedAchievements);
	}

}
