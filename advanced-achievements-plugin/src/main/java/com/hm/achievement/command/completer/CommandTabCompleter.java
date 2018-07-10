package com.hm.achievement.command.completer;

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

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of handling auto-completion for achievements and categories
 * when using /aach check, /aach reset, /aach give or /aach delete commands.
 * 
 * @author Pyves
 *
 */
@Singleton
public class CommandTabCompleter implements TabCompleter, Reloadable {

	private static final int MAX_LIST_LENGTH = 50;

	private final Set<String> enabledCategoriesWithSubcategories = new HashSet<>();
	private final CommentedYamlConfiguration mainConfig;
	private final Map<String, String> achievementsAndDisplayNames;
	private final Set<String> disabledCategories;

	private Set<String> configCommandsKeys;

	@Inject
	public CommandTabCompleter(@Named("main") CommentedYamlConfiguration mainConfig,
			Map<String, String> achievementsAndDisplayNames, Set<String> disabledCategories) {
		this.mainConfig = mainConfig;
		this.achievementsAndDisplayNames = achievementsAndDisplayNames;
		this.disabledCategories = disabledCategories;
	}

	@Override
	public void extractConfigurationParameters() {
		configCommandsKeys = mainConfig.getShallowKeys("Commands");

		enabledCategoriesWithSubcategories.clear();
		for (MultipleAchievements category : MultipleAchievements.values()) {
			for (String subcategory : mainConfig.getShallowKeys(category.toString())) {
				enabledCategoriesWithSubcategories.add(category + "." + StringUtils.deleteWhitespace(subcategory));
			}
		}
		for (NormalAchievements category : NormalAchievements.values()) {
			enabledCategoriesWithSubcategories.add(category.toString());
		}
		enabledCategoriesWithSubcategories.add("Commands");
		// Only auto-complete with non-disabled categories.
		enabledCategoriesWithSubcategories.removeAll(disabledCategories);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!"aach".equals(command.getName()) || args.length == 3 && !"add".equalsIgnoreCase(args[0])
				|| args.length == 4 && "add".equalsIgnoreCase(args[0])) {
			// Complete with players.
			return null;
		} else if (args.length == 2 && "reset".equalsIgnoreCase(args[0])) {
			return getPartialList(enabledCategoriesWithSubcategories, args[1]);
		} else if (args.length == 3 && "add".equalsIgnoreCase(args[0])) {
			return getPartialList(enabledCategoriesWithSubcategories, args[2]);
		} else if (args.length == 2 && "give".equalsIgnoreCase(args[0])) {
			return getPartialList(configCommandsKeys, args[1]);
		} else if (args.length == 2 && ("delete".equalsIgnoreCase(args[0]) || "check".equalsIgnoreCase(args[0]))) {
			return getPartialList(achievementsAndDisplayNames.keySet(), args[1]);
		}
		// No completion.
		return Collections.singletonList("");
	}

	/**
	 * Returns a partial list based on the input set. Members of the returned list
	 * must start with what the player has types so far. The list also has a limited
	 * length to avoid filling the player's screen.
	 * 
	 * @param fullSet
	 * @param prefix
	 * @return a list limited in length, containing elements matching the prefix,
	 */
	private List<String> getPartialList(Set<String> fullSet, String prefix) {
		// Sort matching elements by alphabetical order.
		List<String> fullList = fullSet.stream().filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
				.map(s -> StringUtils.replace(s, " ", "\u2423")).sorted().collect(Collectors.toList());

		if (fullList.size() > MAX_LIST_LENGTH) {
			List<String> partialList = fullList.subList(0, MAX_LIST_LENGTH - 2);
			// Suspension points to show that list was truncated.
			partialList.add("\u2022\u2022\u2022");
			return partialList;
		}
		return fullList;
	}
}
