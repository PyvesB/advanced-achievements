package com.hm.achievement.gui;

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
}
