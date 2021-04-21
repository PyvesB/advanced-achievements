package com.hm.achievement.command.completer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hm.achievement.category.CommandAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.command.executable.AbstractCommand;
import com.hm.achievement.command.executable.BookCommand;
import com.hm.achievement.command.executable.EasterEggCommand;
import com.hm.achievement.command.executable.GenerateCommand;
import com.hm.achievement.command.executable.HelpCommand;
import com.hm.achievement.command.executable.Upgrade13Command;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.domain.Achievement.AchievementBuilder;

/**
 * Class for testing the command tab completer.
 *
 * @author Pyves
 */
@ExtendWith(MockitoExtension.class)
class CommandTabCompleterTest {

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

		Set<AbstractCommand> commands = new HashSet<>();
		commands.add(bookCommand);
		commands.add(easterEggCommand);
		commands.add(upgrade13Command);
		commands.add(generateCommand);
		commands.add(helpCommand);
		AchievementMap achievementMap = new AchievementMap();
		achievementMap.put(new AchievementBuilder().name("ach1").displayName("Special Event Achievement!")
				.category(CommandAchievements.COMMANDS).subcategory("yourAch1").build());
		achievementMap.put(new AchievementBuilder().name("ach2").displayName("&2Coloured &rAchievement!")
				.category(CommandAchievements.COMMANDS).subcategory("yourAch2").build());
		achievementMap.put(new AchievementBuilder().name("Spaced Name Achievement!")
				.displayName("Spaced Name Achievement!").build());
		achievementMap.put(new AchievementBuilder().name("ach4").displayName("Display 4").subcategory("workbench")
				.category(MultipleAchievements.CRAFTS).build());
		underTest = new CommandTabCompleter(achievementMap, commands, 13);
	}

	@Test
	void shouldReturnNullIfNotAachCommand() {
		when(command.getName()).thenReturn("someothercommand");

		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, new String[0]);

		assertNull(completionResult);
	}

	@Test
	void shouldReturnNullForPlayerArgOfGiveCommand() {
		String[] args = { "give", "yourAch1", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertNull(completionResult);
	}

	@Test
	void shouldReturnNullForPlayerArgOfResetCommand() {
		String[] args = { "reset", "Beds", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertNull(completionResult);
	}

	@Test
	void shouldReturnNullForPlayerArgOfCheckCommand() {
		String[] args = { "check", "yourAch1", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertNull(completionResult);
	}

	@Test
	void shouldReturnNullForPlayerArgOfDeleteCommand() {
		String[] args = { "delete", "yourAch1", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertNull(completionResult);
	}

	@Test
	void shouldReturnNullForPlayerArgOfAddCommand() {
		String[] args = { "add", "1", "Breaks.sand", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertNull(completionResult);
	}

	@Test
	void shoudCompleteWithNonEasterEggAndNonUpgrade13Commands() {
		when(commandSender.hasPermission(anyString())).thenReturn(true);

		String[] args = { "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("book", "generate", "help"), completionResult);
	}

	@Test
	void shoudNotCompleteWithGenerateCommandOnOldMinecraftVersion() {
		Set<AbstractCommand> commands = new HashSet<>();
		commands.add(bookCommand);
		commands.add(generateCommand);
		underTest = new CommandTabCompleter(new AchievementMap(), commands, 11);
		when(commandSender.hasPermission(anyString())).thenReturn(true);

		String[] args = { "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("book"), completionResult);
	}

	@Test
	void shoudNotCompleteWithCommandsForWhichTheSenderHasNoPermissions() {
		when(commandSender.hasPermission(anyString())).thenReturn(false);

		String[] args = { "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("help"), completionResult);
	}

	@Test
	void shoudCompleteForResetCommand() {
		String[] args = { "reset", "C" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("Crafts.workbench"), completionResult);
	}

	@Test
	void shoudCompleteWithNumberForAddCommand() {
		String[] args = { "add", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("1"), completionResult);
	}

	@Test
	void shoudCompleteWithCategoryForAddCommand() {
		String[] args = { "add", "1", "Cra" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("Crafts.workbench"), completionResult);
	}

	@Test
	void shoudCompleteForGiveCommand() {
		String[] args = { "give", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("yourAch1", "yourAch2"), completionResult);
	}

	@Test
	void shoudCompleteForDeleteCommand() {
		String[] args = { "delete", "a" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("ach1", "ach2", "ach4"), completionResult);
	}

	@Test
	void shoudCompleteForCheckCommand() {
		String[] args = { "check", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("Spaced␣Name␣Achievement!", "ach1", "ach2", "ach4"), completionResult);
	}

	@Test
	void shoudCompleteForInspectCommand() {
		String[] args = { "inspect", "s" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(asList("spaced␣name␣achievement!", "special␣event␣achievement!"), completionResult);
	}

	@Test
	void shoudTruncateCompletionListOnOldServerVersionsIfOverFiftyElements() {
		AchievementMap achievementMap = new AchievementMap();
		IntStream.rangeClosed(1, 100)
				.boxed()
				.map(i -> new AchievementBuilder()
						.name("ach" + i)
						.displayName("Display " + i)
						.category(CommandAchievements.COMMANDS)
						.subcategory("yourAch" + i)
						.build())
				.forEach(achievementMap::put);
		underTest = new CommandTabCompleter(achievementMap, emptySet(), 12);

		String[] args = { "give", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(50, completionResult.size());
		assertEquals("\u2022\u2022\u2022", completionResult.get(49));
	}

	@Test
	void shoudNotTruncateCompletionListIfRecentServerVersion() {
		AchievementMap achievementMap = new AchievementMap();
		IntStream.rangeClosed(1, 100)
				.boxed()
				.map(i -> new AchievementBuilder()
						.name("ach" + i)
						.displayName("Display " + i)
						.category(CommandAchievements.COMMANDS)
						.subcategory("yourAch" + i)
						.build())
				.forEach(achievementMap::put);
		underTest = new CommandTabCompleter(achievementMap, emptySet(), 16);

		String[] args = { "give", "" };
		List<String> completionResult = underTest.onTabComplete(commandSender, command, null, args);

		assertEquals(100, completionResult.size());
	}

	@Test
	void shoudReturnEmptyStringIfNoCompletionAvailable() {
		String[] args = { "list", "" };
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
