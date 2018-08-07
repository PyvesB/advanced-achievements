package com.hm.achievement.advancement;

/**
 * The values of this Enum correspond to the "frame" parameter of advancements.
 * 
 * @author Pyves
 *
 */
public enum AdvancementType {

	GOAL,
	CHALLENGE,
	TASK;

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
