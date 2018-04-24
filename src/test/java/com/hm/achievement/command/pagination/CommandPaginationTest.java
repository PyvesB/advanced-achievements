package com.hm.achievement.command.pagination;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CommandPaginationTest {

	@Test
	public void testPagination() {
		List<String> toPaginate = Arrays.asList(
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"
		);

		CommandPagination pagination = new CommandPagination(toPaginate);

		List<String> expected = Arrays.asList(
				"§7> §5Page 1/4",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8",
				"§7>"
		);

		List<String> result = new ArrayList<>();
		pagination.sendPage(1, result::add);

		assertEquals(expected, result);
	}

	@Test
	public void testPaginationPage2() {
		List<String> toPaginate = Arrays.asList(
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"
		);

		CommandPagination pagination = new CommandPagination(toPaginate);

		List<String> expected = Arrays.asList(
				"§7> §5Page 2/4",
				"9", "10", "1", "2", "3", "4", "5", "6", "7", "8",
				"9", "10", "1", "2", "3", "4", "5", "6",
				"§7>"
		);

		List<String> result = new ArrayList<>();
		pagination.sendPage(2, result::add);

		assertEquals(expected, result);
	}

	@Test
	public void testPaginationPage3() {
		List<String> toPaginate = Arrays.asList(
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"
		);

		CommandPagination pagination = new CommandPagination(toPaginate);

		List<String> expected = Arrays.asList(
				"§7> §5Page 3/4",
				"7", "8", "9", "10", "1", "2", "3", "4", "5", "6",
				"7", "8", "9", "10", "1", "2", "3", "4",
				"§7>"
		);

		List<String> result = new ArrayList<>();
		pagination.sendPage(3, result::add);

		assertEquals(expected, result);
	}

	@Test
	public void testPaginationPage4() {
		List<String> toPaginate = Arrays.asList(
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"
		);

		CommandPagination pagination = new CommandPagination(toPaginate);

		List<String> expected = Arrays.asList(
				"§7> §5Page 4/4",
				"5", "6", "7", "8", "9", "10",
				"§7>"
		);

		List<String> result = new ArrayList<>();
		pagination.sendPage(4, result::add);

		assertEquals(expected, result);
	}

	@Test
	public void testPaginationPage5WhenOnly4Pages() {
		List<String> toPaginate = Arrays.asList(
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"
		);

		CommandPagination pagination = new CommandPagination(toPaginate);

		List<String> expected = Arrays.asList(
				"§7> §5Page 4/4",
				"5", "6", "7", "8", "9", "10",
				"§7>"
		);

		List<String> result = new ArrayList<>();
		pagination.sendPage(5, result::add);

		assertEquals(expected, result);
	}

	@Test
	public void testEmptyPagination() {
		List<String> toPaginate = Collections.emptyList();

		CommandPagination pagination = new CommandPagination(toPaginate);

		List<String> expected = Arrays.asList(
				"§7> §5Page 0/0",
				"§7>"
		);

		List<String> result = new ArrayList<>();
		pagination.sendPage(1, result::add);

		assertEquals(expected, result);
	}


}