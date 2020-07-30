package com.hm.achievement.db;

import com.hm.achievement.achievement.Achievement;
import com.hm.achievement.achievement.AchievementBuilder;
import com.hm.achievement.category.CommandAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import org.bukkit.command.Command;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class AchievementCache implements Reloadable {

	private final Set<Achievement> cache = new HashSet<>();
	private final Map<String, Achievement> nameMap = new HashMap<>();
	private final Map<String, Set<Achievement>> categoryMap = new HashMap<>();
	private final CommentedYamlConfiguration mainConfig;

	@Inject
	public AchievementCache(@Named("main") CommentedYamlConfiguration mainConfig) {
		this.mainConfig = mainConfig;
	}

	public Set<Achievement> getCache() {
		return cache;
	}

	public Achievement getByName(String name) {
		return nameMap.get(name.toLowerCase());
	}

	public Set<Achievement> getByCategory(String category) {
		return categoryMap.getOrDefault(category.toLowerCase(), new HashSet<>());
	}

	public void load() {
		cache.clear();
		nameMap.clear();
		for (NormalAchievements value : NormalAchievements.values()) {
			if (mainConfig.contains(value.getCategoryName())) {
				cache.addAll(loadInner(value.getCategoryName(), value.getCategoryName()));
			}
		}
		for (MultipleAchievements value : MultipleAchievements.values()) {
			if (mainConfig.contains(value.getCategoryName())) {
				cache.addAll(loadMulti(value.getCategoryName()));
			}
		}
		if (mainConfig.contains(CommandAchievements.COMMANDS.toString())) {
			cache.addAll(loadInner(CommandAchievements.COMMANDS.toString(), CommandAchievements.COMMANDS.toString()));
		}

		nameMap.putAll(cache.stream().collect(Collectors.toMap(a -> a.getName().toLowerCase(), i -> i)));
		categoryMap
				.putAll(cache.stream()
						.collect(Collectors.groupingBy(a -> a.getCategory().toLowerCase(), Collectors.toSet())));
	}


	private Set<Achievement> loadMulti(String category) {
		Set<Achievement> achievement = new HashSet<>();
		for (String key : mainConfig.getShallowKeys(category)) {
			String innerSection = category + "." + key;
			achievement.addAll(loadInner(innerSection, category, key));
		}
		return achievement;
	}

	private Set<Achievement> loadInner(String section, String category) {
		Set<Achievement> achievements = new HashSet<>();
		for (String inner : mainConfig.getShallowKeys(section)) {
			int requirement = 1;
			if (inner.matches("\\d+")) {
				requirement = Integer.parseInt(inner);
			}
			String path = section + "." + inner + ".";
			String goal = mainConfig.getString(path + "Goal", "");
			String displayName = mainConfig.getString(path + "DisplayName", "");
			String name = mainConfig.getString(path + "Name");
			String message = mainConfig.getString(path + "Message", "");
			achievements.add(new AchievementBuilder().name(name)
					.goal(goal)
					.message(message)
					.displayName(displayName)
					.requirement(requirement)
					.category(category)
					.build());
		}
		return achievements;
	}
	private Set<Achievement> loadInner(String section, String category, String subCategory) {
		Set<Achievement> achievements = new HashSet<>();
		for (String inner : mainConfig.getShallowKeys(section)) {
			int requirement = Integer.parseInt(inner);
			String path = section + "." + inner + ".";
			String goal = mainConfig.getString(path + "Goal", "");
			String displayName = mainConfig.getString(path + "DisplayName", "");
			String name = mainConfig.getString(path + "Name");
			String message = mainConfig.getString(path + "Message", "");
			achievements.add(new AchievementBuilder().name(name)
					.goal(goal)
					.message(message)
					.displayName(displayName)
					.requirement(requirement)
					.category(category)
					.subCategory(subCategory)
					.build());
		}
		return achievements;
	}

	@Override
	public void extractConfigurationParameters() {
		load();
	}
}
