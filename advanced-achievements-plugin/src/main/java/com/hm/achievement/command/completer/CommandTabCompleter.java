package com.hm.achievement.command.completer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;

import com.hm.achievement.category.CommandAchievements;
import com.hm.achievement.command.executable.AbstractCommand;
import com.hm.achievement.command.executable.CommandSpec;
import com.hm.achievement.command.executable.EasterEggCommand;
import com.hm.achievement.utils.StringHelper;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of handling auto-completion for achievements and categories when using /aach check, /aach reset,
 * /aach give or /aach delete commands.
 *
 * @author Pyves
 */
@Singleton
public class CommandTabCompleter implements TabCompleter {

	private static final int MAX_LIST_LENGTH = 50;

	private final CommentedYamlConfiguration mainConfig;
	private final Map<String, String> achievementsAndDisplayNames;
	private final Set<String> enabledCategoriesWithSubcategories;
	private final Set<CommandSpec> commandSpecs;
	private final int serverVersion;

	@Inject
	public CommandTabCompleter(@Named("main") CommentedYamlConfiguration mainConfig,
			Map<String, String> achievementsAndDisplayNames, Set<String> enabledCategoriesWithSubcategories,
			Set<AbstractCommand> commands, int serverVersion) {
		this.mainConfig = mainConfig;
		this.achievementsAndDisplayNames = achievementsAndDisplayNames;
		this.enabledCategoriesWithSubcategories = enabledCategoriesWithSubcategories;
		this.serverVersion = serverVersion;
		this.commandSpecs = commands.stream().filter(c -> !(c instanceof EasterEggCommand))
				.map(c -> c.getClass().getAnnotation(CommandSpec.class)).collect(Collectors.toSet());
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (shouldReturnPlayerList(command, args)) {
			return null; // Complete with players.
		}

		String aachCommand = args[0];
		Collection<String> options = Collections.emptyList();
		if (args.length == 2 && "reset".equalsIgnoreCase(aachCommand)) {
			options = enabledCategoriesWithSubcategories;
		} else if (args.length == 2 && "give".equalsIgnoreCase(aachCommand)) {
			options = mainConfig.getShallowKeys(CommandAchievements.COMMANDS.toString());
		} else if (args.length == 2 && StringUtils.equalsAnyIgnoreCase(aachCommand, "check", "delete")) {
			options = achievementsAndDisplayNames.keySet();
		} else if (args.length == 2 && "inspect".equalsIgnoreCase(aachCommand)) {
			options = achievementsAndDisplayNames.values();
		} else if (args.length == 2 && "add".equalsIgnoreCase(aachCommand)) {
			options = Collections.singleton("1");
		} else if (args.length == 3 && "add".equalsIgnoreCase(aachCommand)) {
			options = enabledCategoriesWithSubcategories;
		} else if (args.length == 1) {
			options = commandSpecs.stream()
					.filter(cs -> cs.permission().isEmpty() || sender.hasPermission("achievement." + cs.permission()))
					.map(CommandSpec::name).collect(Collectors.toSet());
		}
		return getPartialList(sender, options, args[args.length - 1]);
	}

	/**
	 * Returns a partial list based on the input set. Members of the returned list must start with what the player has
	 * types so far. The list also has a limited length prior to Minecraft 1.13 to avoid filling the player's screen.
	 *
	 * @param sender
	 * @param options
	 * @param prefix
	 * @return a list limited in length, containing elements matching the prefix.
	 */
	private List<String> getPartialList(CommandSender sender, Collection<String> options, String prefix) {
		if (sender instanceof ConsoleCommandSender) {
			// Console mapper uses the given parameters, spaces and all.
			return getFormattedMatchingOptions(options, prefix, Function.identity());
		} else {
			// Default mapper replaces spaces with an Open Box character to prevent completing wrong word.
			// Prevented Behaviour:
			// T -> Tamer -> Teleport Man -> Teleport The Avener -> Teleport The The Smelter
			return getFormattedMatchingOptions(options, prefix, s -> s.replace(' ', '\u2423'));
		}
	}

	private List<String> getFormattedMatchingOptions(Collection<String> options, String prefix,
			Function<String, String> displayMapper) {
		// Remove chat colors
		// Find matching options
		// Map matches to be displayed properly with displayMapper
		// Sort matching elements by alphabetical order.
		List<String> allOptions = options.stream()
				.map(StringHelper::removeFormattingCodes)
				.filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
				.map(displayMapper).sorted().collect(Collectors.toList());

		if (serverVersion < 13 && allOptions.size() > MAX_LIST_LENGTH) {
			allOptions = allOptions.subList(0, MAX_LIST_LENGTH - 1);
			// Suspension points to show that list was truncated.
			allOptions.add("\u2022\u2022\u2022");
		}
		return allOptions;
	}

	private boolean shouldReturnPlayerList(Command command, String[] args) {
		return !"aach".equals(command.getName())
				|| args.length == 3 && StringUtils.equalsAnyIgnoreCase(args[0], "give", "reset", "check", "delete")
				|| args.length == 4 && "add".equalsIgnoreCase(args[0]);
	}
}
