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
import com.hm.achievement.command.executable.Upgrade13Command;
import com.hm.achievement.command.executable.WeekCommand;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface CommandModule {

	@Binds
	@IntoSet
	abstract AbstractCommand bindHelpCommand(HelpCommand helpCommand);

	@Binds
	@IntoSet
	abstract AbstractCommand bindBookCommand(BookCommand bookCommand);

	@Binds
	@IntoSet
	abstract AbstractCommand bindInfoCommand(InfoCommand infoCommand);

	@Binds
	@IntoSet
	abstract AbstractCommand bindListCommand(ListCommand listCommand);

	@Binds
	@IntoSet
	abstract AbstractCommand bindStatsCommand(StatsCommand statsCommand);

	@Binds
	@IntoSet
	abstract AbstractCommand bindReloadCommand(ReloadCommand reloadCommand);

	@Binds
	@IntoSet
	abstract AbstractCommand bindToggleCommand(ToggleCommand toggleCommand);

	@Binds
	@IntoSet
	abstract AbstractCommand bindGenerateCommand(GenerateCommand generateCommand);

	@Binds
	@IntoSet
	abstract AbstractCommand bindEasterEggCommand(EasterEggCommand easterEggCommand);

	@Binds
	@IntoSet
	abstract AbstractCommand bindTopCommand(TopCommand topCommand);

	@Binds
	@IntoSet
	abstract AbstractCommand bindWeekCommand(WeekCommand weekCommand);

	@Binds
	@IntoSet
	abstract AbstractCommand bindMonthCommand(MonthCommand monthCommand);

	@Binds
	@IntoSet
	abstract AbstractCommand bindGiveCommand(GiveCommand giveCommand);

	@Binds
	@IntoSet
	abstract AbstractCommand bindResetCommand(ResetCommand resetCommand);

	@Binds
	@IntoSet
	abstract AbstractCommand bindCheckCommand(CheckCommand checkCommand);

	@Binds
	@IntoSet
	abstract AbstractCommand bindDeleteCommand(DeleteCommand deleteCommand);

	@Binds
	@IntoSet
	abstract AbstractCommand bindAddCommand(AddCommand addCommand);

	@Binds
	@IntoSet
	abstract AbstractCommand bindUpdate13Command(Upgrade13Command upgrade13Command);

	@Binds
	@IntoSet
	abstract AbstractCommand bindInspectCommand(InspectCommand inspectCommand);
}
