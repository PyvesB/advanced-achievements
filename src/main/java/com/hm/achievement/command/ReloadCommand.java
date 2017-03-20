package com.hm.achievement.command;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.utils.AchievementCommentedYamlConfiguration;

/**
 * Class in charge of handling the /aach reload command, which reloads the plugin's configuration files.
 * 
 * @author Pyves
 */
public class ReloadCommand extends AbstractCommand {

	public ReloadCommand(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	protected void executeCommand(CommandSender sender, String[] args) {
		plugin.reloadConfig();
		plugin.setSuccessfulLoad(true);

		AchievementCommentedYamlConfiguration configFile = plugin.loadAndBackupFile("config.yml");

		// Compare the DisabledCategories configuration list in the previous configuration file and the new one.
		Set<String> disabledCategorySet = new HashSet<>(configFile.getList("DisabledCategories"));
		if (!Bukkit.getPluginManager().isPluginEnabled("PetMaster") || Integer.parseInt(Character.toString(
				Bukkit.getPluginManager().getPlugin("PetMaster").getDescription().getVersion().charAt(2))) < 4) {
			disabledCategorySet.add(NormalAchievements.PETMASTERGIVE.toString());
			disabledCategorySet.add(NormalAchievements.PETMASTERRECEIVE.toString());
		}
		if (!disabledCategorySet.equals(plugin.getDisabledCategorySet())) {
			if (sender instanceof Player) {
				sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang().getString("server-restart-reload",
						"DisabledCategories list was modified. Server must be fully reloaded or restarted for your changes to take effect."));
			}
			plugin.getLogger().warning(
					"DisabledCategories list was modified. Server must be fully reloaded or restarted for your changes to take effect.");
			plugin.getLogger().warning("Aborting plugin reload.");
			return;
		}

		AchievementCommentedYamlConfiguration langFile = plugin
				.loadAndBackupFile(configFile.getString("LanguageFileName", "lang.yml"));

		plugin.configurationLoad(configFile, langFile);

		if (plugin.isSuccessfulLoad()) {
			if (sender instanceof Player) {
				sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang()
						.getString("configuration-successfully-reloaded", "Configuration successfully reloaded."));
			}
			plugin.getLogger().info("Configuration successfully reloaded.");
		} else {
			if (sender instanceof Player) {
				sender.sendMessage(
						plugin.getChatHeader() + plugin.getPluginLang().getString("configuration-reload-failed",
								"Errors while reloading configuration. Please view logs for more details."));
			}
			plugin.getLogger().severe("Errors while reloading configuration. Please view logs for more details.");
		}
	}

}
