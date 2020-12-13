package com.hm.achievement.command.executable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * Class in charge of handling the /aach toggle command, which allows a player to override the default behaviour of the
 * NotifyOtherPlayers parameter.
 * 
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "toggle", permission = "toggle", minArgs = 1, maxArgs = 2)
public class ToggleCommand extends AbstractCommand {

	// Indicates whether a player has used toggle since last server restart.
	private final Set<UUID> toggledPlayers = new HashSet<>();
	private final Map<String, Set<UUID>> typesToToggledPlayers = new HashMap<>();

	private boolean configNotifyOtherPlayers;
	private String langToggleDisplayed;
	private String langToggleHidden;

	@Inject
	public ToggleCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig,
			StringBuilder pluginHeader) {
		super(mainConfig, langConfig, pluginHeader);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configNotifyOtherPlayers = mainConfig.getBoolean("NotifyOtherPlayers");

		langToggleDisplayed = pluginHeader + langConfig.getString("toggle-displayed");
		langToggleHidden = pluginHeader + langConfig.getString("toggle-hidden");
	}

	/**
	 * Indicates whether player has used the /aach toggle command since the last server restart/plugin reload.
	 * 
	 * @param player
	 * @param type
	 * @return true if player has used the toggle command, false otherwise
	 */
	public boolean isPlayerToggled(Player player, String type) {
		return toggledPlayers.contains(player.getUniqueId())
				|| typesToToggledPlayers.getOrDefault(type, Collections.emptySet()).contains(player.getUniqueId());
	}

	@Override
	void onExecute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;
		Set<UUID> toggledPlayersForType = toggledPlayers;
		if (args.length > 1) {
			toggledPlayersForType = typesToToggledPlayers.computeIfAbsent(args[1], t -> new HashSet<>());
		}

		if (toggledPlayersForType.contains(player.getUniqueId())) {
			toggledPlayersForType.remove(player.getUniqueId());
			displayChatMessage(player, configNotifyOtherPlayers);
		} else {
			toggledPlayersForType.add(player.getUniqueId());
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
