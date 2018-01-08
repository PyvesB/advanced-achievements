package com.hm.achievement.command;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.lang.CmdLang;
import com.hm.achievement.lang.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

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

		langToggleDisplayed = Lang.getWithChatHeader(CmdLang.TOGGLE_DISPLAYED, plugin);
		langToggleHidden = Lang.getWithChatHeader(CmdLang.TOGGLE_HIDDEN, plugin);
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
