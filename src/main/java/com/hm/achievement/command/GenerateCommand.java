package com.hm.achievement.command;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.advancement.AdvancementManager;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.command.CmdLang;
import org.bukkit.command.CommandSender;

/**
 * Class in charge of handling the /aach generate command, which creates advancements for the achievements defined in
 * the plugin's configuration.
 * 
 * @author Pyves
 */
public class GenerateCommand extends AbstractCommand {

	private boolean configRegisterAdvancementDescriptions;
	private boolean configHideAdvancements;
	private String langAdvancementsGenerated;
	private String langMinecraftNotSupported;

	public GenerateCommand(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configRegisterAdvancementDescriptions = plugin.getPluginConfig().getBoolean("RegisterAdvancementDescriptions",
				true);
		configHideAdvancements = plugin.getPluginConfig().getBoolean("HideAdvancements", false);

		langAdvancementsGenerated = Lang.getWithChatHeader(CmdLang.ADVANCEMENTS_GENERATED, plugin);
		langMinecraftNotSupported = Lang.getWithChatHeader(CmdLang.MINECRAFT_NOT_SUPPORTED, plugin);
	}

	@Override
	protected void executeCommand(CommandSender sender, String[] args) {
		if (version >= 12) {
			AdvancementManager advancementManager = new AdvancementManager(plugin,
					configRegisterAdvancementDescriptions, configHideAdvancements);
			advancementManager.registerAdvancements();

			sender.sendMessage(langAdvancementsGenerated);
		} else {
			sender.sendMessage(langMinecraftNotSupported);
		}
	}
}
