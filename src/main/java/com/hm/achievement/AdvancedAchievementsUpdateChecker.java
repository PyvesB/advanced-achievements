package com.hm.achievement;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AdvancedAchievementsUpdateChecker {

	private AdvancedAchievements plugin;
	private String version;
	private boolean updateNeeded;

	// Address of the rss feed to retrieve most recent version number.
	private static final String URL = "http://dev.bukkit.org/bukkit-plugins/advanced-achievements/files.rss";

	// Addresses of the project's download pages.
	public static final String BUKKIT_DONWLOAD_URL = "- dev.bukkit.org/bukkit-plugins/advanced-achievements";
	public static final String SPIGOT_DONWLOAD_URL = "- spigotmc.org/resources/advanced-achievements.6239";

	public AdvancedAchievementsUpdateChecker(AdvancedAchievements plugin) {

		this.plugin = plugin;
		updateNeeded = checkForUpdate();

	}

	/**
	 * Check if a new version of AdvancedAchievements is available, and log in
	 * console if new version found.
	 */
	private boolean checkForUpdate() {

		URL filesFeed = null;

		try {
			filesFeed = new URL(URL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		try {
			InputStream input = filesFeed.openConnection().getInputStream();
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);

			Node latestFile = document.getElementsByTagName("item").item(0);
			NodeList children = latestFile.getChildNodes();

			version = children.item(1).getTextContent().replaceAll("[a-zA-Z ]", "");

			if (version.equals(plugin.getDescription().getVersion()))
				return false;

			else {
				plugin.getLogger().info("Update available for Advanced Achievements: v" + version
						+ ". Download at one of the following locations:");
				plugin.getLogger().info(BUKKIT_DONWLOAD_URL);
				plugin.getLogger().info(SPIGOT_DONWLOAD_URL);
				return true;
			}

		} catch (Exception e) {

			plugin.getLogger().severe("Error while checking for AdvancedAchievements update.");
			plugin.setSuccessfulLoad(false);
		}
		return false;

	}

	public String getVersion() {

		return version;
	}

	public boolean isUpdateNeeded() {

		return updateNeeded;
	}

}
