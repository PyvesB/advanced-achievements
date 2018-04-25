package com.hm.achievement.command.pagination;

import com.hm.mcshared.file.CommentedYamlConfiguration;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import utilities.MockUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CommandPaginationTest {

	private final List<String> toPaginate = Arrays.asList(
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"
	);

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
				"§7> §5Page 1/4",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8",
				"§7> "
		);

		List<String> result = new ArrayList<>();
		pagination.sendPage(1, result::add);

		assertEquals(expected, result);
	}

	@Test
	public void testPaginationPage2() {
		CommandPagination pagination = new CommandPagination(toPaginate, 18, langConfig);

		List<String> expected = Arrays.asList(
				"§7> §5Page 2/4",
				"9", "10", "1", "2", "3", "4", "5", "6", "7", "8",
				"9", "10", "1", "2", "3", "4", "5", "6",
				"§7> "
		);

		List<String> result = new ArrayList<>();
		pagination.sendPage(2, result::add);

		assertEquals(expected, result);
	}

	@Test
	public void testPaginationPage3() {
		CommandPagination pagination = new CommandPagination(toPaginate, 18, langConfig);

		List<String> expected = Arrays.asList(
				"§7> §5Page 3/4",
				"7", "8", "9", "10", "1", "2", "3", "4", "5", "6",
				"7", "8", "9", "10", "1", "2", "3", "4",
				"§7> "
		);

		List<String> result = new ArrayList<>();
		pagination.sendPage(3, result::add);

		assertEquals(expected, result);
	}

	@Test
	public void testPaginationPage4() {
		CommandPagination pagination = new CommandPagination(toPaginate, 18, langConfig);

		List<String> expected = Arrays.asList(
				"§7> §5Page 4/4",
				"5", "6", "7", "8", "9", "10",
				"§7> "
		);

		List<String> result = new ArrayList<>();
		pagination.sendPage(4, result::add);

		assertEquals(expected, result);
	}

	@Test
	public void testPaginationPage5WhenOnly4Pages() {
		CommandPagination pagination = new CommandPagination(toPaginate, 18, langConfig);

		List<String> expected = Arrays.asList(
				"§7> §5Page 4/4",
				"5", "6", "7", "8", "9", "10",
				"§7> "
		);

		List<String> result = new ArrayList<>();
		pagination.sendPage(5, result::add);

		assertEquals(expected, result);
	}

	@Test
	public void testEmptyPagination() {
		CommandPagination pagination = new CommandPagination(Collections.emptyList(), 18, langConfig);

		List<String> expected = Arrays.asList(
				"§7> §5Page 0/0",
				"§7> "
		);

		List<String> result = new ArrayList<>();
		pagination.sendPage(1, result::add);

		assertEquals(expected, result);
	}


}