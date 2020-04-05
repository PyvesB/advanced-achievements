package com.hm.achievement.command.completer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.hm.achievement.category.CommandAchievements;
import com.hm.achievement.command.executable.AbstractCommand;
import com.hm.achievement.command.executable.CommandSpec;
import com.hm.achievement.command.executable.DeleteCommand;
import com.hm.achievement.command.executable.EasterEggCommand;
import com.hm.achievement.command.executable.GenerateCommand;
import com.hm.achievement.command.executable.ResetCommand;
import com.hm.achievement.command.executable.Upgrade13Command;
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
	private final Map<String, String> namesToDisplayNames;
	private final Map<String, String> displayNamesToNames;
	private final Set<String> enabledCategoriesWithSubcategories;
	private final Set<CommandSpec> commandSpecs;
	private final int serverVersion;

	@Inject
	public CommandTabCompleter(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("ntd") Map<String, String> namesToDisplayNames, @Named("dtn") Map<String, String> displayNamesToNames,
			Set<String> enabledCategoriesWithSubcategories, Set<AbstractCommand> commands, int serverVersion) {
		this.mainConfig = mainConfig;
		this.namesToDisplayNames = namesToDisplayNames;
		this.displayNamesToNames = displayNamesToNames;
		this.enabledCategoriesWithSubcategories = enabledCategoriesWithSubcategories;
		this.serverVersion = serverVersion;
		this.commandSpecs = commands.stream()
				.filter(c -> !(c instanceof EasterEggCommand || c instanceof Upgrade13Command
						|| serverVersion < 12 && c instanceof GenerateCommand))
				.map(c -> c.getClass().getAnnotation(CommandSpec.class))
				.collect(Collectors.toSet());
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (shouldReturnPlayerList(command, args)) {
			return null; // Complete with players.
		}

		String aachCommand = args[0];
		Collection<String> options = Collections.emptyList();
		if (args.length == 2 && "reset".equalsIgnoreCase(aachCommand)) {
			options = new HashSet<>(enabledCategoriesWithSubcategories);
			options.add(ResetCommand.WILDCARD);
		} else if (args.length == 2 && "give".equalsIgnoreCase(aachCommand)) {
			options = mainConfig.getShallowKeys(CommandAchievements.COMMANDS.toString());
		} else if (args.length == 2 && "check".equalsIgnoreCase(aachCommand)) {
			options = namesToDisplayNames.keySet();
		} else if (args.length == 2 && "delete".equalsIgnoreCase(aachCommand)) {
			options = new HashSet<>(namesToDisplayNames.keySet());
			options.add(DeleteCommand.WILDCARD);
		} else if (args.length == 2 && "inspect".equalsIgnoreCase(aachCommand)) {
			options = displayNamesToNames.keySet();
		} else if (args.length == 2 && "add".equalsIgnoreCase(aachCommand)) {
			options = Collections.singleton("1");
		} else if (args.length == 3 && "add".equalsIgnoreCase(aachCommand)) {
			options = enabledCategoriesWithSubcategories;
		} else if (args.length == 1) {
			options = commandSpecs.stream()
					.filter(cs -> cs.permission().isEmpty() || sender.hasPermission("achievement." + cs.permission()))
					.map(CommandSpec::name).collect(Collectors.toSet());
		}
		return getPartialList(options, args[args.length - 1]);
	}

	/**
	 * Returns a partial list based on the input set. Members of the returned list must start with what the player has
	 * types so far. The list also has a limited length prior to Minecraft 1.13 to avoid filling the player's screen.
	 *
	 * @param options
	 * @param prefix
	 * @return a list limited in length, containing elements matching the prefix.
	 */
	private List<String> getPartialList(Collection<String> options, String prefix) {
		// Find matching options
		// Replace spaces with an Open Box character to prevent completing wrong word. Prevented Behaviour:
		// T -> Tamer -> Teleport Man -> Teleport The Avener -> Teleport The The Smelter
		// Sort matching elements by alphabetical order.
		List<String> allOptions = options.stream()
				.filter(s1 -> s1.toLowerCase().startsWith(prefix.toLowerCase()))
				.map(s -> s.replace(' ', '\u2423'))
				.sorted()
				.collect(Collectors.toList());

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
