package com.hm.achievement.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Class in charge of handling /aach commands and dispatching to the different command modules.
 *
 * @author Pyves
 */
@Singleton
public class PluginCommandExecutor implements CommandExecutor {

	private final GiveCommand giveCommand;
	private final AddCommand addCommand;
	private final BookCommand bookCommand;
	private final TopCommand topCommand;
	private final WeekCommand weekCommand;
	private final MonthCommand monthCommand;
	private final ListCommand listCommand;
	private final StatsCommand statsCommand;
	private final InfoCommand infoCommand;
	private final HelpCommand helpCommand;
	private final CheckCommand checkCommand;
	private final DeleteCommand deleteCommand;
	private final ReloadCommand reloadCommand;
	private final ToggleCommand toggleCommand;
	private final ResetCommand resetCommand;
	private final GenerateCommand generateCommand;
	private final EasterEggCommand easterEggCommand;

	@Inject
	public PluginCommandExecutor(GiveCommand giveCommand, AddCommand addCommand, BookCommand bookCommand,
			TopCommand topCommand, WeekCommand weekCommand, MonthCommand monthCommand, ListCommand listCommand,
			StatsCommand statsCommand, InfoCommand infoCommand, HelpCommand helpCommand, CheckCommand checkCommand,
			DeleteCommand deleteCommand, ReloadCommand reloadCommand, ToggleCommand toggleCommand, ResetCommand resetCommand,
			GenerateCommand generateCommand, EasterEggCommand easterEggCommand) {
		this.giveCommand = giveCommand;
		this.addCommand = addCommand;
		this.bookCommand = bookCommand;
		this.topCommand = topCommand;
		this.weekCommand = weekCommand;
		this.monthCommand = monthCommand;
		this.listCommand = listCommand;
		this.statsCommand = statsCommand;
		this.infoCommand = infoCommand;
		this.helpCommand = helpCommand;
		this.checkCommand = checkCommand;
		this.deleteCommand = deleteCommand;
		this.reloadCommand = reloadCommand;
		this.toggleCommand = toggleCommand;
		this.resetCommand = resetCommand;
		this.generateCommand = generateCommand;
		this.easterEggCommand = easterEggCommand;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String subCmd = args.length > 0 ? args[0] : null;
		// Map to an Advanced Achievements command.
		if ((args.length == 1) && !"help".equalsIgnoreCase(subCmd)) {
			if ("book".equalsIgnoreCase(subCmd)) {
				bookCommand.executeCommand(sender, null, "book");
			} else if ("hcaa".equalsIgnoreCase(subCmd)) {
				easterEggCommand.executeCommand(sender, null, "easteregg");
			} else if ("reload".equalsIgnoreCase(subCmd)) {
				reloadCommand.executeCommand(sender, null, "reload");
			} else if ("generate".equalsIgnoreCase(subCmd)) {
				generateCommand.executeCommand(sender, null, "generate");
			} else if ("stats".equalsIgnoreCase(subCmd)) {
				statsCommand.executeCommand(sender, null, "stats");
			} else if ("list".equalsIgnoreCase(subCmd)) {
				listCommand.executeCommand(sender, null, "list");
			} else if ("top".equalsIgnoreCase(subCmd)) {
				topCommand.executeCommand(sender, args, "top");
			} else if ("week".equalsIgnoreCase(subCmd)) {
				weekCommand.executeCommand(sender, args, "week");
			} else if ("month".equalsIgnoreCase(subCmd)) {
				monthCommand.executeCommand(sender, args, "month");
			} else if ("info".equalsIgnoreCase(subCmd)) {
				infoCommand.executeCommand(sender, null, null);
			} else if ("toggle".equalsIgnoreCase(subCmd)) {
				toggleCommand.executeCommand(sender, null, "toggle");
			} else {
				helpCommand.executeCommand(sender, args, null);
			}
		} else if ((args.length == 3) && "reset".equalsIgnoreCase(subCmd)) {
			resetCommand.executeCommand(sender, args, "reset");
		} else if ((args.length == 3) && "give".equalsIgnoreCase(subCmd)) {
			giveCommand.executeCommand(sender, args, "give");
		} else if ((args.length >= 3) && "check".equalsIgnoreCase(subCmd)) {
			checkCommand.executeCommand(sender, args, "check");
		} else if ((args.length >= 3) && "delete".equalsIgnoreCase(subCmd)) {
			deleteCommand.executeCommand(sender, args, "delete");
		} else if ((args.length == 4) && "add".equalsIgnoreCase(subCmd)) {
			addCommand.executeCommand(sender, args, "add");
		} else {
			helpCommand.executeCommand(sender, args, null);
		}
		return true;
	}

}
