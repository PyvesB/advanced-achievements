package com.hm.achievement.utils;

import java.util.regex.Pattern;

/**
 * Simple class providing a helper methods to process text.
 * 
 * @author Pyves
 */
public class TextHelper {

	private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(&|§)([a-f]|r|[k-o]|[0-9]){1}");

	public static String removeFormattingCodes(String text) {
		return FORMATTING_CODE_PATTERN.matcher(text).replaceAll("");
	}

}
