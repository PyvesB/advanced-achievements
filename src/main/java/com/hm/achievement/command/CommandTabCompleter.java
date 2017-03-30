package com.hm.achievement.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Class in charge of handling auto-completion for achievements and categories when using /aach check, /aach reset,
 * /aach give or /aach delete commands.
 * 
 * @author Pyves
 *
 */
public class CommandTabCompleter implements TabCompleter {

	private static final int MAX_LIST_LENGTH = 50;

	private final Set<String> categories;
	private final AdvancedAchievements plugin;

	public CommandTabCompleter(AdvancedAchievements plugin) {
		Set<String> categories = new HashSet<>(
				MultipleAchievements.values().length + NormalAchievements.values().length + 1);
		for (MultipleAchievements category : MultipleAchievements.values()) {
			categories.add(category.toString());
		}
		for (NormalAchievements category : NormalAchievements.values()) {
			categories.add(category.toString());
		}
		categories.add("Commands");
		this.categories = categories;
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!"aach".equals(command.getName()) || args.length == 3) {
			// Complete with players.
			return null;
		} else if (args.length == 2 && "reset".equalsIgnoreCase(args[0])) {
			return getPartialList(categories, args[1]);
		} else if (args.length == 2 && "give".equalsIgnoreCase(args[0])) {
			return getPartialList(plugin.getPluginConfig().getConfigurationSection("Commands").getKeys(false), args[1]);
		} else if (args.length == 2 && ("delete".equalsIgnoreCase(args[0]) || "check".equalsIgnoreCase(args[0]))) {
			return getPartialList(plugin.getAchievementsAndDisplayNames().keySet(), args[1]);
		}
		// No completion.
		List<String> list = new ArrayList<>(1);
		list.add("");
		return list;
	}

	/**
	 * Returns a partial list based on the input set. Members of the returned list must start with what the player has
	 * types so far. The list also has a limited length to avoid spamming the console.
	 * 
	 * @param fullSet
	 * @param prefix
	 * @return
	 */
	private List<String> getPartialList(Set<String> fullSet, String prefix) {
		List<String> fullList = new ArrayList<>(fullSet.size() + 1);
		for (String string : fullSet) {
			// Check whether string is a prefix of what the player has typed so far.
			if (string.toLowerCase().startsWith(prefix.toLowerCase())) {
				fullList.add(StringUtils.replace(string, " ", "\u2423"));
			}
		}
		// Sort list alphabetically.
		Collections.sort(fullList);
		if (fullList.size() > MAX_LIST_LENGTH) {
			List<String> partialList = fullList.subList(0, MAX_LIST_LENGTH - 2);
			// Suspension points to show that list was truncated.
			partialList.add("\u2022\u2022\u2022");
			return partialList;
		}
		return fullList;
	}
}
