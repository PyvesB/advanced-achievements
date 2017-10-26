package com.hm.achievement.command;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class in charge of handling the /aach toggle command, which allows a player to override the default behaviour of the
 * NotifyOtherPlayers parameter.
 * 
 * @author Pyves
 */
public class ToggleCommand extends AbstractCommand {

	// Indicates whether a player has used toggle since last server restart.
	private final Set<String> toggledPlayers;

	private boolean configNotifyOtherPlayers;
	private String langToggleDisplayed;
	private String langToggleHidden;

	public ToggleCommand(AdvancedAchievements plugin) {
		super(plugin);

		toggledPlayers = new HashSet<>();
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configNotifyOtherPlayers = plugin.getPluginConfig().getBoolean("NotifyOtherPlayers", false);

		langToggleDisplayed = plugin.getChatHeader() + plugin.getPluginLang().getString("toggle-displayed",
				"You will now be notified when other players get achievements.");
		langToggleHidden = plugin.getChatHeader() + plugin.getPluginLang().getString("toggle-hidden",
				"You will no longer be notified when other players get achievements.");
	}

	/**
	 * Indicates whether player has used the /aach toggle command since the last server restart/plugin reload.
	 * 
	 * @param player
	 * @return true if player has used the toggle command, false otherwise
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
			displayChatMessage(player, configNotifyOtherPlayers);
		} else {
			toggledPlayers.add(uuid);
			displayChatMessage(player, !configNotifyOtherPlayers);
		}
	}

	private void displayChatMessage(Player player, boolean notifications) {
		if (notifications) {
			player.sendMessage(langToggleDisplayed);
		} else {
			player.sendMessage(langToggleHidden);
		}
	}
}
