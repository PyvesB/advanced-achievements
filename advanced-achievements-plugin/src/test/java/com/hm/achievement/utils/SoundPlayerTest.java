package com.hm.achievement.utils;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SoundPlayerTest {

	@Mock
	private Logger logger;

	@Mock
	private Player player;

	private Location location;

	@Before
	public void setUp() {
		location = new Location(null, 0, 0, 0);
		when(player.getLocation()).thenReturn(location);
	}

	@Test
	public void shouldUseProvidedSoundIfValid() {
		SoundPlayer underTest = new SoundPlayer(logger, 13);

		underTest.play(player, "ENTITY_FIREWORK_ROCKET_BLAST", "SOME_FALLBACK", "SOME_FALLBACK",
				"SOME_FALLBACK");

		verify(player).playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 0.7f);
	}

	@Test
	public void shouldUseFallbackSoundIfProvidedInvalid() {
		SoundPlayer underTest = new SoundPlayer(logger, 13);

		underTest.play(player, "INVALID", "ENTITY_FIREWORK_ROCKET_BLAST", "SOME_FALLBACK",
				"SOME_FALLBACK");

		verify(player).playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 0.7f);
	}

	@Test
	public void shouldUseCurrentSoundWhenUsingMinecraft13() {
		SoundPlayer underTest = new SoundPlayer(logger, 13);

		underTest.play(player, "ENTITY_FIREWORK_ROCKET_BLAST", "SOME_FALLBACK", "SOME_FALLBACK");

		verify(player).playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 0.7f);
	}

	@Test
	public void shouldUsePre13SoundWhenUsingMinecraft12() {
		SoundPlayer underTest = new SoundPlayer(logger, 12);

		underTest.play(player, "SOME_FALLBACK", "ENTITY_FIREWORK_ROCKET_BLAST", "SOME_FALLBACK");

		verify(player).playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 0.7f);
	}

	@Test
	public void shouldUsePre9SoundWhenUsingMinecraft8() {
		SoundPlayer underTest = new SoundPlayer(logger, 8);

		underTest.play(player, "SOME_FALLBACK", "SOME_FALLBACK", "ENTITY_FIREWORK_ROCKET_BLAST");

		verify(player).playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 0.7f);
	}

}
