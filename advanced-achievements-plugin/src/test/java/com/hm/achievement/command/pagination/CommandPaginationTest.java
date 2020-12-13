package com.hm.achievement.command.pagination;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Class for testing the command pagination utility.
 * 
 * @author Rsl1122
 */
class CommandPaginationTest {

	private final List<String> toPaginate = Arrays.asList(
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

	private YamlConfiguration langConfig;

	@BeforeEach
	void setUp() throws Exception {
		langConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(getClass().getResourceAsStream("/lang.yml")));
	}

	@Test
	void testPagination() {
		CommandPagination pagination = new CommandPagination(toPaginate, 18, langConfig);

		List<String> expected = Arrays.asList(
				getPaginationHeader(1, 4),
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8",
				getPaginationFooter());

		List<String> result = new ArrayList<>();
		pagination.sendPage(1, result::add);

		assertEquals(expected, result);
	}

	@Test
	void testPaginationPage2() {
		CommandPagination pagination = new CommandPagination(toPaginate, 18, langConfig);

		List<String> expected = Arrays.asList(
				getPaginationHeader(2, 4),
				"9", "10", "1", "2", "3", "4", "5", "6", "7", "8",
				"9", "10", "1", "2", "3", "4", "5", "6",
				getPaginationFooter());

		List<String> result = new ArrayList<>();
		pagination.sendPage(2, result::add);

		assertEquals(expected, result);
	}

	@Test
	void testPaginationPage3() {
		CommandPagination pagination = new CommandPagination(toPaginate, 18, langConfig);

		List<String> expected = Arrays.asList(
				getPaginationHeader(3, 4),
				"7", "8", "9", "10", "1", "2", "3", "4", "5", "6",
				"7", "8", "9", "10", "1", "2", "3", "4",
				getPaginationFooter());

		List<String> result = new ArrayList<>();
		pagination.sendPage(3, result::add);

		assertEquals(expected, result);
	}

	@Test
	void testPaginationPage4() {
		CommandPagination pagination = new CommandPagination(toPaginate, 18, langConfig);

		List<String> expected = Arrays.asList(
				getPaginationHeader(4, 4),
				"5", "6", "7", "8", "9", "10",
				getPaginationFooter());

		List<String> result = new ArrayList<>();
		pagination.sendPage(4, result::add);

		assertEquals(expected, result);
	}

	@Test
	void testPaginationPage5WhenOnly4Pages() {
		CommandPagination pagination = new CommandPagination(toPaginate, 18, langConfig);

		List<String> expected = Arrays.asList(
				getPaginationHeader(4, 4),
				"5", "6", "7", "8", "9", "10",
				getPaginationFooter());

		List<String> result = new ArrayList<>();
		pagination.sendPage(5, result::add);

		assertEquals(expected, result);
	}

	@Test
	void testPaginationPageSinglePage() {
		CommandPagination pagination = new CommandPagination(Collections.singletonList("1"), 18, langConfig);

		List<String> expected = Arrays.asList(
				getPaginationHeader(1, 1),
				"1",
				getPaginationFooter());

		List<String> result = new ArrayList<>();
		pagination.sendPage(5, result::add);

		assertEquals(expected, result);
	}

	@Test
	void testEmptyPagination() {
		CommandPagination pagination = new CommandPagination(Collections.emptyList(), 18, langConfig);

		List<String> expected = Arrays.asList(
				getPaginationHeader(0, 0),
				getPaginationFooter());

		List<String> result = new ArrayList<>();
		pagination.sendPage(1, result::add);

		assertEquals(expected, result);
	}

	private String getPaginationHeader(int page, int max) {
		return ChatColor.translateAlternateColorCodes('&',
				StringUtils.replaceEach(langConfig.getString("pagination-header"), new String[] { "PAGE", "MAX" },
						new String[] { Integer.toString(page), Integer.toString(max) }));
	}

	private String getPaginationFooter() {
		return ChatColor.translateAlternateColorCodes('&', langConfig.getString("pagination-footer"));
	}

}
