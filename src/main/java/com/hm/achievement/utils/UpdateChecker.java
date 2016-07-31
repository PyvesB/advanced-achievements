package com.hm.achievement.utils;

import java.io.InputStream;
import java.net.URL;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class used to check for newer versions of the plugin.
 * 
 * @author Pyves
 */
public class UpdateChecker {

	private AdvancedAchievements plugin;
	private String version;
	private boolean updateNeeded;

	// Address of the rss feed to retrieve most recent version number.
	private static final String BUKKIT_URL = "https://dev.bukkit.org/bukkit-plugins/advanced-achievements/files.rss";
	// Alternative GitHub address, where version is in pom.xml.
	private static final String GITHUB_URL = "https://raw.githubusercontent.com/PyvesB/AdvancedAchievements/master/pom.xml";

	// Addresses of the project's download pages.
	public static final String BUKKIT_DONWLOAD_URL = "- dev.bukkit.org/bukkit-plugins/advanced-achievements";
	public static final String SPIGOT_DONWLOAD_URL = "- spigotmc.org/resources/advanced-achievements.6239";

	public UpdateChecker(AdvancedAchievements plugin) {

		this.plugin = plugin;
		updateNeeded = checkForUpdateBukkit();
	}

	/**
	 * Check if a new version of AdvancedAchievements is available, and log in console if new version found.
	 */
	private boolean checkForUpdateBukkit() {

		URL filesFeed = null;
		Document document = null;
		boolean bukkit = true;

		try {
			filesFeed = new URL(BUKKIT_URL);
			InputStream input = filesFeed.openConnection().getInputStream();
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
		} catch (Exception eB) {
			try {
				// If XML parsing for Bukkit has failed (website down, address change, etc.), try on GitHub.
				bukkit = false;
				filesFeed = new URL(GITHUB_URL);
				InputStream input = filesFeed.openConnection().getInputStream();
				document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
			} catch (Exception eG) {
				plugin.getLogger().severe("Error while checking for AdvancedAchievements update.");
				plugin.setSuccessfulLoad(false);
				return false;
			}
		}

		// Retrieve version information depending on whether Bukkit or GitHub was queried.
		if (bukkit) {
			Node latestFile = document.getElementsByTagName("item").item(0);
			NodeList children = latestFile.getChildNodes();

			version = children.item(1).getTextContent().replaceAll("[a-zA-Z ]", "");
		} else {
			version = document.getElementsByTagName("version").item(0).getTextContent();
		}

		if (version.equals(plugin.getDescription().getVersion()))
			return false;

		// Version of current plugin.
		String[] pluginVersion = plugin.getDescription().getVersion().split("\\.");

		// Version of Bukkit's latest file.
		String[] onlineVersion = version.split("\\.");

		// Compare version numbers.
		for (int i = 0; i < Math.min(pluginVersion.length, onlineVersion.length); i++) {
			if (Integer.parseInt(pluginVersion[i]) > Integer.parseInt(onlineVersion[i]))
				return false;
			else if (Integer.parseInt(pluginVersion[i]) < Integer.parseInt(onlineVersion[i])) {
				logUpdate();
				return true;
			}
		}

		// Additional check (for instance pluginVersion = 2.2 and onlineVersion = 2.2.1).
		if (pluginVersion.length < onlineVersion.length) {
			logUpdate();
			return true;
		}

		return false;
	}

	private void logUpdate() {

		plugin.getLogger().warning("Update available: v" + version + ". Download at one of the following:");
		plugin.getLogger().warning(BUKKIT_DONWLOAD_URL);
		plugin.getLogger().warning(SPIGOT_DONWLOAD_URL);
	}

	public String getVersion() {

		return version;
	}

	public boolean isUpdateNeeded() {

		return updateNeeded;
	}

}
