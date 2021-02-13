package com.hm.achievement.module;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.darkblade12.particleeffect.ReflectionUtils.PackageType;

import dagger.Module;
import dagger.Provides;

@Module
public class ServerVersionModule {

	@Provides
	@Singleton
	int provideServerVersion() {
		// Simple parsing of game version. Might need to be updated in the future depending on how the Minecraft
		// versions change in the future.
		return Integer.parseInt(StringUtils.substringBetween(PackageType.getServerVersion(), "_"));
	}

}
