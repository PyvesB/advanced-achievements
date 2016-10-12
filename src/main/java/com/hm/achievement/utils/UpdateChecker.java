package com.hm.achievement.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class used to check for newer versions of the plugin.
 * 
 * @author Pyves
 */
public class UpdateChecker {

	private AdvancedAchievements plugin;
	private Boolean updateNeeded = null;
	private FutureTask<Boolean> updateCheckerFutureTask;
	// Marked as volatile to ensure that once the updateCheckerFutureTask is done, the version is visible to the main
	// thread of execution.
	private volatile String version;

	// Address of the rss feed to retrieve most recent version number.
	private static final String BUKKIT_URL = "https://dev.bukkit.org/bukkit-plugins/advanced-achievements/files.rss";
	// Alternative GitHub address, where version is in pom.xml.
	private static final String GITHUB_URL = "https://raw.githubusercontent.com/PyvesB/AdvancedAchievements/master/pom.xml";

	// Addresses of the project's download pages.
	public static final String BUKKIT_DONWLOAD_URL = "- dev.bukkit.org/bukkit-plugins/advanced-achievements";
	public static final String SPIGOT_DONWLOAD_URL = "- spigotmc.org/resources/advanced-achievements.6239";

	public UpdateChecker(AdvancedAchievements plugin) {

		this.plugin = plugin;
		updateCheckerFutureTask = new FutureTask<Boolean>(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {

				return checkForUpdate();
			}
		});
		// Run the FutureTask in a new thread.
		new Thread(updateCheckerFutureTask).start();
	}

	/**
	 * Check if a new version of AdvancedAchievements is available, and log in console if new version found.
	 * 
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	private boolean checkForUpdate() throws SAXException, IOException, ParserConfigurationException {

		plugin.getLogger().info("Checking for plugin update...");

		URL filesFeed = new URL(BUKKIT_URL);
		Document document = null;
		boolean bukkit = true;

		try (InputStream inputBukkit = filesFeed.openConnection().getInputStream()) {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputBukkit);
		} catch (Exception eB) {
			// If XML parsing for Bukkit has failed (website down, address change, etc.), try on GitHub.
			bukkit = false;
			filesFeed = new URL(GITHUB_URL);
			try (InputStream inputGithub = filesFeed.openConnection().getInputStream()) {
				document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputGithub);
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

		// Completion of the FutureTask has not yet been checked.
		if (updateNeeded == null) {
			if (updateCheckerFutureTask.isDone()) {
				try {
					// Retrieve result of the FutureTask.
					updateNeeded = updateCheckerFutureTask.get();
				} catch (InterruptedException | ExecutionException e) {
					// Error during execution; assume that no updates are available.
					updateNeeded = false;
					plugin.getLogger().severe("Error while checking for AdvancedAchievements update.");
				}
			} else {
				// FutureTask not yet completed; indicate that no updates are available. If an OP joins before the task
				// completes, he will not be notified; this is both an unlikely and non critical scenario.
				return false;
			}
		}
		return updateNeeded;
	}

}
