package com.hm.achievement.domain;

import java.util.UUID;

/**
 * Class linking an {@link Achievement} with a player UUID and an awarded date (number in milliseconds representing the
 * difference between the awarded time and midnight, January 1, 1970 UTC).
 *
 * @author Pyves
 */
public class AwardedAchievement {

	private final Achievement achievement;
	private final UUID player;
	private final long awardedDate;

	public AwardedAchievement(Achievement achievement, UUID player, long awardedDate) {
		this.achievement = achievement;
		this.player = player;
		this.awardedDate = awardedDate;
	}

	public Achievement getAchievement() {
		return achievement;
	}

	public UUID getPlayer() {
		return player;
	}

	public long getAwardedDate() {
		return awardedDate;
	}

}
