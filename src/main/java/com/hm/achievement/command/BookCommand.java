package com.hm.achievement.command;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.hm.achievement.AdvancedAchievements;
import com.hm.mcshared.particle.ParticleEffect;

/**
 * Class in charge of handling the /aach book command, which creates and gives a book containing the player's
 * achievements.
 * 
 * @author Pyves
 */
public class BookCommand extends AbstractCommand {

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

		langBookDelay = plugin.getChatHeader() + StringUtils.replaceOnce(
				plugin.getPluginLang().getString("book-delay",
						"You must wait TIME seconds between each book reception!"),
				"TIME", Integer.toString(configTimeBook / 1000));
		langBookNotReceived = plugin.getChatHeader()
				+ plugin.getPluginLang().getString("book-not-received", "You have not yet received any achievements.");
		langBookDate = ChatColor.translateAlternateColorCodes('&',
				"&8" + plugin.getPluginLang().getString("book-date", "Book created on DATE."));
		langBookName = plugin.getPluginLang().getString("book-name", "Achievements Book");
		langBookReceived = plugin.getChatHeader()
				+ plugin.getPluginLang().getString("book-received", "You received your achievements book!");

		String localeString = plugin.getPluginConfig().getString("DateLocale", "en");
		dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, new Locale(localeString));
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
				Sound sound;
				if (version < 9) {
					// Old enum for versions prior to Minecraft 1.9. Retrieving it by name as it does no longer exist in
					// newer versions.
					sound = Sound.valueOf("LEVEL_UP");
				} else {
					// Play sound with enum for newer versions.
					sound = Sound.ENTITY_PLAYER_LEVELUP;
				}
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
	private void fillBook(List<String> achievements, Player player) {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		List<String> pages = new ArrayList<>(achievements.size() / 3);
		BookMeta bm = (BookMeta) book.getItemMeta();

		try {
			// Elements in the array go by groups of 3: name, description and date.
			for (int i = 0; i < achievements.size(); i += 3) {
				String currentAchievement = "&0" + achievements.get(i) + configBookSeparator + achievements.get(i + 1)
						+ configBookSeparator + achievements.get(i + 2);
				currentAchievement = ChatColor.translateAlternateColorCodes('&', currentAchievement);
				pages.add(currentAchievement);
			}
		} catch (Exception e) {
			// Catch runtime exception (for instance ArrayIndexOutOfBoundsException); this should not happen unless
			// something went wrong in the database.
			plugin.getLogger().severe("Error while creating pages of book.");
		}

		if (pages.isEmpty()) {
			player.sendMessage(langBookNotReceived);
			return;
		}

		// Set the pages and other elements of the book (author, title and date of reception).
		bm.setPages(pages);
		bm.setAuthor(player.getName());
		bm.setTitle(langBookName);
		bm.setLore(Arrays.asList(StringUtils.replaceOnce(langBookDate, "DATE", dateFormat.format(new Date()))));

		book.setItemMeta(bm);

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

	public Map<String, Long> getPlayersBookTime() {
		return playersBookTime;
	}
}
