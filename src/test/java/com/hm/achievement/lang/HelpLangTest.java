package com.hm.achievement.lang;

import com.hm.achievement.lang.command.HelpLang;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class HelpLangTest {

	@Test
	public void testHelpAndHoverValuesExists() {
		Set<String> helpNames = Arrays.stream(HelpLang.values())
				.map(HelpLang::name)
				.collect(Collectors.toSet());
		Set<String> hoverNames = Arrays.stream(HelpLang.Hover.values())
				.map(HelpLang.Hover::name)
				.collect(Collectors.toSet());
		assertEquals(helpNames, hoverNames);
	}
	
}