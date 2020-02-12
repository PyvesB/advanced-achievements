package com.hm.achievement.command.pagination;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.mcshared.file.CommentedYamlConfiguration;

import utilities.MockUtility;

/**
 * Class for testing the command pagination utility.
 * 
 * @author Rsl1122
 */
public class CommandPaginationTest {

	private final List<String> toPaginate = Arrays.asList(
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

	@ClassRule
	public static final TemporaryFolder temporaryFolder = new TemporaryFolder();

	private CommentedYamlConfiguration langConfig;

	@Before
	public void setUp() throws Exception {
		MockUtility mockUtility = MockUtility.setUp()
				.withPluginDescription()
				.withLogger()
				.withDataFolder(temporaryFolder.getRoot())
				.withPluginFile("lang.yml");
		langConfig = mockUtility.getLoadedConfig("lang.yml");
	}

	@Test
	public void testPagination() {
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
	public void testPaginationPage2() {
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
	public void testPaginationPage3() {
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
	public void testPaginationPage4() {
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
	public void testPaginationPage5WhenOnly4Pages() {
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
	public void testPaginationPageSinglePage() {
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
	public void testEmptyPagination() {
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
				LangHelper.getEachReplaced(CmdLang.PAGINATION_HEADER, langConfig,
						new String[] { "PAGE", "MAX" },
						new String[] { Integer.toString(page), Integer.toString(max) }));
	}

	private String getPaginationFooter() {
		return ChatColor.translateAlternateColorCodes('&',
				LangHelper.get(CmdLang.PAGINATION_FOOTER, langConfig));
	}

}
