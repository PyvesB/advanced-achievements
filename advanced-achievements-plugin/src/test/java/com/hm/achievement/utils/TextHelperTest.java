package com.hm.achievement.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Class for testing the text helpers.
 *
 * @author Pyves
 */
public class TextHelperTest {

	@Test
	public void shouldRemoveFormattingCodes() {
		String result = TextHelper.removeFormattingCodes("&0&1§2&3&4&5&6&7&8&9This &a&b§c&d&eis&f &ksome&l&m&n&o te&rxt!");

		assertEquals("This is some text!", result);
	}

	@Test
	public void shouldNotRemoveInvalidFormattingCodes() {
		String result = TextHelper.removeFormattingCodes("Incorrect formatting codes: &h& z");

		assertEquals("Incorrect formatting codes: &h& z", result);
	}

}
