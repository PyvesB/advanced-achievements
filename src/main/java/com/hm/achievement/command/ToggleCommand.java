package com.hm.achievement.command;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of handling the /aach toggle command, which allows a player to override the default behaviour of the
 * NotifyOtherPlayers parameter.
 * 
 * @author Pyves
 */
@Singleton
public class ToggleCommand extends AbstractCommand {

	// Indicates whether a player has used toggle since last server restart.
	private final Set<String> toggledPlayers = new HashSet<>();

	private boolean configNotifyOtherPlayers;
	private String langToggleDisplayed;
	private String langToggleHidden;

	@Inject
	public ToggleCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, ReloadCommand reloadCommand) {
		super(mainConfig, langConfig, pluginHeader, reloadCommand);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configNotifyOtherPlayers = mainConfig.getBoolean("NotifyOtherPlayers", false);

		langToggleDisplayed = pluginHeader + Lang.get(CmdLang.TOGGLE_DISPLAYED, langConfig);
		langToggleHidden = pluginHeader + Lang.get(CmdLang.TOGGLE_HIDDEN, langConfig);
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
	void executeCommand(CommandSender sender, String[] args) {
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
