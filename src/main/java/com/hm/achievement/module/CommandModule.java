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
public abstract class CommandModule {

	@Binds
	@IntoSet
	public abstract AbstractCommand bindHelpCommand(HelpCommand helpCommand);

	@Binds
	@IntoSet
	public abstract AbstractCommand bindBookCommand(BookCommand bookCommand);

	@Binds
	@IntoSet
	public abstract AbstractCommand bindInfoCommand(InfoCommand infoCommand);

	@Binds
	@IntoSet
	public abstract AbstractCommand bindListCommand(ListCommand listCommand);

	@Binds
	@IntoSet
	public abstract AbstractCommand bindStatsCommand(StatsCommand statsCommand);

	@Binds
	@IntoSet
	public abstract AbstractCommand bindReloadCommand(ReloadCommand reloadCommand);

	@Binds
	@IntoSet
	public abstract AbstractCommand bindToggleCommand(ToggleCommand toggleCommand);

	@Binds
	@IntoSet
	public abstract AbstractCommand bindGenerateCommand(GenerateCommand generateCommand);

	@Binds
	@IntoSet
	public abstract AbstractCommand bindEasterEggCommand(EasterEggCommand easterEggCommand);

	@Binds
	@IntoSet
	public abstract AbstractCommand bindTopCommand(TopCommand topCommand);

	@Binds
	@IntoSet
	public abstract AbstractCommand bindWeekCommand(WeekCommand weekCommand);

	@Binds
	@IntoSet
	public abstract AbstractCommand bindMonthCommand(MonthCommand monthCommand);

	@Binds
	@IntoSet
	public abstract AbstractCommand bindGiveCommand(GiveCommand giveCommand);

	@Binds
	@IntoSet
	public abstract AbstractCommand bindResetCommand(ResetCommand resetCommand);

	@Binds
	@IntoSet
	public abstract AbstractCommand bindCheckCommand(CheckCommand checkCommand);

	@Binds
	@IntoSet
	public abstract AbstractCommand bindDeleteCommand(DeleteCommand deleteCommand);

	@Binds
	@IntoSet
	public abstract AbstractCommand bindAddCommand(AddCommand addCommand);

}
