package com.hm.achievement.command.executable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of handling the /aach toggle command, which allows a player to override the default behaviour of the
 * NotifyOtherPlayers parameter.
 * 
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "toggle", permission = "toggle", minArgs = 1, maxArgs = 1)
public class ToggleCommand extends AbstractCommand {

	// Indicates whether a player has used toggle since last server restart.
	private final Set<UUID> toggledPlayers = new HashSet<>();

	private boolean configNotifyOtherPlayers;
	private String langToggleDisplayed;
	private String langToggleHidden;

	@Inject
	public ToggleCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader) {
		super(mainConfig, langConfig, pluginHeader);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configNotifyOtherPlayers = mainConfig.getBoolean("NotifyOtherPlayers");

		langToggleDisplayed = pluginHeader + LangHelper.get(CmdLang.TOGGLE_DISPLAYED, langConfig);
		langToggleHidden = pluginHeader + LangHelper.get(CmdLang.TOGGLE_HIDDEN, langConfig);
	}

	/**
	 * Indicates whether player has used the /aach toggle command since the last server restart/plugin reload.
	 * 
	 * @param player
	 * @return true if player has used the toggle command, false otherwise
	 */
	public boolean isPlayerToggled(Player player) {
		return toggledPlayers.contains(player.getUniqueId());
	}

	@Override
	void onExecute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;
		if (toggledPlayers.contains(player.getUniqueId())) {
			toggledPlayers.remove(player.getUniqueId());
			displayChatMessage(player, configNotifyOtherPlayers);
		} else {
			toggledPlayers.add(player.getUniqueId());
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
