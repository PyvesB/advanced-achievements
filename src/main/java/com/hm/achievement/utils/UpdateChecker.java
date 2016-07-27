package com.hm.achievement.utils;

import java.io.InputStream;
import java.net.MalformedURLException;
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
	private static final String URL = "http://dev.bukkit.org/bukkit-plugins/advanced-achievements/files.rss";

	// Addresses of the project's download pages.
	public static final String BUKKIT_DONWLOAD_URL = "- dev.bukkit.org/bukkit-plugins/advanced-achievements";
	public static final String SPIGOT_DONWLOAD_URL = "- spigotmc.org/resources/advanced-achievements.6239";

	public UpdateChecker(AdvancedAchievements plugin) {

		this.plugin = plugin;
		updateNeeded = checkForUpdate();

	}

	/**
	 * Check if a new version of AdvancedAchievements is available, and log in console if new version found.
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

		} catch (Exception e) {

			plugin.getLogger().severe("Error while checking for AdvancedAchievements update.");
			plugin.setSuccessfulLoad(false);
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
