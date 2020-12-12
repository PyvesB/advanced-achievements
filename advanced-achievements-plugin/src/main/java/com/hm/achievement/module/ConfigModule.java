package com.hm.achievement.module;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;

import com.hm.achievement.category.Category;
import com.hm.achievement.config.ReloadableYamlConfiguration;

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
	YamlConfiguration providesMainConfig() {
		return new ReloadableYamlConfiguration();
	}

	@Provides
	@Singleton
	@Named("lang")
	YamlConfiguration providesLangConfig() {
		return new ReloadableYamlConfiguration();
	}

	@Provides
	@Singleton
	@Named("gui")
	YamlConfiguration providesGuiConfig() {
		return new ReloadableYamlConfiguration();
	}

}
