package com.hm.achievement.domain;

import java.util.Collections;
import java.util.List;

import com.hm.achievement.category.Category;

/**
 * Class representing an achievement found in Advanced Achievements' config.yml.
 *
 * @author Pyves
 */
public class Achievement {

	private final Category category;
	private final String subcategory;
	private final long threshold;
	private final String name;
	private final String displayName;
	private final String message;
	private final String goal;
	private final String type;
	private final List<Reward> rewards;

	private Achievement(Category category, String subcategory, long threshold, String name, String displayName,
			String message, String goal, String type, List<Reward> rewards) {
		this.category = category;
		this.subcategory = subcategory;
		this.threshold = threshold;
		this.name = name;
		this.displayName = displayName;
		this.message = message;
		this.goal = goal;
		this.type = type;
		this.rewards = rewards;
	}

	public Category getCategory() {
		return category;
	}

	public String getSubcategory() {
		return subcategory;
	}

	public long getThreshold() {
		return threshold;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getMessage() {
		return message;
	}

	public String getGoal() {
		return goal;
	}

	public String getType() {
		return type;
	}

	public List<Reward> getRewards() {
		return rewards;
	}

	public static class AchievementBuilder {

		private Category category;
		private String subcategory;
		private long threshold;
		private String name;
		private String displayName;
		private String message;
		private String goal;
		private String type;
		private List<Reward> rewards = Collections.emptyList();

		public AchievementBuilder category(Category category) {
			this.category = category;
			return this;
		}

		public AchievementBuilder subcategory(String subcategory) {
			this.subcategory = subcategory;
			return this;
		}

		public AchievementBuilder threshold(long threshold) {
			this.threshold = threshold;
			return this;
		}

		public AchievementBuilder name(String name) {
			this.name = name;
			return this;
		}

		public AchievementBuilder displayName(String displayName) {
			this.displayName = displayName;
			return this;
		}

		public AchievementBuilder message(String message) {
			this.message = message;
			return this;
		}

		public AchievementBuilder goal(String goal) {
			this.goal = goal;
			return this;
		}

		public AchievementBuilder type(String type) {
			this.type = type;
			return this;
		}

		public AchievementBuilder rewards(List<Reward> rewards) {
			this.rewards = rewards;
			return this;
		}

		public Achievement build() {
			return new Achievement(category, subcategory, threshold, name, displayName, message, goal, type, rewards);
		}
	}

}
