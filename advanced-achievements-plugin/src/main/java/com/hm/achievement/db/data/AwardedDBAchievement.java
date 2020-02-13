package com.hm.achievement.db.data;

import com.hm.achievement.api.AdvancedAchievementsAPI;
import com.hm.achievement.category.Category;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents an Achievement that has been awarded to a player.
 */
public class AwardedDBAchievement extends DBAchievement {

	private final UUID awardedTo;
	private final long dateAwarded;
	private final String formattedDate;

	public AwardedDBAchievement(UUID awardedTo, String name, String message, long dateAwarded, String formattedDate) {
		super(name, message);
		this.awardedTo = awardedTo;
		this.dateAwarded = dateAwarded;
		this.formattedDate = formattedDate;
	}

	public AwardedDBAchievement(UUID awardedTo, String name, String message, long dateAwarded, String formattedDate,
			Category category) {
		super(name, message, category);
		this.awardedTo = awardedTo;
		this.dateAwarded = dateAwarded;
		this.formattedDate = formattedDate;
	}

	public UUID getAwardedTo() {
		return awardedTo;
	}

	public long getDateAwarded() {
		return dateAwarded;
	}

	public String getFormattedDate() {
		return formattedDate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;
		AwardedDBAchievement that = (AwardedDBAchievement) o;
		return Objects.equals(awardedTo, that.awardedTo) && Objects.equals(dateAwarded, that.dateAwarded);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), awardedTo, dateAwarded);
	}

	public AdvancedAchievementsAPI.Achievement toAPIAchievement() {
		return new AdvancedAchievementsAPI.Achievement(name, message, formattedDate);
	}
}
