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
	private URL filesFeed;
	private String version;
	private String url;

	public AdvancedAchievementsUpdateChecker(AdvancedAchievements plugin, String url) {

		this.plugin = plugin;

		try {
			this.filesFeed = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}

	public boolean updateNeeded() {

		try {
			InputStream input = this.filesFeed.openConnection()
					.getInputStream();
			Document document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(input);

			Node latestFile = document.getElementsByTagName("item").item(0);
			NodeList children = latestFile.getChildNodes();

			version = children.item(1).getTextContent()
					.replaceAll("[a-zA-Z ]", "");
			url = children.item(3).getTextContent();

			if (version.equals(plugin.getDescription().getVersion()))
				return false;

			else {
				plugin.getLogger().info(
						"Update available for Advanced Achievements: v" + version);
				plugin.getLogger().info("Download at: " + url);
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	public String getVersion() {
		return version;
	}

	public String getUrl() {
		return url;
	}

}

