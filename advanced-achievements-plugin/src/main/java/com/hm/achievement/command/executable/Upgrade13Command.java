package com.hm.achievement.command.executable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.DatabaseUpdater;

/**
 * Class in charge of handling the /aach upgrade13 command, which updates some of the database tables to use new
 * Minecraft 1.13 Material names.
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "upgrade13", permission = "*", minArgs = 1, maxArgs = 1)
public class Upgrade13Command extends AbstractCommand {

	private final AbstractDatabaseManager databaseManager;
	private final DatabaseUpdater databaseUpdater;

	private int configTableMaxSizeOfGroupedSubcategories;

	@Inject
	public Upgrade13Command(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig,
			StringBuilder pluginHeader, AbstractDatabaseManager databaseManager, DatabaseUpdater databaseUpdater) {
		super(mainConfig, langConfig, pluginHeader);
		this.databaseManager = databaseManager;
		this.databaseUpdater = databaseUpdater;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configTableMaxSizeOfGroupedSubcategories = mainConfig.getInt("TableMaxSizeOfGroupedSubcategories");
	}

	@Override
	void onExecute(CommandSender sender, String[] args) {
		databaseUpdater.updateOldMaterialsToNewOnes(databaseManager, configTableMaxSizeOfGroupedSubcategories);
		sender.sendMessage(pluginHeader +
				"Database upgrade to Minecraft 1.13 completed for the Crafts, Places and Breaks categories.");
	}

}
