package com.hm.achievement.db.data;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents an Achievement that has been awarded to a player.
 */
public class AwardedDBAchievement {

	private final UUID awardedTo;
	private final String name;
	private final long dateAwarded;
	private final String formattedDate;

	public AwardedDBAchievement(UUID awardedTo, String name, long dateAwarded, String formattedDate) {
		this.awardedTo = awardedTo;
		this.name = name;
		this.dateAwarded = dateAwarded;
		this.formattedDate = formattedDate;
	}

	public UUID getAwardedTo() {
		return awardedTo;
	}

	public String getName() {
		return name;
	}

	public long getDateAwarded() {
		return dateAwarded;
	}

	public String getFormattedDate() {
		return formattedDate;
	}

	@Override
	public int hashCode() {
		return Objects.hash(awardedTo, dateAwarded, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AwardedDBAchievement)) {
			return false;
		}
		AwardedDBAchievement other = (AwardedDBAchievement) obj;
		return Objects.equals(awardedTo, other.awardedTo) && dateAwarded == other.dateAwarded
				&& Objects.equals(name, other.name);
	}

}
