package com.hm.achievement.command;

import org.bukkit.command.CommandSender;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.advancement.AdvancementManager;

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

		langAdvancementsGenerated = plugin.getChatHeader() + plugin.getPluginLang().getString("advancements-generated",
				"Advancements were successfully generated.");
		langMinecraftNotSupported = plugin.getChatHeader() + plugin.getPluginLang().getString("minecraft-not-supported",
				"Advancements not supported in your Minecraft version. Please update to 1.12+.");
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
