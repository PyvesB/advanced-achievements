package com.hm.achievement.command;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.mcshared.particle.ReflectionUtils.PackageType;

/**
 * Abstract class in charge of factoring out common functionality for commands.
 * 
 * @author Pyves
 */
public abstract class AbstractCommand {

	protected final AdvancedAchievements plugin;
	protected final int version;

	protected AbstractCommand(AdvancedAchievements plugin) {
		this.plugin = plugin;
		// Simple parsing of game version. Might need to be updated in the future depending on how the Minecraft
		// versions change in the future.
		version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);
	}

	/**
	 * Executes the command issued by the sender if he has the relevant permissions. If permission null, skip check.
	 * 
	 * @param sender
	 * @param args
	 * @param permission
	 */
	public void executeCommand(CommandSender sender, String[] args, String permission) {
		if (permission != null && !sender.hasPermission("achievement." + permission)) {
			sender.sendMessage(plugin.getChatHeader()
					+ plugin.getPluginLang().getString("no-permissions", "You do not have the permission to do this."));
			return;
		}

		executeCommand(sender, args);
	}

	/**
	 * Executes the command issued by the sender.
	 * 
	 * @param sender
	 * @param args
	 */
	protected abstract void executeCommand(CommandSender sender, String[] args);

	/**
	 * Plays a firework sound.
	 * 
	 * @param player
	 */
	protected void playFireworkSound(Player player) {
		Sound sound;
		if (version < 9) {
			// Old enum for versions prior to Minecraft 1.9. Retrieving it by name as it does no longer exist in newer
			// versions.
			sound = Sound.valueOf("FIREWORK_BLAST");
		} else {
			// Play sound with enum for newer versions.
			sound = Sound.ENTITY_FIREWORK_LARGE_BLAST;
		}
		player.getWorld().playSound(player.getLocation(), sound, 1, 0.7f);
	}
}
