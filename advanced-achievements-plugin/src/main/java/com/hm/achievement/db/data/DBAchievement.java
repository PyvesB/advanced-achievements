package com.hm.achievement.db.data;

import com.hm.achievement.category.Category;

import java.util.Objects;
import java.util.Optional;

/**
 * Data container object for a single achievement.
 * <p>
 * The object was created to reduce "messy lists" returned by the database.
 */
public class DBAchievement {

	String name;
	String message;

	Category category;

	public DBAchievement(String name, String message) {
		this.name = name;
		this.message = message;
	}

	public DBAchievement(String name, String message, Category category) {
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
		return this instanceof AwardedDBAchievement;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DBAchievement that = (DBAchievement) o;
		return Objects.equals(name, that.name) && Objects.equals(message, that.message)
				&& Objects.equals(category, that.category);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, message, category);
	}
}
