package com.hm.achievement.command.completer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.hm.achievement.command.executable.AbstractCommand;
import com.hm.achievement.command.executable.BookCommand;
import com.hm.achievement.command.executable.EasterEggCommand;
import com.hm.achievement.command.executable.HelpCommand;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class for testing the command tab completer.
 *
 * @author Pyves
 */
@RunWith(MockitoJUnitRunner.class)
public class CommandTabCompleterTest {

	@Mock
	private CommentedYamlConfiguration mainConfig;

	@Mock
	private Command command;

	@Mock
	private CommandSender commandSender;

	@Mock
	private BookCommand bookCommand;

	@Mock
	private HelpCommand helpCommand;

	@Mock
	private EasterEggCommand easterEggCommand;

	private CommandTabCompleter underTest;

	@Before
	public void setUp() {
		when(command.getName()).thenReturn("aach");

		when(mainConfig.getShallowKeys(any())).thenReturn(singleton("someSubcategory"));
		when(mainConfig.getShallowKeys("Commands")).thenReturn(singleton("myCommand"));

		Set<AbstractCommand> commands = new HashSet<>();
		commands.add(bookCommand);
		commands.add(easterEggCommand);
		commands.add(helpCommand);
		Map<String, String> achievementsAndDisplayNames = new HashMap<>();
		achievementsAndDisplayNames.put("yourAch1", "Special Event Achievement!");
		achievementsAndDisplayNames.put("yourAch2", "&2Coloured &rAchievement!");
		achievementsAndDisplayNames.put("No Display Name Achievements!", "");
		List<String> enabledCategoriesWithSubcategories = Arrays.asList("Custom.someSubcategory", "Beds",
				"Breaks.someSubcategory", "Breeding.someSubcategory", "Brewing");
		underTest = new CommandTabCompleter(mainConfig, achievementsAndDisplayNames, enabledCategoriesWithSubcategories,
				commands, 13);
	}

	@Test
	public void shouldReturnNullIfNotAachCommand() {
		when(command.getName()).thenReturn("someothercommand");

		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, new String[0]);

		assertNull(completionResult);
	}

	@Test
	public void shouldReturnNullForPlayerArgOfGiveCommand() {
		String[] args = new String[] { "give", "yourAch1", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertNull(completionResult);
	}

	@Test
	public void shouldReturnNullForPlayerArgOfResetCommand() {
		String[] args = new String[] { "reset", "Beds", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertNull(completionResult);
	}

	@Test
	public void shouldReturnNullForPlayerArgOfCheckCommand() {
		String[] args = new String[] { "check", "yourAch1", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertNull(completionResult);
	}

	@Test
	public void shouldReturnNullForPlayerArgOfDeleteCommand() {
		String[] args = new String[] { "delete", "yourAch1", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertNull(completionResult);
	}

	@Test
	public void shouldReturnNullForPlayerArgOfAddCommand() {
		String[] args = new String[] { "add", "1", "Breaks.sand", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertNull(completionResult);
	}

	@Test
	public void shoudCompleteWithNonEasterEggCommands() {
		when(commandSender.hasPermission(anyString())).thenReturn(true);

		String[] args = new String[] { "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("book", "help"), completionResult);
	}

	@Test
	public void shoudNotCompleteWithCommandsForWhichTheSenderHasNoPermissions() {
		when(commandSender.hasPermission(anyString())).thenReturn(false);

		String[] args = new String[] { "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("help"), completionResult);
	}

	@Test
	public void shoudCompleteForResetCommand() {
		String[] args = new String[] { "reset", "B" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("Beds", "Breaks.someSubcategory", "Breeding.someSubcategory", "Brewing"), completionResult);
	}

	@Test
	public void shoudCompleteForAddCommand() {
		String[] args = new String[] { "add", "1", "Cust" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("Custom.someSubcategory"), completionResult);
	}

	@Test
	public void shoudCompleteForGiveCommand() {
		String[] args = new String[] { "give", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("myCommand"), completionResult);
	}

	@Test
	public void shoudCompleteForDeleteCommand() {
		String[] args = new String[] { "delete", "y" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("yourAch1", "yourAch2"), completionResult);
	}

	@Test
	public void shoudCompleteForCheckCommand() {
		String[] args = new String[] { "check", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("No␣Display␣Name␣Achievements!", "yourAch1", "yourAch2"), completionResult);
	}

	@Test
	public void shoudCompleteForInspectCommand() {
		String[] args = new String[] { "inspect", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("", "Coloured␣Achievement!", "Special␣Event␣Achievement!"), completionResult);
	}

	@Test
	public void shoudCompleteWithoutReplacingWhitespacesIfConsole() {
		ConsoleCommandSender consoleCommandSender = Mockito.mock(ConsoleCommandSender.class);
		String[] args = new String[] { "check", "N" };
		List<String> completionResult = underTest.onTabComplete(consoleCommandSender, command, null, args);

		assertEquals(asList("No Display Name Achievements!"), completionResult);
	}

	@Test
	public void shoudTruncateCompletionListOnOldServerVersionsIfOverFiftyElements() {
		underTest = new CommandTabCompleter(mainConfig, emptyMap(), emptyList(), emptySet(), 12);
		Set<String> commands = IntStream.rangeClosed(1, 100).boxed().map(i -> ("myCommand" + i)).collect(Collectors.toSet());
		when(mainConfig.getShallowKeys("Commands")).thenReturn(commands);

		String[] args = new String[] { "give", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(50, completionResult.size());
		assertEquals("\u2022\u2022\u2022", completionResult.get(49));
	}

	@Test
	public void shoudNotTruncateCompletionListIfRecentServerVersion() {
		Set<String> commands = IntStream.rangeClosed(1, 100).boxed().map(i -> ("myCommand" + i)).collect(Collectors.toSet());
		when(mainConfig.getShallowKeys("Commands")).thenReturn(commands);

		String[] args = new String[] { "give", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(100, completionResult.size());
	}

	@Test
	public void shoudReturnEmptyStringIfNoCompletionAvailable() {
		String[] args = new String[] { "list", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(emptyList(), completionResult);

		args = new String[] { "top", "1", "" };
		completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(emptyList(), completionResult);

		args = new String[] { "delete", "yourAch1", "DarkPyves", "" };
		completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(emptyList(), completionResult);
	}

}
