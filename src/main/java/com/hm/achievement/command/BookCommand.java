package com.hm.achievement.command;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.data.AwardedDBAchievement;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.utils.Cleanable;
import com.hm.mcshared.particle.ParticleEffect;
import com.hm.mcshared.particle.ReflectionUtils.PackageType;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.*;

/**
 * Class in charge of handling the /aach book command, which creates and gives a book containing the player's
 * achievements.
 * 
 * @author Pyves
 */
public class BookCommand extends AbstractCommand implements Cleanable {

	// Strings related to Reflection.
	private static final String PACKAGE_INVENTORY = "inventory";
	private static final String PACKAGE_UTIL = "util";
	private static final String CLASS_CRAFT_META_BOOK = "CraftMetaBook";
	private static final String CLASS_CRAFT_CHAT_MESSAGE = "CraftChatMessage";
	private static final String FIELD_PAGES = "pages";
	private static final String METHOD_FROM_STRING = "fromString";

	// Corresponds to times at which players have received their books. Cooldown structure.
	private final HashMap<String, Long> playersBookTime;

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

	public BookCommand(AdvancedAchievements plugin) {
		super(plugin);

		playersBookTime = new HashMap<>();
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configTimeBook = plugin.getPluginConfig().getInt("TimeBook", 0) * 1000;
		configBookSeparator = "\n&r" + plugin.getPluginConfig().getString("BookSeparator", "") + "\n&r";
		configAdditionalEffects = plugin.getPluginConfig().getBoolean("AdditionalEffects", true);
		configSound = plugin.getPluginConfig().getBoolean("Sound", true);

		langBookDelay = plugin.getChatHeader() + Lang.getReplacedOnce(CmdLang.BOOK_DELAY, 
				"TIME", Integer.toString(configTimeBook / 1000), plugin);
		langBookNotReceived = Lang.getWithChatHeader(CmdLang.BOOK_NOT_RECEIVED, plugin);
		langBookDate = ChatColor.translateAlternateColorCodes('&',
				"&8" + Lang.get(CmdLang.BOOK_DATE, plugin));
		langBookName = Lang.get(CmdLang.BOOK_NAME, plugin);
		langBookReceived = Lang.getWithChatHeader(CmdLang.BOOK_RECEIVED, plugin);

		String localeString = plugin.getPluginConfig().getString("DateLocale", "en");
		dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, new Locale(localeString));
	}

	@Override
	public void cleanPlayerData(UUID uuid) {
		playersBookTime.remove(uuid.toString());
	}

	@Override
	protected void executeCommand(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;

		if (!isInCooldownPeriod(player)) {
			// Play special particle effect when receiving the book.
			if (configAdditionalEffects) {
				try {
					ParticleEffect.ENCHANTMENT_TABLE.display(0, 2, 0, 1, 1000, player.getLocation(), 100);
				} catch (Exception e) {
					plugin.getLogger().severe("Error while displaying additional particle effects for books.");
				}
			}

			// Play special sound when receiving the book.
			if (configSound) {
				// If old version, retrieving sound by name as it no longer exists in newer versions.
				Sound sound = version < 9 ? Sound.valueOf("LEVEL_UP") : Sound.ENTITY_PLAYER_LEVELUP;
				player.getWorld().playSound(player.getLocation(), sound, 1.0f, 0.0f);
			}

			fillBook(plugin.getDatabaseManager().getPlayerAchievementsList(player.getUniqueId()), player);
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
			String currentAchievement = "&0" + achievement.getName() 
					+ configBookSeparator + achievement.getMessage()
					+ configBookSeparator + achievement.getFormattedDate();
			currentAchievement = translateColorCodes(currentAchievement);
			bookPages.add(currentAchievement);
		}

		if (bookPages.isEmpty()) {
			player.sendMessage(langBookNotReceived);
			return;
		}

		// Set the pages and other elements of the book (author, title and date of reception).
		setBookPages(bookPages, bookMeta);
		bookMeta.setAuthor(player.getName());
		bookMeta.setTitle(langBookName);
		bookMeta.setLore(Arrays
				.asList(StringUtils.replaceOnce(langBookDate, "DATE", dateFormat.format(System.currentTimeMillis()))));
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
		String uuid = player.getUniqueId().toString();
		Long lastListTime = playersBookTime.get(uuid);
		if (lastListTime == null || currentTime - lastListTime > configTimeBook) {
			playersBookTime.put(uuid, currentTime);
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
		if (version >= 11) {
			try {
				// Code we're trying to execute: this.pages.add(CraftChatMessage.fromString(page, true)[0]); in
				// CraftMetaBook.java.
				Class<?> craftMetaBookClass = PackageType.CRAFTBUKKIT
						.getClass(PACKAGE_INVENTORY + "." + CLASS_CRAFT_META_BOOK);
				List<Object> pages = (List<Object>) craftMetaBookClass.getField(FIELD_PAGES)
						.get(craftMetaBookClass.cast(bookMeta));
				Method fromStringMethod = PackageType.CRAFTBUKKIT
						.getClass(PACKAGE_UTIL + "." + CLASS_CRAFT_CHAT_MESSAGE)
						.getMethod(METHOD_FROM_STRING, String.class, boolean.class);
				for (String bookPage : bookPages) {
					pages.add(((Object[]) fromStringMethod.invoke(null, bookPage, true))[0]);
				}
			} catch (Exception e) {
				plugin.getLogger().warning(
						"Error while creating book pages. Your achievements book may be trimmed down to 50 pages.");
				bookMeta.setPages(bookPages);
			}
		} else {
			bookMeta.setPages(bookPages);
		}
	}
}
