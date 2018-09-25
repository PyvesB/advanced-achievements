package com.hm.achievement.utils;

import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * Simple class providing a helper methods to process strings.
 * 
 * @author Pyves
 */
public class StringHelper {

	private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(&|§)([a-f]|r|[k-o]|[0-9]){1}");

	public static String removeFormattingCodes(String text) {
		return FORMATTING_CODE_PATTERN.matcher(text).replaceAll("");
	}

	public static String getClosestMatch(String toMatch, Collection<String> possibleMatches) {
		Integer smallestDistance = Integer.MAX_VALUE;
		String closestMatch = "";
		for (String possibleMatch : possibleMatches) {
			Integer distance = LevenshteinDistance.getDefaultInstance().apply(toMatch, possibleMatch);
			if (distance < smallestDistance) {
				smallestDistance = distance;
				closestMatch = possibleMatch;
			}
		}
		return closestMatch;
	}

	private StringHelper() {
		// Not called.
	}

}
