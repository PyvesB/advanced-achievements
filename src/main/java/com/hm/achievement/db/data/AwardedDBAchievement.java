package com.hm.achievement.db.data;

import com.hm.achievement.category.Category;

import java.sql.Date;
import java.util.UUID;

/**
 * Represents an Achievement that has been awarded to a player.
 */
public class AwardedDBAchievement extends DBAchievement {

    private final UUID awardedTo;
    private final Date dateAwarded;

    public AwardedDBAchievement(UUID awardedTo, String name, String message, Date dateAwarded) {
        super(name, message);
        this.awardedTo = awardedTo;
        this.dateAwarded = dateAwarded;
    }

    public AwardedDBAchievement(UUID awardedTo, String name, String message, Date dateAwarded, Category category) {
        super(name, message, category);
        this.awardedTo = awardedTo;
        this.dateAwarded = dateAwarded;
    }

    public UUID getAwardedTo() {
        return awardedTo;
    }

    public Date getDateAwarded() {
        return dateAwarded;
    }
}
