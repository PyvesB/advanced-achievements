package com.hm.achievement.command;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.gui.MainGUI;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of handling the /aach list command, which displays interactive GUIs.
 * 
 * @author Pyves
 */
@Singleton
public class ListCommand extends AbstractCommand {

	private final MainGUI mainGUI;

	@Inject
	public ListCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, ReloadCommand reloadCommand,
			MainGUI mainGUI) {
		super(mainConfig, langConfig, pluginHeader, reloadCommand);
		this.mainGUI = mainGUI;
	}

	@Override
	void executeCommand(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;

		mainGUI.displayMainGUI(player);
	}
}
