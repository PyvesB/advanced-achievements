package com.hm.achievement.db.data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ConnectionInformation {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final String date;
	private final long count;

	public ConnectionInformation(String date, long count) {
		this.date = date;
		this.count = count;
	}

	public ConnectionInformation() {
		this.date = LocalDate.EPOCH.format(DATE_TIME_FORMATTER);
		this.count = 0L;
	}

	public String getDate() {
		return date;
	}

	public long getCount() {
		return count;
	}

	public boolean isToday() {
		return today().equals(date);
	}

	public static String today() {
		return LocalDate.now().format(DATE_TIME_FORMATTER);
	}

}
