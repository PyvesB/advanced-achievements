package com.hm.achievement.config;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;

import org.junit.jupiter.api.Test;

class ReloadableYamlConfigurationTest {

	@Test
	void shouldClearKeyInPreviouslyLoadedConfiguration() throws Exception {
		ReloadableYamlConfiguration underTest = new ReloadableYamlConfiguration();
		underTest.load(new File(getClass().getClassLoader().getResource("config-updated.yml").toURI()));
		underTest.load(new File(getClass().getClassLoader().getResource("config-missing-sections.yml").toURI()));

		assertFalse(underTest.contains("CheckForUpdate"));
		assertFalse(underTest.contains("book-date"));
	}

}
