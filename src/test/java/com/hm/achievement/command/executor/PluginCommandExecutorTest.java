package com.hm.achievement.command.executor;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.hm.achievement.command.executable.AbstractCommand;
import com.hm.achievement.command.executable.HelpCommand;
import com.hm.achievement.command.executable.ListCommand;

/**
 * Class for testing the command executor.
 *
 * @author Pyves
 */
@RunWith(MockitoJUnitRunner.class)
public class PluginCommandExecutorTest {

	@Mock
	private HelpCommand helpCommand;

	@Mock
	private ListCommand listCommand;

	private PluginCommandExecutor underTest;

	@Before
	public void setUp() {
		Set<AbstractCommand> commands = new HashSet<>();
		commands.add(helpCommand);
		commands.add(listCommand);
		underTest = new PluginCommandExecutor(helpCommand, commands);
	}

	@Test
	public void itShouldCallListCommand() {
		CommandSender sender = Mockito.mock(CommandSender.class);
		String[] args = new String[] { "list" };
		underTest.onCommand(sender, null, null, args);

		verify(listCommand).execute(sender, args);
		verifyNoMoreInteractions(listCommand, helpCommand);
	}

	@Test
	public void itShouldFallBackToHelpCommandIfNoOtherCommandCouldBeMapped() {
		CommandSender sender = Mockito.mock(CommandSender.class);
		String[] args = new String[] { "list", "unexpected_arg" };
		underTest.onCommand(sender, null, null, args);

		verify(helpCommand).execute(sender, args);
		verifyNoMoreInteractions(listCommand, helpCommand);
	}

}
