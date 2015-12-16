package com.hm.achievement.command;

import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.language.Lang;
import com.hm.achievement.particle.PacketSender;

public class ListCommand {

	private AdvancedAchievements plugin;
	private boolean hideNotReceivedCategories;
	private boolean obfuscateNotReceived;

	public ListCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
		hideNotReceivedCategories = plugin.getConfig().getBoolean("HideNotReceivedCategories", false);
		obfuscateNotReceived = plugin.getConfig().getBoolean("ObfuscateNotReceived", true);
	}

	/**
	 * Display name of received achievements and name of missing achievements
	 * (goes through the entire config file). Does not display category is
	 * language file string is empty.
	 */
	public void getList(Player player) {

		String[] normalAchievementTypesLanguage = { Lang.LIST_CONNECTIONS.toString(), Lang.LIST_DEATHS.toString(),
				Lang.LIST_ARROWS.toString(), Lang.LIST_SNOWBALLS.toString(), Lang.LIST_EGGS.toString(),
				Lang.LIST_FISH.toString(), Lang.LIST_ITEMBREAKS.toString(), Lang.LIST_EATENITEMS.toString(),
				Lang.LIST_SHEAR.toString(), Lang.LIST_MILK.toString(), Lang.LIST_TRADES.toString(),
				Lang.LIST_ANVILS.toString(), Lang.LIST_ENCHANTMENTS.toString(), Lang.LIST_BEDS.toString(),
				Lang.LIST_MAXLEVEL.toString(), Lang.LIST_POTIONS.toString(), Lang.LIST_PLAYEDTIME.toString(),
				Lang.LIST_ITEMDROPS.toString(), Lang.LIST_HOEPLOWINGS.toString(), Lang.LIST_FERTILISING.toString(),
				Lang.LIST_TAMING.toString(), Lang.LIST_BREWING.toString(), Lang.LIST_COMMANDS.toString() };
		String[] multipleAchievementTypesLanguage = { Lang.LIST_PLACES.toString(), Lang.LIST_BREAKS.toString(),
				Lang.LIST_KILLS.toString(), Lang.LIST_CRAFTS.toString() };

		StringBuilder achievementsList = new StringBuilder();
		int numberInCategory = 0;

		// Build list of achievements with multiple sub-categories.
		for (int i = 0; i < plugin.getMULTIPLE_ACHIEVEMENTS().length; i++)
			if (!multipleAchievementTypesLanguage[i].equals("")) {
				for (String section : plugin.getConfig().getConfigurationSection(plugin.getMULTIPLE_ACHIEVEMENTS()[i])
						.getKeys(false))
					for (String ach : plugin.getConfig()
							.getConfigurationSection(plugin.getMULTIPLE_ACHIEVEMENTS()[i] + "." + section)
							.getKeys(false))
						if (plugin.getDb()
								.hasPlayerAchievement(
										player,
										plugin.getConfig().getString(
												plugin.getMULTIPLE_ACHIEVEMENTS()[i] + "." + section + "." + ach
														+ ".Name", ""))) {
							numberInCategory++;
							addHoverLine(
									achievementsList,
									"&f"
											+ plugin.getConfig().getString(
													plugin.getMULTIPLE_ACHIEVEMENTS()[i] + "." + section + "." + ach
															+ ".Name", ""),
									ach,
									plugin.getReward().getRewardType(
											plugin.getMULTIPLE_ACHIEVEMENTS()[i] + "." + section + "." + ach));
						} else
							addHoverLine(
									achievementsList,
									"&8§o"
											+ plugin.getConfig()
													.getString(
															plugin.getMULTIPLE_ACHIEVEMENTS()[i] + "." + section + "."
																	+ ach + ".Name", "")
													.replaceAll("&([a-f]|[0-9]){1}", ""),
									ach,
									plugin.getReward().getRewardType(
											plugin.getMULTIPLE_ACHIEVEMENTS()[i] + "." + section + "." + ach));
				if (achievementsList.length() > 0 && (numberInCategory != 0 || !hideNotReceivedCategories))
					sendJsonHoverMessage(player, " &7" + plugin.getIcon() + " " + multipleAchievementTypesLanguage[i]
							+ " " + " &7" + plugin.getIcon() + " ",
							achievementsList.substring(0, achievementsList.length() - 1));
				achievementsList.setLength(0);
				numberInCategory = 0;
			}

		// Build distance achievements list item.
		if (!Lang.LIST_DISTANCE.toString().equals("")) {
			for (String distanceType : plugin.getDISTANCE_ACHIEVEMENTS())
				for (String ach : plugin.getConfig().getConfigurationSection(distanceType).getKeys(false))
					if (plugin.getDb().hasPlayerAchievement(player,
							plugin.getConfig().getString(distanceType + "." + ach + ".Name", ""))) {
						numberInCategory++;
						addHoverLine(achievementsList,
								"&f" + plugin.getConfig().getString(distanceType + "." + ach + ".Name", ""), ach,
								plugin.getReward().getRewardType(distanceType + "." + ach));
					} else
						addHoverLine(achievementsList,
								"&8§o"
										+ plugin.getConfig().getString(distanceType + "." + ach + ".Name", "")
												.replaceAll("&([a-f]|[0-9]){1}", ""), ach, plugin.getReward()
										.getRewardType(distanceType + "." + ach));

			if (achievementsList.length() > 0 && (numberInCategory != 0 || !hideNotReceivedCategories))
				sendJsonHoverMessage(player,
						" &7" + plugin.getIcon() + " " + Lang.LIST_DISTANCE + " " + " &7" + plugin.getIcon() + " ",
						achievementsList.substring(0, achievementsList.length() - 1));
			achievementsList.setLength(0);
			numberInCategory = 0;
		}

		// Build list of normal achievements.
		for (int i = 0; i < plugin.getNORMAL_ACHIEVEMENTS().length; i++)
			if (!normalAchievementTypesLanguage[i].equals("")) {
				for (String ach : plugin.getConfig().getConfigurationSection(plugin.getNORMAL_ACHIEVEMENTS()[i])
						.getKeys(false))
					if (plugin.getDb().hasPlayerAchievement(player,
							plugin.getConfig().getString(plugin.getNORMAL_ACHIEVEMENTS()[i] + "." + ach + ".Name", ""))) {
						numberInCategory++;
						addHoverLine(
								achievementsList,
								"&f"
										+ plugin.getConfig().getString(
												plugin.getNORMAL_ACHIEVEMENTS()[i] + "." + ach + ".Name", ""), ach,
								plugin.getReward().getRewardType(plugin.getNORMAL_ACHIEVEMENTS()[i] + "." + ach));
					} else
						addHoverLine(
								achievementsList,
								"&8§o"
										+ plugin.getConfig()
												.getString(plugin.getNORMAL_ACHIEVEMENTS()[i] + "." + ach + ".Name", "")
												.replaceAll("&([a-f]|[0-9]){1}", ""), ach, plugin.getReward()
										.getRewardType(plugin.getNORMAL_ACHIEVEMENTS()[i] + "." + ach));
				if (achievementsList.length() > 0 && (numberInCategory != 0 || !hideNotReceivedCategories))
					sendJsonHoverMessage(player, " &7" + plugin.getIcon() + " " + normalAchievementTypesLanguage[i]
							+ " " + " &7" + plugin.getIcon() + " ",
							achievementsList.substring(0, achievementsList.length() - 1));
				achievementsList.setLength(0);
				numberInCategory = 0;
			}
	}

	/**
	 * Send a packet message to the server in order to display a hover. Parts of
	 * this method were extracted from ELCHILEN0's AutoMessage plugin, under MIT
	 * license (http://dev.bukkit.org/bukkit-plugins/automessage/). Thanks for
	 * his help on this matter.
	 */
	public void sendJsonHoverMessage(Player player, String message, String hover) {

		// Build the json format string.
		String json = "{text:\"" + message + "\",hoverEvent:{action:show_text,value:[{text:\"" + hover
				+ "\",color:blue}]}}";

		try {
			PacketSender.sendChatPacket(player, json);
		} catch (Exception ex) {

			plugin.getLogger().severe(
					"Errors while trying to display hovers in /aach list command. Is your server up-to-date?");
			ex.printStackTrace();
		}
	}

	/**
	 * Add an achievement line to the current hover box.
	 */
	public StringBuilder addHoverLine(StringBuilder currentString, String name, String level, String reward) {

		if (obfuscateNotReceived)
			name = name.replace("&8§o", "&8§k");

		if (reward != "")
			currentString.append(name + " - " + Lang.LIST_AMOUNT + " " + level + " - " + Lang.LIST_REWARD + " "
					+ reward + "\n");
		else
			currentString.append(name + " - " + Lang.LIST_AMOUNT + " " + level + "\n");

		return currentString;

	}

}
