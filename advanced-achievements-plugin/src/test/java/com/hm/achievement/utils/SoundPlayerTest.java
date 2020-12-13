package com.hm.achievement.utils;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SoundPlayerTest {

	@Mock
	private Logger logger;

	@Mock
	private Player player;

	private Location location;

	@BeforeEach
	void setUp() {
		location = new Location(null, 0, 0, 0);
		when(player.getLocation()).thenReturn(location);
	}

	@Test
	void shouldUseProvidedSoundIfValid() {
		SoundPlayer underTest = new SoundPlayer(logger, 13);

		underTest.play(player, "ENTITY_FIREWORK_ROCKET_BLAST", "SOME_FALLBACK", "SOME_FALLBACK", "SOME_FALLBACK");

		verify(player).playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 0.7f);
	}

	@Test
	void shouldUseFallbackSoundIfProvidedInvalid() {
		SoundPlayer underTest = new SoundPlayer(logger, 13);

		underTest.play(player, "INVALID", "ENTITY_FIREWORK_ROCKET_BLAST", "SOME_FALLBACK", "SOME_FALLBACK");

		verify(player).playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 0.7f);
	}

	@Test
	void shouldUseCurrentSoundWhenUsingMinecraft13() {
		SoundPlayer underTest = new SoundPlayer(logger, 13);

		underTest.play(player, "ENTITY_FIREWORK_ROCKET_BLAST", "SOME_FALLBACK", "SOME_FALLBACK");

		verify(player).playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 0.7f);
	}

	@Test
	void shouldUsePre13SoundWhenUsingMinecraft12() {
		SoundPlayer underTest = new SoundPlayer(logger, 12);

		underTest.play(player, "SOME_FALLBACK", "ENTITY_FIREWORK_ROCKET_BLAST", "SOME_FALLBACK");

		verify(player).playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 0.7f);
	}

	@Test
	void shouldUsePre9SoundWhenUsingMinecraft8() {
		SoundPlayer underTest = new SoundPlayer(logger, 8);

		underTest.play(player, "SOME_FALLBACK", "SOME_FALLBACK", "ENTITY_FIREWORK_ROCKET_BLAST");

		verify(player).playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 0.7f);
	}

}
