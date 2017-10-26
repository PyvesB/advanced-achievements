package com.hm.achievement.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class in charge of handling the /aach list command, which displays interactive GUIs.
 * 
 * @author Pyves
 */
public class ListCommand extends AbstractCommand {

	public ListCommand(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	protected void executeCommand(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;

		plugin.getMainGUI().displayMainGUI(player);
	}
}
