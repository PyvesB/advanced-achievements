package com.hm.achievement.command.executor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.hm.achievement.command.executable.AbstractCommand;
import com.hm.achievement.command.executable.AddCommand;
import com.hm.achievement.command.executable.BookCommand;
import com.hm.achievement.command.executable.CheckCommand;
import com.hm.achievement.command.executable.DeleteCommand;
import com.hm.achievement.command.executable.EasterEggCommand;
import com.hm.achievement.command.executable.GenerateCommand;
import com.hm.achievement.command.executable.GiveCommand;
import com.hm.achievement.command.executable.HelpCommand;
import com.hm.achievement.command.executable.InfoCommand;
import com.hm.achievement.command.executable.ListCommand;
import com.hm.achievement.command.executable.MonthCommand;
import com.hm.achievement.command.executable.ReloadCommand;
import com.hm.achievement.command.executable.ResetCommand;
import com.hm.achievement.command.executable.StatsCommand;
import com.hm.achievement.command.executable.ToggleCommand;
import com.hm.achievement.command.executable.TopCommand;
import com.hm.achievement.command.executable.WeekCommand;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Class in charge of handling /aach commands and dispatching to the different command modules.
 *
 * @author Pyves
 */
@Singleton
public class PluginCommandExecutor implements CommandExecutor {

	private final HelpCommand helpCommand;
	private final Set<CommandSpec> commandSpecs = new HashSet<>();

	@Inject
	public PluginCommandExecutor(HelpCommand helpCommand, BookCommand bookCommand, InfoCommand infoCommand,
			ListCommand listCommand, StatsCommand statsCommand, ReloadCommand reloadCommand, ToggleCommand toggleCommand,
			GenerateCommand generateCommand, EasterEggCommand easterEggCommand, TopCommand topCommand,
			WeekCommand weekCommand, MonthCommand monthCommand, GiveCommand giveCommand, ResetCommand resetCommand,
			CheckCommand checkCommand, DeleteCommand deleteCommand, AddCommand addCommand) {
		this.helpCommand = helpCommand;
		commandSpecs.add(new CommandSpec(bookCommand, "book", 1, 1));
		commandSpecs.add(new CommandSpec(infoCommand, "info", 1, 1));
		commandSpecs.add(new CommandSpec(listCommand, "list", 1, 1));
		commandSpecs.add(new CommandSpec(statsCommand, "stats", 1, 1));
		commandSpecs.add(new CommandSpec(reloadCommand, "reload", 1, 1));
		commandSpecs.add(new CommandSpec(toggleCommand, "toggle", 1, 1));
		commandSpecs.add(new CommandSpec(generateCommand, "generate", 1, 1));
		commandSpecs.add(new CommandSpec(easterEggCommand, "hcaa", 1, 1));
		commandSpecs.add(new CommandSpec(topCommand, "top", 1, 2));
		commandSpecs.add(new CommandSpec(weekCommand, "week", 1, 2));
		commandSpecs.add(new CommandSpec(monthCommand, "month", 1, 2));
		commandSpecs.add(new CommandSpec(giveCommand, "give", 3, 3));
		commandSpecs.add(new CommandSpec(resetCommand, "reset", 3, 3));
		commandSpecs.add(new CommandSpec(checkCommand, "check", 3, Integer.MAX_VALUE));
		commandSpecs.add(new CommandSpec(deleteCommand, "delete", 3, Integer.MAX_VALUE));
		commandSpecs.add(new CommandSpec(addCommand, "add", 4, 4));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		commandSpecs.stream().filter(cs -> matchesSpec(cs, args)).map(cs -> cs.command).findFirst().orElse(helpCommand)
				.execute(sender, args);
		return true;
	}

	private boolean matchesSpec(CommandSpec commandSpec, String[] args) {
		return args.length >= commandSpec.minArgs && args.length <= commandSpec.maxArgs
				&& commandSpec.name.equalsIgnoreCase(args[0]);
	}

	private static final class CommandSpec {

		private final AbstractCommand command;
		private final String name;
		private final int minArgs;
		private final int maxArgs;

		private CommandSpec(AbstractCommand command, String name, int minArgs, int maxArgs) {
			this.command = command;
			this.name = name;
			this.minArgs = minArgs;
			this.maxArgs = maxArgs;
		}
	}
}
