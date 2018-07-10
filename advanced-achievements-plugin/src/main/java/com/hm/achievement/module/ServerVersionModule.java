package com.hm.achievement.module;

import javax.inject.Singleton;

import com.hm.mcshared.particle.ReflectionUtils.PackageType;

import dagger.Module;
import dagger.Provides;

@Module
public class ServerVersionModule {

	@Provides
	@Singleton
	int provideServerVersion() {
		// Simple parsing of game version. Might need to be updated in the future
		// depending on how the Minecraft
		// versions change in the future.
		return Integer.parseInt(PackageType.getServerVersion().split("_")[1]);
	}

}
