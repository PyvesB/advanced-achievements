package com.hm.achievement.utils;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Class in charge of player sounds when players successfully complete some actions.
 *
 * @author Pyves
 */
@Singleton
public class SoundPlayer {

	private final Logger logger;

	@Inject
	public SoundPlayer(Logger logger) {
		this.logger = logger;
	}

	/**
	 * Plays a sound provided via configuration. If the sound is invalid, this method falls back to the provided
	 * fallback.
	 * 
	 * @param player
	 * @param providedSound
	 * @param fallbackSound
	 */
	public void play(Player player, String providedSound, String fallbackSound) {
		if (EnumUtils.isValidEnum(Sound.class, providedSound)) {
			player.playSound(player.getLocation(), Sound.valueOf(providedSound), 1, 0.7f);
		} else {
			player.playSound(player.getLocation(), Sound.valueOf(fallbackSound), 1, 0.7f);
			logger.warning("Sound " + providedSound + " is invalid, using default instead.");
		}
	}

}
