package com.hm.achievement.db;

import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

public class SubcategoryUUID {

	private final String subcategory;
	private final UUID uuid;

	public SubcategoryUUID(String subcategory, UUID uuid) {
		this.subcategory = StringUtils.deleteWhitespace(subcategory);
		this.uuid = uuid;
	}

	public String getSubcategory() {
		return subcategory;
	}

	public UUID getUUID() {
		return uuid;
	}

	@Override
	public int hashCode() {
		return Objects.hash(subcategory, uuid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SubcategoryUUID)) {
			return false;
		}
		SubcategoryUUID other = (SubcategoryUUID) obj;
		return Objects.equals(subcategory, other.subcategory) && Objects.equals(uuid, other.uuid);
	}

}
