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
import org.xml.sax.SAXException;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class used to check for newer versions of the plugin.
 * 
 * @author Pyves
 */
public class UpdateChecker {

	private final AdvancedAchievements plugin;
	private final FutureTask<Boolean> updateCheckerFutureTask;

	private Boolean updateNeeded = null;
	// Marked as volatile to ensure that once the updateCheckerFutureTask is done, the version is visible to the main
	// thread of execution.
	private volatile String version;

	// GitHub address, where version is in pom.xml.
	private static final String GITHUB_URL = "https://raw.githubusercontent.com/PyvesB/AdvancedAchievements/master/pom.xml";

	// Addresses of the project's download pages.
	public static final String BUKKIT_DONWLOAD_URL = "- dev.bukkit.org/bukkit-plugins/advanced-achievements/files";
	public static final String SPIGOT_DONWLOAD_URL = "- spigotmc.org/resources/advanced-achievements.6239";

	public UpdateChecker(AdvancedAchievements plugin) {

		this.plugin = plugin;
		updateCheckerFutureTask = new FutureTask<>(new Callable<Boolean>() {

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

		Document document = null;

		URL filesFeed = new URL(GITHUB_URL);
		try (InputStream inputGithub = filesFeed.openConnection().getInputStream()) {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputGithub);
		}

		// Retrieve version information.
		version = document.getElementsByTagName("version").item(0).getTextContent();

		if (version.equals(plugin.getDescription().getVersion())) {
			return false;
		}
		// Version of current plugin.
		String[] pluginVersion = plugin.getDescription().getVersion().split("\\.");

		// Version of Github's latest file.
		String[] onlineVersion = version.split("\\.");

		// Compare version numbers.
		for (int i = 0; i < Math.min(pluginVersion.length, onlineVersion.length); i++) {
			if (Integer.parseInt(pluginVersion[i]) > Integer.parseInt(onlineVersion[i])) {
				return false;
			} else if (Integer.parseInt(pluginVersion[i]) < Integer.parseInt(onlineVersion[i])) {
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

		plugin.getLogger().warning("Update available: v" + version + "! Download at one of the following locations:");
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
					plugin.getLogger().severe("Error while checking for Advanced Achievements update.");
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
