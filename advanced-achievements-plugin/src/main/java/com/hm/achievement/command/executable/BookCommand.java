package com.hm.achievement.command.executable;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.data.AwardedDBAchievement;
import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.lifecycle.Cleanable;
import com.hm.achievement.listener.QuitListener;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import com.hm.mcshared.particle.ParticleEffect;
import com.hm.mcshared.particle.ReflectionUtils.PackageType;

/**
 * Class in charge of handling the /aach book command, which creates and gives a book containing the player's
 * achievements.
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "book", permission = "book", minArgs = 1, maxArgs = 1)
public class BookCommand extends AbstractCommand implements Cleanable {

	// Strings related to Reflection.
	private static final String PACKAGE_INVENTORY = "inventory";
	private static final String PACKAGE_UTIL = "util";
	private static final String CLASS_CRAFT_META_BOOK = "CraftMetaBook";
	private static final String CLASS_CRAFT_CHAT_MESSAGE = "CraftChatMessage";
	private static final String FIELD_PAGES = "pages";
	private static final String METHOD_FROM_STRING = "fromString";

	// Corresponds to times at which players have received their books. Cooldown structure.
	private final HashMap<UUID, Long> playersBookTime = new HashMap<>();
	private final Logger logger;
	private final int serverVersion;
	private final AbstractDatabaseManager sqlDatabaseManager;

	private int configTimeBook;
	private String configBookSeparator;
	private boolean configAdditionalEffects;
	private boolean configSound;
	private String langBookDelay;
	private String langBookNotReceived;
	private String langBookDate;
	private String langBookName;
	private String langBookReceived;
	private DateFormat dateFormat;

	@Inject
	public BookCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, ReloadCommand reloadCommand,
			Logger logger, int serverVersion, AbstractDatabaseManager sqlDatabaseManager, QuitListener quitListener) {
		super(mainConfig, langConfig, pluginHeader, reloadCommand);
		this.logger = logger;
		this.serverVersion = serverVersion;
		this.sqlDatabaseManager = sqlDatabaseManager;
		quitListener.addObserver(this);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configTimeBook = mainConfig.getInt("TimeBook", 0) * 1000;
		configBookSeparator = "\n&r" + mainConfig.getString("BookSeparator", "") + "\n&r";
		configAdditionalEffects = mainConfig.getBoolean("AdditionalEffects", true);
		configSound = mainConfig.getBoolean("Sound", true);

		langBookDelay = pluginHeader + LangHelper.getReplacedOnce(CmdLang.BOOK_DELAY, "TIME",
				Integer.toString(configTimeBook / 1000), langConfig);
		langBookNotReceived = pluginHeader + LangHelper.get(CmdLang.BOOK_NOT_RECEIVED, langConfig);
		langBookDate = translateColorCodes("&8" + LangHelper.get(CmdLang.BOOK_DATE, langConfig));
		langBookName = LangHelper.get(CmdLang.BOOK_NAME, langConfig);
		langBookReceived = pluginHeader + LangHelper.get(CmdLang.BOOK_RECEIVED, langConfig);

		String localeString = mainConfig.getString("DateLocale", "en");
		dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, new Locale(localeString));
	}

	@Override
	public void cleanPlayerData(UUID uuid) {
		playersBookTime.remove(uuid);
	}

	@Override
	void onExecute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;

		if (!isInCooldownPeriod(player)) {
			List<AwardedDBAchievement> playerAchievementsList = sqlDatabaseManager
					.getPlayerAchievementsList(player.getUniqueId());
			if (playerAchievementsList.isEmpty()) {
				player.sendMessage(langBookNotReceived);
				return;
			}
			// Play special particle effect when receiving the book.
			if (configAdditionalEffects) {
				try {
					ParticleEffect.ENCHANTMENT_TABLE.display(0, 2, 0, 1, 1000, player.getLocation(), 100);
				} catch (Exception e) {
					logger.warning("Failed to display additional particle effects for books.");
				}
			}

			// Play special sound when receiving the book.
			if (configSound) {
				// If old version, retrieving sound by name as it no longer exists in newer versions.
				Sound sound = serverVersion < 9 ? Sound.valueOf("LEVEL_UP") : Sound.ENTITY_PLAYER_LEVELUP;
				player.getWorld().playSound(player.getLocation(), sound, 1.0f, 0.0f);
			}

			fillBook(playerAchievementsList, player);
		} else {
			player.sendMessage(langBookDelay);
		}
	}

	/**
	 * Constructs the pages of a book.
	 *
	 * @param achievements
	 * @param player
	 */
	private void fillBook(List<AwardedDBAchievement> achievements, Player player) {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		List<String> bookPages = new ArrayList<>(achievements.size());
		BookMeta bookMeta = (BookMeta) book.getItemMeta();

		for (AwardedDBAchievement achievement : achievements) {
			String currentAchievement = "&0" + achievement.getName() + configBookSeparator + achievement.getMessage()
					+ configBookSeparator + achievement.getFormattedDate();
			currentAchievement = translateColorCodes(currentAchievement);
			bookPages.add(currentAchievement);
		}

		// Set the pages and other elements of the book (author, title and date of reception).
		setBookPages(bookPages, bookMeta);
		bookMeta.setAuthor(player.getName());
		bookMeta.setTitle(langBookName);
		bookMeta.setLore(
				Arrays.asList(StringUtils.replaceOnce(langBookDate, "DATE", dateFormat.format(System.currentTimeMillis()))));
		book.setItemMeta(bookMeta);

		// Check whether player has room in his inventory, else drop book on the ground.
		if (player.getInventory().firstEmpty() != -1) {
			player.getInventory().addItem(book);
		} else {
			player.getWorld().dropItem(player.getLocation(), book);
		}
		player.sendMessage(langBookReceived);
	}

	/**
	 * Checks if player hasn't done a command too recently (with "too recently" being defined in configuration file).
	 *
	 * @param player
	 * @return whether a player is authorised to perform the list command
	 */
	private boolean isInCooldownPeriod(Player player) {
		// Player bypasses cooldown if he has full plugin permissions.
		if (player.hasPermission("achievement.*") || configTimeBook == 0) {
			return false;
		}
		long currentTime = System.currentTimeMillis();
		Long lastListTime = playersBookTime.get(player.getUniqueId());
		if (lastListTime == null || currentTime - lastListTime > configTimeBook) {
			playersBookTime.put(player.getUniqueId(), currentTime);
			return false;
		}
		return true;
	}

	/**
	 * Adds pages to the BookMeta. A Spigot commit in the late days of Minecraft 1.11.2 started enforcing extremely low
	 * limits (why? If it's not broken, don't fix it.), with books limited in page size and total number of pages, as
	 * well as title length. This function bypasses such limits and restores the original CraftBukkit behaviour. See
	 * https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/commits/4acd0f49e07e0912096e79494472535baf0db2ab
	 * for more information.
	 *
	 * @param bookPages
	 * @param bookMeta
	 */
	@SuppressWarnings("unchecked")
	private void setBookPages(List<String> bookPages, BookMeta bookMeta) {
		if (serverVersion >= 11) {
			try {
				// Code we're trying to execute: this.pages.add(CraftChatMessage.fromString(page, true)[0]); in
				// CraftMetaBook.java.
				Class<?> craftMetaBookClass = PackageType.CRAFTBUKKIT
						.getClass(PACKAGE_INVENTORY + "." + CLASS_CRAFT_META_BOOK);
				List<Object> pages = (List<Object>) craftMetaBookClass.getField(FIELD_PAGES)
						.get(craftMetaBookClass.cast(bookMeta));
				Method fromStringMethod = PackageType.CRAFTBUKKIT.getClass(PACKAGE_UTIL + "." + CLASS_CRAFT_CHAT_MESSAGE)
						.getMethod(METHOD_FROM_STRING, String.class, boolean.class);
				for (String bookPage : bookPages) {
					pages.add(((Object[]) fromStringMethod.invoke(null, bookPage, true))[0]);
				}
			} catch (Exception e) {
				logger.warning("Error while creating book pages. Your achievements book may be trimmed down to 50 pages.");
				bookMeta.setPages(bookPages);
			}
		} else {
			bookMeta.setPages(bookPages);
		}
	}
}
