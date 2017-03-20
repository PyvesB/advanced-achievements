package com.hm.achievement.command;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class in charge of handling the /aach toggle command, which allows a player to override the default behaviour of the
 * ChatNotify parameter.
 * 
 * @author Pyves
 */
public class ToggleCommand extends AbstractCommand {

	private final Set<String> toggledPlayers;

	public ToggleCommand(AdvancedAchievements plugin) {
		super(plugin);
		toggledPlayers = new HashSet<>();
	}

	/**
	 * Indicates whether player has used the /aach toggle command since the last server restart/plugin reload.
	 * 
	 * @param player
	 */
	public boolean isPlayerToggled(Player player) {
		return toggledPlayers.contains(player.getUniqueId().toString());
	}

	@Override
	protected void executeCommand(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;
		String uuid = player.getUniqueId().toString();
		if (toggledPlayers.contains(uuid)) {
			toggledPlayers.remove(uuid);
			displayChatMessage(player, plugin.isChatNotify());
		} else {
			toggledPlayers.add(uuid);
			displayChatMessage(player, !plugin.isChatNotify());
		}
	}

	private void displayChatMessage(Player player, boolean notifications) {
		if (notifications) {
			player.sendMessage(plugin.getChatHeader() + plugin.getPluginLang().getString("toggle-displayed",
					"You will now be notified when other players get achievements."));
		} else {
			player.sendMessage(plugin.getChatHeader() + plugin.getPluginLang().getString("toggle-hidden",
					"You will no longer be notified when other players get achievements."));
		}
	}
}
