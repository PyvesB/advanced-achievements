package com.hm.achievement.command.completer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hm.achievement.command.executable.AbstractCommand;
import com.hm.achievement.command.executable.BookCommand;
import com.hm.achievement.command.executable.EasterEggCommand;
import com.hm.achievement.command.executable.GenerateCommand;
import com.hm.achievement.command.executable.HelpCommand;
import com.hm.achievement.command.executable.Upgrade13Command;

/**
 * Class for testing the command tab completer.
 *
 * @author Pyves
 */
@ExtendWith(MockitoExtension.class)
class CommandTabCompleterTest {

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private YamlConfiguration mainConfig;

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

	@Mock
	private Upgrade13Command upgrade13Command;

	@Mock
	private GenerateCommand generateCommand;

	private CommandTabCompleter underTest;

	@BeforeEach
	void setUp() {
		when(command.getName()).thenReturn("aach");

		when(mainConfig.getConfigurationSection("Commands").getKeys(false)).thenReturn(singleton("myCommand"));

		Set<AbstractCommand> commands = new HashSet<>();
		commands.add(bookCommand);
		commands.add(easterEggCommand);
		commands.add(upgrade13Command);
		commands.add(generateCommand);
		commands.add(helpCommand);
		Map<String, String> namesToDisplayNames = new HashMap<>();
		namesToDisplayNames.put("yourAch1", "Special Event Achievement!");
		namesToDisplayNames.put("yourAch2", "&2Coloured &rAchievement!");
		namesToDisplayNames.put("No Display Name Achievement!", "");
		Map<String, String> displayNamesToNames = new HashMap<>();
		displayNamesToNames.put("special event achievement!", "yourAch1");
		displayNamesToNames.put("coloured achievement!", "yourAch2");
		displayNamesToNames.put("no display name Achievement!", "No Display Name Achievement!");
		Set<String> enabledCategoriesWithSubcategories = new HashSet<>(Arrays.asList("Custom.someSubcategory", "Beds",
				"Breaks.someSubcategory", "Breeding.someSubcategory", "Brewing"));
		underTest = new CommandTabCompleter(mainConfig, namesToDisplayNames, displayNamesToNames,
				enabledCategoriesWithSubcategories, commands, 13);
	}

	@Test
	void shouldReturnNullIfNotAachCommand() {
		when(command.getName()).thenReturn("someothercommand");

		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, new String[0]);

		assertNull(completionResult);
	}

	@Test
	void shouldReturnNullForPlayerArgOfGiveCommand() {
		String[] args = new String[] { "give", "yourAch1", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertNull(completionResult);
	}

	@Test
	void shouldReturnNullForPlayerArgOfResetCommand() {
		String[] args = new String[] { "reset", "Beds", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertNull(completionResult);
	}

	@Test
	void shouldReturnNullForPlayerArgOfCheckCommand() {
		String[] args = new String[] { "check", "yourAch1", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertNull(completionResult);
	}

	@Test
	void shouldReturnNullForPlayerArgOfDeleteCommand() {
		String[] args = new String[] { "delete", "yourAch1", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertNull(completionResult);
	}

	@Test
	void shouldReturnNullForPlayerArgOfAddCommand() {
		String[] args = new String[] { "add", "1", "Breaks.sand", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertNull(completionResult);
	}

	@Test
	void shoudCompleteWithNonEasterEggAndNonUpgrade13Commands() {
		when(commandSender.hasPermission(anyString())).thenReturn(true);

		String[] args = new String[] { "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("book", "generate", "help"), completionResult);
	}

	@Test
	void shoudNotCompleteWithGenerateCommandOnOldMinecraftVersion() {
		Set<AbstractCommand> commands = new HashSet<>();
		commands.add(bookCommand);
		commands.add(generateCommand);
		underTest = new CommandTabCompleter(mainConfig, emptyMap(), emptyMap(), emptySet(), commands, 11);
		when(commandSender.hasPermission(anyString())).thenReturn(true);

		String[] args = new String[] { "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("book"), completionResult);
	}

	@Test
	void shoudNotCompleteWithCommandsForWhichTheSenderHasNoPermissions() {
		when(commandSender.hasPermission(anyString())).thenReturn(false);

		String[] args = new String[] { "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("help"), completionResult);
	}

	@Test
	void shoudCompleteForResetCommand() {
		String[] args = new String[] { "reset", "B" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("Beds", "Breaks.someSubcategory", "Breeding.someSubcategory", "Brewing"), completionResult);
	}

	@Test
	void shoudCompleteWithNumberForAddCommand() {
		String[] args = new String[] { "add", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("1"), completionResult);
	}

	@Test
	void shoudCompleteWithCategoryForAddCommand() {
		String[] args = new String[] { "add", "1", "Cust" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("Custom.someSubcategory"), completionResult);
	}

	@Test
	void shoudCompleteForGiveCommand() {
		String[] args = new String[] { "give", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("myCommand"), completionResult);
	}

	@Test
	void shoudCompleteForDeleteCommand() {
		String[] args = new String[] { "delete", "y" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("yourAch1", "yourAch2"), completionResult);
	}

	@Test
	void shoudCompleteForCheckCommand() {
		String[] args = new String[] { "check", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("No␣Display␣Name␣Achievement!", "yourAch1", "yourAch2"), completionResult);
	}

	@Test
	void shoudCompleteForInspectCommand() {
		String[] args = new String[] { "inspect", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("coloured␣achievement!", "no␣display␣name␣Achievement!", "special␣event␣achievement!"),
				completionResult);
	}

	@Test
	void shoudTruncateCompletionListOnOldServerVersionsIfOverFiftyElements() {
		underTest = new CommandTabCompleter(mainConfig, emptyMap(), emptyMap(), emptySet(), emptySet(), 12);
		Set<String> commands = IntStream.rangeClosed(1, 100).boxed().map(i -> ("myCommand" + i)).collect(Collectors.toSet());
		when(mainConfig.getConfigurationSection("Commands").getKeys(false)).thenReturn(commands);

		String[] args = new String[] { "give", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(50, completionResult.size());
		assertEquals("\u2022\u2022\u2022", completionResult.get(49));
	}

	@Test
	void shoudNotTruncateCompletionListIfRecentServerVersion() {
		Set<String> commands = IntStream.rangeClosed(1, 100).boxed().map(i -> ("myCommand" + i)).collect(Collectors.toSet());
		when(mainConfig.getConfigurationSection("Commands").getKeys(false)).thenReturn(commands);

		String[] args = new String[] { "give", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(100, completionResult.size());
	}

	@Test
	void shoudReturnEmptyStringIfNoCompletionAvailable() {
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
