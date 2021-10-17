package com.hm.achievement.module;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;

import com.hm.achievement.category.Category;

import dagger.Module;
import dagger.Provides;

@Module
public class ConfigModule {

	@Provides
	@Singleton
	Set<Category> provideDisabledCategories() {
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
		return new YamlConfiguration();
	}

	@Provides
	@Singleton
	@Named("lang")
	YamlConfiguration providesLangConfig() {
		return new YamlConfiguration();
	}

	@Provides
	@Singleton
	@Named("gui")
	YamlConfiguration providesGuiConfig() {
		return new YamlConfiguration();
	}

}
