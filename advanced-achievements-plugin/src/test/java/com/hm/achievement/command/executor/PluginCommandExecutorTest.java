package com.hm.achievement.command.executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hm.achievement.command.executable.AbstractCommand;
import com.hm.achievement.command.executable.HelpCommand;
import com.hm.achievement.command.executable.InspectCommand;
import com.hm.achievement.command.executable.ListCommand;

/**
 * Class for testing the command executor.
 *
 * @author Pyves
 */
@ExtendWith(MockitoExtension.class)
class PluginCommandExecutorTest {

	private static final String PLUGIN_HEADER = "header ";
	private static final String ERROR_MESSAGE = "error message";

	@Mock
	private CommandSender sender;

	@Mock
	private YamlConfiguration langConfig;

	@Mock
	private HelpCommand helpCommand;

	@Mock
	private ListCommand listCommand;

	@Mock
	private InspectCommand argsCommand;

	private PluginCommandExecutor underTest;

	@BeforeEach
	void setUp() {
		when(langConfig.getString(any())).thenReturn(ERROR_MESSAGE);
		Set<AbstractCommand> commands = new HashSet<>();
		commands.add(helpCommand);
		commands.add(listCommand);
		commands.add(argsCommand);
		StringBuilder pluginHeader = new StringBuilder(PLUGIN_HEADER);
		underTest = new PluginCommandExecutor(langConfig, commands, pluginHeader);
		underTest.extractConfigurationParameters();
	}

	@Test
	void itShouldCallListCommand() {
		String[] args = { "list" };
		underTest.onCommand(sender, null, null, args);

		verify(listCommand).execute(sender, args);
		verifyNoMoreInteractions(listCommand, helpCommand, sender);
	}

	@Test
	void itShouldDisplayErrorMessageIfNoCommandCouldBeMapped() {
		String[] args = { "book", "unexpected_arg" };
		underTest.onCommand(sender, null, null, args);

		verify(sender).sendMessage(PLUGIN_HEADER + ERROR_MESSAGE);
		verifyNoMoreInteractions(listCommand, helpCommand, sender);
	}

	@Test
	void itShouldFallBackToHelpCommandIfArgsEmpty() {
		String[] noArgs = {};
		underTest.onCommand(sender, null, null, noArgs);

		verify(helpCommand).execute(sender, noArgs);
		verifyNoMoreInteractions(listCommand, helpCommand, sender);
	}

	@Test
	void itShouldParseOpenBoxCharactersIntoArray() {
		String[] args = { "inspect", "one\u2423two", "three\u2423four" };
		String[] expected = { "inspect", "one", "two", "three", "four" };
		underTest.onCommand(sender, null, null, args);

		verify(argsCommand).execute(sender, expected);
	}
}
