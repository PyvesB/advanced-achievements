package com.hm.achievement.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class used to send the cached statistics to the database in an asynchronous manner.
 * 
 * @author Pyves
 *
 */
public class PooledRequestsSenderAsync implements Runnable {

	private AdvancedAchievements plugin;

	public PooledRequestsSenderAsync(AdvancedAchievements plugin, boolean init) {

		if (init == true)
			plugin.getPoolsManager().databasePoolsInit(plugin.isAsyncPooledRequestsSender());
		this.plugin = plugin;
	}

	@Override
	public void run() {

		sendRequests();

	}

	/**
	 * * Sends requests to the database to deal with regular events and prevent plugin from hitting server performance.
	 * Non event related categories (distances and play times) are not handled by pools.
	 * 
	 * Queries must not be batched because of race conditions; a database entry must first be updated, and if cached
	 * value in HashMap has not changed (by a listener running on main thread), it can be removed from the HashMap. Any
	 * thread doing a subsequent read in the database will retrieve the correct value.
	 * 
	 * Enumerations return elements reflecting the state of the hash table at some point at or since the creation of the
	 * enumeration, and therefore the (atomic) remove method can be used within them.
	 * 
	 * The cast operations are necessary to ensure compatibility with Java versions prior to Java 8 (Map interface did
	 * not support remove(key, value) before then).
	 * 
	 * PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT construct, which is available for
	 * PostgreSQL 9.5+.
	 */
	public void sendRequests() {

		try {
			Connection conn = plugin.getDb().getSQLConnection();
			Statement st = conn.createStatement();

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getDeathHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "deaths VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")" + " ON CONFLICT (playername) DO UPDATE SET (deaths)=("
							+ entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "deaths VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getDeathHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getArrowHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "arrows VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")" + " ON CONFLICT (playername) DO UPDATE SET (arrows)=("
							+ entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "arrows VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getArrowHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getSnowballHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "snowballs VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")" + " ON CONFLICT (playername) DO UPDATE SET (snowballs)=("
							+ entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "snowballs VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getSnowballHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getEggHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "eggs VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")" + " ON CONFLICT (playername) DO UPDATE SET (eggs)=("
							+ entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "eggs VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getEggHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getFishHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "fish VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")" + " ON CONFLICT (playername) DO UPDATE SET (fish)=("
							+ entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "fish VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getFishHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getItemBreakHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "itembreaks VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")"
							+ " ON CONFLICT (playername) DO UPDATE SET (itembreaks)=(" + entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "itembreaks VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getItemBreakHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getEatenItemsHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "eatenitems VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")"
							+ " ON CONFLICT (playername) DO UPDATE SET (eatenitems)=(" + entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "eatenitems VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getEatenItemsHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getShearHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "shears VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")" + " ON CONFLICT (playername) DO UPDATE SET (shears)=("
							+ entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "shears VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getShearHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getMilkHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "milks VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")" + " ON CONFLICT (playername) DO UPDATE SET (milks)=("
							+ entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "milks VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getMilkHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getTradeHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "trades VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")" + " ON CONFLICT (playername) DO UPDATE SET (trades)=("
							+ entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "trades VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getTradeHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getAnvilHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "anvils VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")" + " ON CONFLICT (playername) DO UPDATE SET (anvils)=("
							+ entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "anvils VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getAnvilHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getEnchantmentHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "enchantments VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")"
							+ " ON CONFLICT (playername) DO UPDATE SET (enchantments)=(" + entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "enchantments VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getEnchantmentHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getBedHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "beds VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")" + " ON CONFLICT (playername) DO UPDATE SET (beds)=("
							+ entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "beds VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getBedHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getXpHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "levels VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")" + " ON CONFLICT (playername) DO UPDATE SET (levels)=("
							+ entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "levels VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getXpHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getConsumedPotionsHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "consumedpotions VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")"
							+ " ON CONFLICT (playername) DO UPDATE SET (consumedpotions)=(" + entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "consumedpotions VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getConsumedPotionsHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getDropHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "drops VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")" + " ON CONFLICT (playername) DO UPDATE SET (drops)=("
							+ entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "drops VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getDropHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getHoePlowingHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "hoeplowing VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")"
							+ " ON CONFLICT (playername) DO UPDATE SET (hoeplowing)=(" + entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "hoeplowing VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getHoePlowingHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getFertiliseHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "fertilising VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")"
							+ " ON CONFLICT (playername) DO UPDATE SET (fertilising)=(" + entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "fertilising VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getFertiliseHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getTameHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "tames VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")" + " ON CONFLICT (playername) DO UPDATE SET (tames)=("
							+ entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "tames VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getTameHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getBrewingHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "brewing VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")" + " ON CONFLICT (playername) DO UPDATE SET (brewing)=("
							+ entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "brewing VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getBrewingHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getFireworkHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "fireworks VALUES ('" + entry.getKey()
							+ "', " + entry.getValue() + ")" + " ON CONFLICT (playername) DO UPDATE SET (fireworks)=("
							+ entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "fireworks VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getFireworkHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getMusicDiscHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "musicdiscs VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")"
							+ " ON CONFLICT (playername) DO UPDATE SET (musicdiscs)=(" + entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "musicdiscs VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getMusicDiscHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getEnderPearlHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "enderpearls VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")"
							+ " ON CONFLICT (playername) DO UPDATE SET (enderpearls)=(" + entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "enderpearls VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getEnderPearlHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getBlockPlaceHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "places VALUES ('"
							+ entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
							+ entry.getValue() + ")" + " ON CONFLICT (playername,blockid) DO UPDATE SET (places)=("
							+ entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "places VALUES ('"
							+ entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
							+ entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getBlockPlaceHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getBlockBreakHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "breaks VALUES ('"
							+ entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
							+ entry.getValue() + ")" + " ON CONFLICT (playername,blockid) DO UPDATE SET (breaks)=("
							+ entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "breaks VALUES ('"
							+ entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
							+ entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getBlockBreakHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getKillHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "kills VALUES ('"
							+ entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
							+ entry.getValue() + ")" + " ON CONFLICT (playername,mobname) DO UPDATE SET (kills)=("
							+ entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "kills VALUES ('"
							+ entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
							+ entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getKillHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getCraftHashMap().entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + "crafts VALUES ('"
							+ entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
							+ entry.getValue() + ")" + " ON CONFLICT (playername,item) DO UPDATE SET (crafts)=("
							+ entry.getValue() + ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + "crafts VALUES ('"
							+ entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
							+ entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getCraftHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			st.close();

		} catch (SQLException e) {
			plugin.getLogger().severe("Error while sending async pooled requests to database: " + e);
			e.printStackTrace();
		}

	}
}
