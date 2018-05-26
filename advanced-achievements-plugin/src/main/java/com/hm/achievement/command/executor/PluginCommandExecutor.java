package com.hm.achievement.command.executor;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.hm.achievement.command.executable.AbstractCommand;
import com.hm.achievement.command.executable.CommandSpec;
import com.hm.achievement.command.executable.HelpCommand;

/**
 * Class in charge of handling /aach commands and dispatching to the different command modules.
 *
 * @author Pyves
 */
@Singleton
public class PluginCommandExecutor implements CommandExecutor {

	private final HelpCommand helpCommand;
	private final Set<AbstractCommand> commands;

	@Inject
	public PluginCommandExecutor(HelpCommand helpCommand, Set<AbstractCommand> commands) {
		this.helpCommand = helpCommand;
		this.commands = commands;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		commands.stream().filter(cmd -> shouldExecute(cmd, args)).findFirst().orElse(helpCommand).execute(sender, args);
		return true;
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
		return args.length >= Math.max(1, annotation.minArgs()) && args.length <= annotation.maxArgs()
				&& annotation.name().equalsIgnoreCase(args[0]);
	}
}
