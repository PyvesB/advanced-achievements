package com.hm.achievement.db.data;

import com.hm.achievement.category.Category;

import java.util.Optional;

/**
 * Data container object for a single achievement.
 *
 * The object was created to reduce "messy lists" returned by the database.
 */
public class Achievement {

    private String name;
    private String message;

    private Category category;

    public Achievement(String name, String message) {
        this.name = name;
        this.message = message;
    }

    public Achievement(String name, String message, Category category) {
        this.name = name;
        this.message = message;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Optional<Category> getCategory() {
        return Optional.ofNullable(category);
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public boolean hasBeenAwarded() {
        return this instanceof AwardedAchievement;
    }
}
