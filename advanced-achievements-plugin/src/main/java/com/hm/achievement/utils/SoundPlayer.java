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
	private final int serverVersion;

	@Inject
	public SoundPlayer(Logger logger, int serverVersion) {
		this.logger = logger;
		this.serverVersion = serverVersion;
	}

	/**
	 * Plays a sound provided via configuration. If the sound is invalid, this method falls back to one of the other
	 * values provided, dependent on the Minecraft version.
	 * 
	 * @param player
	 * @param providedSound
	 * @param fallbackSound
	 * @param pre13FallbackSound
	 * @param pre9FallbackSound
	 */
	public void play(Player player, String providedSound, String fallbackSound, String pre13FallbackSound,
			String pre9FallbackSound) {
		if (EnumUtils.isValidEnum(Sound.class, providedSound)) {
			player.playSound(player.getLocation(), Sound.valueOf(providedSound), 1, 0.7f);
		} else {
			play(player, fallbackSound, pre13FallbackSound, pre9FallbackSound);
			logger.warning("Sound " + providedSound + " is invalid, using default instead.");
		}

	}

	/**
	 * Plays a sound dependent on the Minecraft version.
	 * 
	 * @param player
	 * @param currentSound
	 * @param pre13Sound
	 * @param pre9Sound
	 */
	public void play(Player player, String currentSound, String pre13Sound, String pre9Sound) {
		String soundValue;
		if (serverVersion < 9) {
			soundValue = pre9Sound;
		} else if (serverVersion < 13) {
			soundValue = pre13Sound;
		} else {
			soundValue = currentSound;
		}
		player.playSound(player.getLocation(), Sound.valueOf(soundValue), 1, 0.7f);
	}

}
