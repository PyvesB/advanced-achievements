package com.hm.achievement.command.executable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.hm.achievement.lang.GuiLang;
import com.hm.achievement.lang.LangHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.gui.MainGUI;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of handling the /aach list command, which displays interactive GUIs.
 * 
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "list", permission = "list", minArgs = 1, maxArgs = 1)
public class ListCommand extends AbstractCommand {

	private final MainGUI mainGUI;

	@Inject
	public ListCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, MainGUI mainGUI) {
		super(mainConfig, langConfig, pluginHeader);
		this.mainGUI = mainGUI;
	}

	@Override
	void onExecute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;

		if (player.isSleeping()) {
			sender.sendMessage(LangHelper.get(GuiLang.UNAVAILABLE_WHILST_SLEEPING, langConfig));
			return;
		}

		mainGUI.displayMainGUI(player);
	}
}
