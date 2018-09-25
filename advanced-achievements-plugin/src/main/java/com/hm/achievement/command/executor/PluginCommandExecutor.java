package com.hm.achievement.command.executor;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.hm.achievement.command.executable.AbstractCommand;
import com.hm.achievement.command.executable.CommandSpec;
import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of handling /aach commands and dispatching to the different command modules.
 *
 * @author Pyves
 */
@Singleton
public class PluginCommandExecutor implements CommandExecutor, Reloadable {

	private final CommentedYamlConfiguration langConfig;
	private final Set<AbstractCommand> commands;
	private final StringBuilder pluginHeader;

	private String langInvalidCommand;

	@Inject
	public PluginCommandExecutor(@Named("lang") CommentedYamlConfiguration langConfig, Set<AbstractCommand> commands,
			StringBuilder pluginHeader) {
		this.langConfig = langConfig;
		this.commands = commands;
		this.pluginHeader = pluginHeader;
	}

	@Override
	public void extractConfigurationParameters() {
		langInvalidCommand = pluginHeader + LangHelper.get(CmdLang.INVALID_COMMAND, langConfig);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String[] parsedArgs = parseArguments(args);
		Optional<AbstractCommand> cmdToExecute = commands.stream().filter(cmd -> shouldExecute(cmd, parsedArgs)).findFirst();
		if (cmdToExecute.isPresent()) {
			cmdToExecute.get().execute(sender, parsedArgs);
		} else {
			sender.sendMessage(langInvalidCommand);
		}
		return true;
	}

	private String[] parseArguments(String[] args) {
		return Arrays.stream(args)
				.flatMap(argument -> Arrays.stream(StringUtils.split(argument, '\u2423')))
				.toArray(String[]::new);
	}

	/**
	 * Determines whether an Advanced Achievements command should be executed based on the provided command line
	 * arguments.
	 * 
	 * @param command
	 * @param args
	 * @return true if command matches args, false otherwise.
	 */
	private boolean shouldExecute(AbstractCommand command, String[] args) {
		CommandSpec annotation = command.getClass().getAnnotation(CommandSpec.class);
		return args.length >= annotation.minArgs() && args.length <= annotation.maxArgs()
				&& (args.length == 0 || annotation.name().equalsIgnoreCase(args[0]));
	}
}
