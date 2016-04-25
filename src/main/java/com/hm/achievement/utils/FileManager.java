package com.hm.achievement.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;

import com.hm.achievement.AdvancedAchievements;

public class FileManager {

	private AdvancedAchievements plugin;

	public FileManager(AdvancedAchievements plugin) {
		this.plugin = plugin;
	}

	public YamlManager getNewConfig(String fileName) throws IOException {

		File file = this.getConfigFile(fileName);

		if (!file.exists()) {
			this.prepareFile(fileName);
		}

		YamlManager yamlManager = new YamlManager(this.getConfigContent(fileName), file, this.getCommentsAmount(file),
				plugin);
		return yamlManager;
	}

	/**
	 * Retrieve a configuration file with its name.
	 */
	private File getConfigFile(String file) {

		if (file.isEmpty() || file == null)
			return null;

		File configFile;

		if (file.contains("/")) {
			if (file.startsWith("/"))
				configFile = new File(plugin.getDataFolder() + file.replace("/", File.separator));
			else
				configFile = new File(plugin.getDataFolder() + File.separator + file.replace("/", File.separator));
		} else
			configFile = new File(plugin.getDataFolder(), file);

		return configFile;
	}

	/**
	 * Create file.
	 */
	public void prepareFile(String resource) throws IOException {

		File file = this.getConfigFile(resource);

		if (file.exists())
			return;

		file.getParentFile().mkdirs();
		file.createNewFile();

		if (resource != null)
			if (!resource.isEmpty())
				this.copyResource(plugin.getResource(resource), file);
	}

	/**
	 * Extract the configuration from the file and rework it in order to provide
	 * a workaround to save comments.
	 */
	public Reader getConfigContent(File file) throws IOException {

		if (!file.exists())
			return null;
		int commentNum = 0;

		String addLine;
		String currentLine;
		String pluginName = this.getPluginName();

		StringBuilder whole = new StringBuilder("");
		BufferedReader reader = new BufferedReader(new FileReader(file));

		while ((currentLine = reader.readLine()) != null) {
			// Rework comment line so it becomes a normal value in the
			// config file.
			// This workaround allows the comment to be saved in the Yaml
			// file.
			if (currentLine.startsWith("#")) {
				addLine = currentLine.replace(":", "_COLON_").replace("|", "_VERT_").replace("-", "_HYPHEN_")
						.replaceFirst("#", pluginName + "_COMMENT_" + commentNum + ": ");
				whole.append(addLine + "\n");
				commentNum++;
			} else
				whole.append(currentLine + "\n");
		}

		String config = whole.toString();
		StringReader configStream = new StringReader(config);

		reader.close();
		return configStream;
	}

	/**
	 * Return the total number of comments in the file.
	 */
	private int getCommentsAmount(File file) throws IOException {

		if (!file.exists())
			return 0;
		int comments = 0;
		String currentLine;

		BufferedReader reader = new BufferedReader(new FileReader(file));

		while ((currentLine = reader.readLine()) != null)
			if (currentLine.startsWith("#"))
				comments++;

		reader.close();
		return comments;
	}

	public Reader getConfigContent(String filePath) throws IOException {

		return this.getConfigContent(this.getConfigFile(filePath));
	}

	/**
	 * Rework the configuration string in order to provide a workaround to save
	 * comments.
	 */
	private String prepareConfigString(String configString) {

		int lastLine = 0;

		String[] lines = configString.split("\n");
		StringBuilder config = new StringBuilder("");

		for (String line : lines) {
			// Rework comment line so it is converted back to a normal comment.
			if (line.startsWith(this.getPluginName() + "_COMMENT")) {
				String comment = ("#" + line.trim().substring(line.indexOf(": ") + 1)).replace("_COLON_", ":")
						.replace("_HYPHEN_", "-").replace("_VERT_", "|");

				String normalComment;
				if (comment.startsWith("# ' "))
					normalComment = comment.substring(0, comment.length() - 1).replaceFirst("# ' ", "# ");
				else
					normalComment = comment;

				if (lastLine == 0)
					config.append(normalComment + "\n");
				else if (lastLine == 1)
					config.append("\n" + normalComment + "\n");

				lastLine = 0;
			} else {
				config.append(line + "\n");
				lastLine = 1;
			}
		}
		return config.toString();
	}

	/**
	 * Write a string into a file.
	 */
	public void saveConfig(String configString, File file) throws IOException {

		String configuration = this.prepareConfigString(configString);

		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(configuration);
		writer.flush();
		writer.close();

	}

	public String getPluginName() {

		return plugin.getDescription().getName();
	}

	/**
	 * Write a resource represented by an input stream into a file.
	 */
	private void copyResource(InputStream resource, File file) throws IOException {

		OutputStream out = new FileOutputStream(file);

		int length;
		byte[] buf = new byte[1024];

		while ((length = resource.read(buf)) > 0)
			out.write(buf, 0, length);

		out.close();
		resource.close();
	}

	/**
	 * Perform a backup of a file contained in the plugin's data folder; the
	 * backup simply has a .bak extension.
	 */
	public void backupFile(String name) throws IOException {

		File original = new File(plugin.getDataFolder(), name);
		File backup = new File(plugin.getDataFolder(), name + ".bak");

		// Do a backup only if a newer version of the file exists.
		if (original.lastModified() > backup.lastModified() && original.exists()) {

			FileInputStream inStream = new FileInputStream(original);
			FileOutputStream outStream;
			outStream = new FileOutputStream(backup);

			byte[] buffer = new byte[1024];

			int length;
			while ((length = inStream.read(buffer)) > 0) {
				outStream.write(buffer, 0, length);
			}

			if (inStream != null)
				inStream.close();
			if (outStream != null)
				outStream.close();
		}

	}

}
