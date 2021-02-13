package com.hm.achievement.listener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class used to check for newer versions of the plugin by retrieving a version tag in its pom.xml file. For instance
 * versions 2.0, 1.2 and 1.1.1 are all considered newer than 1.1.
 * <p>
 * If a newer version is available, the information will be logged in the server's console and a message will be sent
 * when players with a specific permission join the game.
 * 
 * @author Pyves
 */
public class UpdateChecker implements Listener {

	private final AdvancedAchievements plugin;
	private final StringBuilder pluginHeader;

	// Marked as volatile to ensure that once the updateCheckerFutureTask is done, the version is visible to the main
	// thread of execution.
	private volatile String version;

	private Boolean updateNeeded = null;
	private FutureTask<Boolean> updateCheckerFutureTask;

	@Inject
	public UpdateChecker(AdvancedAchievements plugin, StringBuilder pluginHeader) {
		this.plugin = plugin;
		this.pluginHeader = pluginHeader;
	}

	/**
	 * Listens to PlayerJoinEvents and informs players with a specific permission that an update is available.
	 * 
	 * @param event Event sent to this listener method.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (isUpdateNeeded() && event.getPlayer().hasPermission("achievement.update")) {
			event.getPlayer().sendMessage(pluginHeader + plugin.getDescription().getName() + " update available: v"
					+ version + ". Download at spigotmc.org/resources/advanced-achievements.83466");
		}
	}

	/**
	 * Launches a new thread to asynchronously check whether an update is available.
	 */
	public void launchUpdateCheckerTask() {
		updateCheckerFutureTask = new FutureTask<>(() -> checkForUpdate());
		// Run the FutureTask in a new thread.
		new Thread(updateCheckerFutureTask).start();
	}

	/**
	 * Returns whether the plugin needs to be updated. Will always return false if launchUpdateCheckerTask has not yet
	 * been called or the asynchronous call has not yet completed.
	 * 
	 * @return true is an update is available, false otherwise.
	 */
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
					plugin.getLogger()
							.severe("Error while checking for " + plugin.getDescription().getName() + " update.");
				}
			} else {
				// FutureTask not yet completed; indicate that no updates are available. If an OP joins before the task
				// completes, he will not be notified; this is both an unlikely and non critical scenario.
				return false;
			}
		}
		return updateNeeded;
	}

	/**
	 * Returns the version tag of the plugin parsed from the specified pom.xml. Will be null if launchUpdateCheckerTask
	 * has not yet been called or the asynchronous call has not yet completed.
	 * 
	 * @return The version tag of the plugin parsed from the specified pom.xml.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Checks if a new version of the plugin is available, and logs in the server's console if a new version is found.
	 * 
	 * @return True if an update is available, false otherwise.
	 * 
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	private boolean checkForUpdate() throws SAXException, IOException, ParserConfigurationException {
		plugin.getLogger().info("Checking for plugin update...");

		Document document = null;

		URL filesFeed = new URL("https://raw.githubusercontent.com/PyvesB/advanced-achievements/master/pom.xml");
		try (InputStream pomInput = filesFeed.openConnection().getInputStream()) {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pomInput);
		}

		// Retrieve version information.
		version = document.getElementsByTagName("version").item(0).getTextContent();

		if (version.equals(plugin.getDescription().getVersion())) {
			return false;
		}
		// Version of current plugin.
		String[] pluginVersion = plugin.getDescription().getVersion().split("\\.");

		// Version of the pom.xml file.
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

		// Both versions have the same prefix; additional length check (for instance pluginVersion = 2.2 and
		// onlineVersion = 2.2.1).
		if (pluginVersion.length < onlineVersion.length) {
			logUpdate();
			return true;
		}
		return false;
	}

	/**
	 * Logs in the server's console if a new version is found. The new version number and download links are printed.
	 */
	private void logUpdate() {
		plugin.getLogger().warning("Update available: v" + version
				+ "! Download at spigotmc.org/resources/advanced-achievements.83466");
	}
}
