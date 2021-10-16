package com.hm.achievement.module;

import com.hm.achievement.command.executable.AbstractCommand;
import com.hm.achievement.command.executable.AddCommand;
import com.hm.achievement.command.executable.BookCommand;
import com.hm.achievement.command.executable.CheckCommand;
import com.hm.achievement.command.executable.DeleteCommand;
import com.hm.achievement.command.executable.EasterEggCommand;
import com.hm.achievement.command.executable.GenerateCommand;
import com.hm.achievement.command.executable.GiveCommand;
import com.hm.achievement.command.executable.HelpCommand;
import com.hm.achievement.command.executable.InfoCommand;
import com.hm.achievement.command.executable.InspectCommand;
import com.hm.achievement.command.executable.ListCommand;
import com.hm.achievement.command.executable.MonthCommand;
import com.hm.achievement.command.executable.ReloadCommand;
import com.hm.achievement.command.executable.ResetCommand;
import com.hm.achievement.command.executable.StatsCommand;
import com.hm.achievement.command.executable.ToggleCommand;
import com.hm.achievement.command.executable.TopCommand;
import com.hm.achievement.command.executable.WeekCommand;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface CommandModule {

	@Binds
	@IntoSet
	AbstractCommand bindHelpCommand(HelpCommand helpCommand);

	@Binds
	@IntoSet
	AbstractCommand bindBookCommand(BookCommand bookCommand);

	@Binds
	@IntoSet
	AbstractCommand bindInfoCommand(InfoCommand infoCommand);

	@Binds
	@IntoSet
	AbstractCommand bindListCommand(ListCommand listCommand);

	@Binds
	@IntoSet
	AbstractCommand bindStatsCommand(StatsCommand statsCommand);

	@Binds
	@IntoSet
	AbstractCommand bindReloadCommand(ReloadCommand reloadCommand);

	@Binds
	@IntoSet
	AbstractCommand bindToggleCommand(ToggleCommand toggleCommand);

	@Binds
	@IntoSet
	AbstractCommand bindGenerateCommand(GenerateCommand generateCommand);

	@Binds
	@IntoSet
	AbstractCommand bindEasterEggCommand(EasterEggCommand easterEggCommand);

	@Binds
	@IntoSet
	AbstractCommand bindTopCommand(TopCommand topCommand);

	@Binds
	@IntoSet
	AbstractCommand bindWeekCommand(WeekCommand weekCommand);

	@Binds
	@IntoSet
	AbstractCommand bindMonthCommand(MonthCommand monthCommand);

	@Binds
	@IntoSet
	AbstractCommand bindGiveCommand(GiveCommand giveCommand);

	@Binds
	@IntoSet
	AbstractCommand bindResetCommand(ResetCommand resetCommand);

	@Binds
	@IntoSet
	AbstractCommand bindCheckCommand(CheckCommand checkCommand);

	@Binds
	@IntoSet
	AbstractCommand bindDeleteCommand(DeleteCommand deleteCommand);

	@Binds
	@IntoSet
	AbstractCommand bindAddCommand(AddCommand addCommand);

	@Binds
	@IntoSet
	AbstractCommand bindInspectCommand(InspectCommand inspectCommand);
}
