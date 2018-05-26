package com.hm.achievement.command.executable;

import org.bukkit.command.CommandSender;

import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Dummy class to simulate a command implementation allowing zero argument as an input.
 * 
 * @author Pyves
 *
 */
@CommandSpec(name = "", permission = "", minArgs = 0, maxArgs = Integer.MAX_VALUE)
public class NoArgsCommand extends AbstractCommand {

	NoArgsCommand(CommentedYamlConfiguration mainConfig, CommentedYamlConfiguration langConfig, StringBuilder pluginHeader) {
		super(mainConfig, langConfig, pluginHeader);
	}

	@Override
	void onExecute(CommandSender sender, String[] args) {
	}

}
