package com.hm.achievement.command.executable;

import com.hm.achievement.utils.CommandUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.mcshared.file.CommentedYamlConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 * Abstract class in charge of factoring out common functionality for commands with more than one argument (/aach give,
 * delete and check).
 *
 * @author Pyves
 */
public abstract class AbstractParsableCommand extends AbstractCommand {

	private String langPlayerOffline;
	private String langEntityNotPlayer;

	AbstractParsableCommand(CommentedYamlConfiguration mainConfig, CommentedYamlConfiguration langConfig,
			StringBuilder pluginHeader) {
		super(mainConfig, langConfig, pluginHeader);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langPlayerOffline = LangHelper.get(CmdLang.PLAYER_OFFLINE, langConfig);
		langEntityNotPlayer = LangHelper.get(CmdLang.NOT_A_PLAYER, langConfig);
	}

	/**
	 * Executes actions specific to the class extending this abstract class.
	 *
	 * @param sender
	 * @param args
	 * @param player
	 */
	abstract void onExecuteForPlayer(CommandSender sender, String[] args, Player player);

	@Override
	void onExecute(CommandSender sender, String[] args) {
		String searchedName = args[args.length - 1];
		Entity[] entities = CommandUtils.getTargets(sender, searchedName);
		for(int i = 0; i < entities.length;i++){
			if(entities[i]==null) {
				sender.sendMessage(pluginHeader + StringUtils.replaceOnce(langPlayerOffline, "PLAYER", searchedName));
				break;
			}
			try {
				onExecuteForPlayer(sender, args, (Player) entities[i]);
			} catch (ClassCastException e) {
				sender.sendMessage(pluginHeader + langPlayerOffline);
			}
		}

	}

	/**
	 * Extracts the name of the achievement from the command line arguments.
	 *
	 * @param args
	 * @return the achievement name
	 */
	String parseAchievementName(String[] args) {
		StringBuilder achievementName = new StringBuilder();
		// Rebuild name of achievement by concatenating elements in the string array. The name of the player is last.
		for (int i = 1; i < args.length - 1; i++) {
			achievementName.append(args[i]);
			if (i != args.length - 2) {
				achievementName.append(' ');
			}
		}
		return achievementName.toString();
	}

//	private boolean startsWithSelector(String arg) {
//		List<String> selectors = Arrays.asList("@a", "@p", "@s", "@r");
//		for(String selector : selectors) {
//			if(arg.startsWith(selector)) return true;
//		}
//		return false;
//
//				String argsString = arg;
//				for(int i = 2; i < args.length; i++) {
//					argsString += " " + args[i];
//				}
//				//boolean opBefore = sender.isOp();//Only needed if Command should be useable by non-ops
//				//sender.setOp(true);
//				Bukkit.dispatchCommand(sender, "minecraft:execute " + args[0] + " ~ ~ ~ " + label + " self " + argsString);
//				//sender.setOp(opBefore);
//				return true;
//		return false;
//	}
}
