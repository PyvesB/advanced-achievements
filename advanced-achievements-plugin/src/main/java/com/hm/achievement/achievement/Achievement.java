package com.hm.achievement.achievement;

public class Achievement {

	private final String name;
	private final String displayName;
	private final String message;
	private final String goal;
	private final int requirement;
	private final String category;
	private final String subCategory;

	public Achievement(String name, String displayName, String message, String goal, int requirement, String category,
			String subCategory) {
		this.name = name;
		this.displayName = displayName;
		this.message = message;
		this.goal = goal;
		this.requirement = requirement;
		this.category = category;
		this.subCategory = subCategory;
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

	public int getRequirement() {
		return requirement;
	}

	public String getCategory() {
		return category;
	}

	public String getSubCategory() {
		return subCategory;
	}
}
