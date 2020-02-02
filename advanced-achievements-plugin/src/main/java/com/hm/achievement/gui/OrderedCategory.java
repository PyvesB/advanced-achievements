package com.hm.achievement.gui;

import java.util.Objects;

import com.hm.achievement.category.Category;

/**
 * Small wrapper to define a ordering between the categories.
 * 
 * @author Pyves
 *
 */
public class OrderedCategory implements Comparable<OrderedCategory> {

	private final int order;
	private final Category category;

	public OrderedCategory(int order, Category category) {
		this.order = order;
		this.category = category;
	}

	public int getOrder() {
		return order;
	}

	public Category getCategory() {
		return category;
	}

	@Override
	public int compareTo(OrderedCategory o) {
		return Integer.compare(getOrder(), o.getOrder());
	}

	// Only use category for hashCode and equals to avoid duplicates in maps.
	@Override
	public int hashCode() {
		return Objects.hash(category);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof OrderedCategory)) {
			return false;
		}
		OrderedCategory other = (OrderedCategory) obj;
		return Objects.equals(category, other.category);
	}
}
