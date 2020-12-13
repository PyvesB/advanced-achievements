package com.hm.achievement.command.executable;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.gui.CategoryGUI;
import com.hm.achievement.gui.GUIItems;
import com.hm.achievement.gui.MainGUI;
import com.hm.achievement.gui.OrderedCategory;
import com.hm.achievement.utils.StringHelper;

/**
 * Class in charge of handling the /aach list command, which displays interactive GUIs.
 * 
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "list", permission = "list", minArgs = 1, maxArgs = 2)
public class ListCommand extends AbstractCommand {

	private final MainGUI mainGUI;
	private final CategoryGUI categoryGUI;
	private final GUIItems guiItems;

	private String langCategoryDoesNotExist;

	@Inject
	public ListCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig,
			StringBuilder pluginHeader, MainGUI mainGUI, CategoryGUI categoryGUI, GUIItems guiItems) {
		super(mainConfig, langConfig, pluginHeader);
		this.mainGUI = mainGUI;
		this.categoryGUI = categoryGUI;
		this.guiItems = guiItems;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langCategoryDoesNotExist = pluginHeader + langConfig.getString("category-does-not-exist");
	}

	@Override
	void onExecute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;

		if (player.isSleeping()) {
			sender.sendMessage(langConfig.getString("list-unavailable-whilst-sleeping"));
			return;
		}

		if (args.length == 1) {
			mainGUI.displayMainGUI(player);
		} else {
			String categoryName = args[1];
			Optional<Entry<OrderedCategory, ItemStack>> matchingCategory = guiItems.getOrderedAchievementItems().entrySet()
					.stream()
					.filter(e -> e.getKey().getCategory().toString().equals(categoryName))
					.findFirst();
			if (matchingCategory.isPresent()) {
				categoryGUI.displayCategoryGUI(matchingCategory.get().getValue(), player, 0);
			} else {
				List<String> allGUICategoryNames = guiItems.getOrderedAchievementItems().keySet()
						.stream()
						.map(c -> c.getCategory().toString())
						.collect(Collectors.toList());
				sender.sendMessage(StringUtils.replaceEach(langCategoryDoesNotExist, new String[] { "CAT", "CLOSEST_MATCH" },
						new String[] { categoryName, StringHelper.getClosestMatch(categoryName, allGUICategoryNames) }));
			}
		}
	}
}
