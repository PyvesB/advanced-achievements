package com.hm.achievement.achievement;

public class AchievementBuilder {

	private String name;
	private String displayName;
	private String message;
	private String goal;
	private int requirement;
	private String category;

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

	public AchievementBuilder requirement(int requirement) {
		this.requirement = requirement;
		return this;
	}

	public AchievementBuilder category(String category) {
		this.category = category;
		return this;
	}

	public Achievement build() {
		return new Achievement(name, displayName, message, goal, requirement, category);
	}

}
