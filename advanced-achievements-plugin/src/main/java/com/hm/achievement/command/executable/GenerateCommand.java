package com.hm.achievement.command.executable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.command.CommandSender;

import com.hm.achievement.advancement.AdvancementManager;
import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of handling the /aach generate command, which creates advancements for the achievements defined in
 * the plugin's configuration.
 * 
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "generate", permission = "generate", minArgs = 1, maxArgs = 1)
public class GenerateCommand extends AbstractCommand {

	private final int serverVersion;
	private final AdvancementManager advancementManager;

	private String langAdvancementsGenerated;
	private String langMinecraftNotSupported;

	@Inject
	public GenerateCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, int serverVersion,
			AdvancementManager advancementManager) {
		super(mainConfig, langConfig, pluginHeader);
		this.serverVersion = serverVersion;
		this.advancementManager = advancementManager;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langAdvancementsGenerated = pluginHeader + LangHelper.get(CmdLang.ADVANCEMENTS_GENERATED, langConfig);
		langMinecraftNotSupported = pluginHeader + LangHelper.get(CmdLang.MINECRAFT_NOT_SUPPORTED, langConfig);
	}

	@Override
	void onExecute(CommandSender sender, String[] args) {
		if (serverVersion >= 12) {
			advancementManager.registerAdvancements();

			sender.sendMessage(langAdvancementsGenerated);
		} else {
			sender.sendMessage(langMinecraftNotSupported);
		}
	}
}
