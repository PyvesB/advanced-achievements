package com.hm.achievement.command.executor;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.bukkit.command.CommandSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.hm.achievement.command.executable.HelpCommand;
import com.hm.achievement.command.executable.ListCommand;
import com.hm.achievement.command.executor.PluginCommandExecutor;

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

	@InjectMocks
	private PluginCommandExecutor underTest;

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
