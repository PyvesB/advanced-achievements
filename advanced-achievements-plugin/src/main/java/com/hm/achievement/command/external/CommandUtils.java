package com.hm.achievement.command.external;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CommandUtils {

	/**
	 * The CommandUtils class is used for selector support in commands (@p, @a etc.) Originally made by ZombieStriker
	 * https://github.com/ZombieStriker/PsudoCommands/blob/master/src/me/zombie_striker/psudocommands/CommandUtils.java
	 */

	/**
	 * Use this if you are unsure if a player provided the "@a" tag. This will allow multiple entities to be retrieved.
	 * <p>
	 * This can return a null variable if no tags are included, or if a value for a tag does not exist (I.e if the tag
	 * [type=___] contains an entity that does not exist in the specified world)
	 * <p>
	 * The may also be empty or null values at the end of the array. Once a null value has been reached, you do not need
	 * to loop through any of the higher indexes
	 * <p>
	 * Currently supports the tags:
	 *
	 * @param arg the argument that we are testing for
	 * @param sender the sender of the command
	 * @return The entities that match the criteria
	 * @p , @a , @e , @r
	 *    <p>
	 *    Currently supports the selectors: [type=] [r=] [rm=] [c=] [w=] [m=] [name=] [l=] [lm=] [h=] [hm=] [rx=] [rxm=]
	 *    [ry=] [rym=] [team=] [score_---=] [score_---_min=] [x] [y] [z] [limit=] [x_rotation] [y_rotation]
	 *    <p>
	 *    All selectors can be inverted.
	 */
	public static Entity[] getTargets(CommandSender sender, String arg) {
		Entity[] ents;
		Location loc = null;
		if (sender instanceof Player) {
			loc = ((Player) sender).getLocation();
		} else if (sender instanceof BlockCommandSender) {
			// Center of block.
			loc = ((BlockCommandSender) sender).getBlock().getLocation().add(0.5, 0, 0.5);
		} else if (sender instanceof CommandMinecart) {
			loc = ((CommandMinecart) sender).getLocation();
		}
		String[] tags = getTags(arg);

		// prefab fix
		for (String s : tags) {
			if (hasTag(SelectorType.X, s)) {
				loc.setX(getInt(s));
			} else if (hasTag(SelectorType.Y, s)) {
				loc.setY(getInt(s));
			} else if (hasTag(SelectorType.Z, s)) {
				loc.setZ(getInt(s));
			}
		}

		if (arg.startsWith("@s")) {
			ents = new Entity[1];
			if (sender instanceof Player) {
				boolean good = true;
				for (int b = 0; b < tags.length; b++) {
					if (!canBeAccepted(tags[b], (Entity) sender, loc)) {
						good = false;
						break;
					}
				}
				if (good) {
					ents[0] = (Entity) sender;
				}
			} else {
				return null;
			}
			return ents;
		} else if (arg.startsWith("@a")) {
			// ents = new Entity[maxEnts];
			List<Entity> listOfValidEntities = new ArrayList<>();
			int C = getLimit(arg);

			boolean usePlayers = true;
			for (String tag : tags) {
				if (hasTag(SelectorType.TYPE, tag)) {
					usePlayers = false;
					break;
				}
			}
			List<Entity> ea = new ArrayList<Entity>(Bukkit.getOnlinePlayers());
			if (!usePlayers) {
				ea.clear();
				for (World w : getAcceptedWorldsFullString(loc, arg)) {
					ea.addAll(w.getEntities());
				}
			}
			for (Entity e : ea) {
				if (listOfValidEntities.size() >= C)
					break;
				boolean isValid = true;
				for (int b = 0; b < tags.length; b++) {
					if (!canBeAccepted(tags[b], e, loc)) {
						isValid = false;
						break;
					}
				}
				if (isValid) {
					listOfValidEntities.add(e);
				}
			}

			ents = listOfValidEntities.toArray(new Entity[listOfValidEntities.size()]);

		} else if (arg.startsWith("@p")) {
			ents = new Entity[1];
			double closestInt = Double.MAX_VALUE;
			Entity closest = null;

			for (World w : getAcceptedWorldsFullString(loc, arg)) {
				for (Player e : w.getPlayers()) {
					if (e == sender)
						continue;
					Location temp = loc;
					if (temp == null)
						temp = e.getWorld().getSpawnLocation();
					double distance = e.getLocation().distanceSquared(temp);
					if (closestInt > distance) {
						boolean good = true;
						for (String tag : tags) {
							if (!canBeAccepted(tag, e, temp)) {
								good = false;
								break;
							}
						}
						if (good) {
							closestInt = distance;
							closest = e;
						}
					}
				}
			}
			ents[0] = closest;
		} else if (arg.startsWith("@e")) {
			List<Entity> entities = new ArrayList<Entity>();
			int C = getLimit(arg);
			for (World w : getAcceptedWorldsFullString(loc, arg)) {
				for (Entity e : w.getEntities()) {
					if (entities.size() > C)
						break;
					if (e == sender)
						continue;
					boolean valid = true;
					for (String tag : tags) {
						if (!canBeAccepted(tag, e, loc)) {
							valid = false;
							break;
						}
					}
					if (valid) {
						entities.add(e);
					}
				}
			}
			ents = entities.toArray(new Entity[entities.size()]);
		} else if (arg.startsWith("@r")) {
			Random r = ThreadLocalRandom.current();
			ents = new Entity[1];

			List<Entity> validEntities = new ArrayList<>();
			for (World w : getAcceptedWorldsFullString(loc, arg)) {
				if (hasTag(SelectorType.TYPE, arg)) {
					for (Entity e : w.getEntities()) {
						boolean good = true;
						for (String tag : tags) {
							if (!canBeAccepted(tag, e, loc)) {
								good = false;
								break;
							}
						}
						if (good)
							validEntities.add(e);
					}
				} else {
					for (Entity e : Bukkit.getOnlinePlayers()) {
						boolean good = true;
						for (String tag : tags) {
							if (!canBeAccepted(tag, e, loc)) {
								good = false;
								break;
							}
						}
						if (good)
							validEntities.add(e);
					}
				}
			}
			ents[0] = validEntities.get(r.nextInt(validEntities.size()));
		} else {
			ents = new Entity[] { Bukkit.getPlayer(arg) };
		}
		return ents;
	}

	/**
	 * Returns one entity. Use this if you know the player will not provide the '@a' tag.
	 * <p>
	 * This can return a null variable if no tags are included, or if a value for a tag does not exist (I.e if the tag
	 * [type=___] contains an entity that does not exist in the specified world)
	 *
	 * @param sender the command sender
	 * @param arg the argument of the target
	 * @return The first entity retrieved.
	 */
	public static Entity getTarget(CommandSender sender, String arg) {
		Entity[] e = getTargets(sender, arg);
		if (e.length == 0)
			return null;
		return e[0];
	}

	/**
	 * Returns an integer. Use this to support "~" by providing what it will mean.
	 * <p>
	 * E.g. rel="x" when ~ should be turn into the entity's X coord.
	 * <p>
	 * Currently supports "x", "y" and "z".
	 *
	 * @param arg The target
	 * @param rel relative to the X,Y, or Z
	 * @param e The entity to check relative to.
	 * @return the int
	 */
	public static int getIntRelative(String arg, String rel, Entity e) {
		int relInt = 0;
		if (arg.startsWith("~")) {
			switch (rel.toLowerCase()) {
				case "x":
					relInt = e.getLocation().getBlockX();
					break;
				case "y":
					relInt = e.getLocation().getBlockY();
					break;
				case "z":
					relInt = e.getLocation().getBlockZ();
					break;
			}
			return mathIt(arg, relInt);
		} else if (arg.startsWith("^")) {
			// TODO: Fix code. The currently just acts the same as ~. This should move the
			// entity relative to what its looking at.

			switch (rel.toLowerCase()) {
				case "x":
					relInt = e.getLocation().getBlockX();
					break;
				case "y":
					relInt = e.getLocation().getBlockY();
					break;
				case "z":
					relInt = e.getLocation().getBlockZ();
					break;
			}
			return mathIt(arg, relInt);
		}
		return 0;
	}

	private static boolean canBeAccepted(String arg, Entity e, Location loc) {
		if (hasTag(SelectorType.X_ROTATION, arg) && isWithinYaw(arg, e))
			return true;
		if (hasTag(SelectorType.Y_ROTATION, arg) && isWithinPitch(arg, e))
			return true;
		if (hasTag(SelectorType.TYPE, arg) && isType(arg, e))
			return true;
		if (hasTag(SelectorType.NAME, arg) && isName(arg, e))
			return true;
		if (hasTag(SelectorType.TEAM, arg) && isTeam(arg, e))
			return true;
		if (hasTag(SelectorType.SCORE_FULL, arg) && isScore(arg, e))
			return true;
		if (hasTag(SelectorType.SCORE_MIN, arg) && isScoreMin(arg, e))
			return true;
		if (hasTag(SelectorType.SCORE_13, arg) && isScoreWithin(arg, e))
			return true;
		if (hasTag(SelectorType.DISTANCE, arg) && isWithinDistance(arg, loc, e))
			return true;
		if (hasTag(SelectorType.LEVEL, arg) && isWithinLevel(arg, e))
			return true;
		if (hasTag(SelectorType.TAG, arg) && isHasTags(arg, e))
			return true;
		if (hasTag(SelectorType.RYM, arg) && isRYM(arg, e))
			return true;
		if (hasTag(SelectorType.RXM, arg) && isRXM(arg, e))
			return true;
		if (hasTag(SelectorType.HM, arg) && isHM(arg, e))
			return true;
		if (hasTag(SelectorType.RY, arg) && isRY(arg, e))
			return true;
		if (hasTag(SelectorType.RX, arg) && isRX(arg, e))
			return true;
		if (hasTag(SelectorType.RM, arg) && isRM(arg, loc, e))
			return true;
		if (hasTag(SelectorType.LMax, arg) && isLM(arg, e))
			return true;
		if (hasTag(SelectorType.L, arg) && isL(arg, e))
			return true;
		if (hasTag(SelectorType.m, arg) && isM(arg, e))
			return true;
		if (hasTag(SelectorType.H, arg) && isH(arg, e))
			return true;
		if (hasTag(SelectorType.World, arg) && isW(arg, loc, e))
			return true;
		if (hasTag(SelectorType.R, arg) && isR(arg, loc, e))
			return true;
		if (hasTag(SelectorType.X, arg))
			return true;
		if (hasTag(SelectorType.Y, arg))
			return true;
		if (hasTag(SelectorType.Z, arg))
			return true;
		return false;
	}

	private static String[] getTags(String arg) {
		if (!arg.contains("["))
			return new String[0];
		String tags = arg.split("\\[")[1].split("\\]")[0];
		return tags.split(",");
	}

	private static int mathIt(String args, int relInt) {
		int total = 0;
		short mode = 0;
		String arg = args.replace("~", String.valueOf(relInt));
		String intString = "";
		for (int i = 0; i < arg.length(); i++) {
			if (arg.charAt(i) == '+' || arg.charAt(i) == '-' || arg.charAt(i) == '*' || arg.charAt(i) == '/') {
				try {
					switch (mode) {
						case 0:
							total = total + Integer.parseInt(intString);
							break;
						case 1:
							total = total - Integer.parseInt(intString);
							break;
						case 2:
							total = total * Integer.parseInt(intString);
							break;
						case 3:
							total = total / Integer.parseInt(intString);
							break;
					}
					mode = (short) ((arg.charAt(i) == '+') ? 0
							: ((arg.charAt(i) == '-') ? 1
									: ((arg.charAt(i) == '*') ? 2 : ((arg.charAt(i) == '/') ? 3 : -1))));
				} catch (Exception e) {
					Bukkit.getLogger().severe("There has been an issue with a plugin using the CommandUtils class!");
				}

			} else if (args.length() == i || arg.charAt(i) == ' ' || arg.charAt(i) == ',' || arg.charAt(i) == ']') {
				try {
					switch (mode) {
						case 0:
							total = total + Integer.parseInt(intString);
							break;
						case 1:
							total = total - Integer.parseInt(intString);
							break;
						case 2:
							total = total * Integer.parseInt(intString);
							break;
						case 3:
							total = total / Integer.parseInt(intString);
							break;
					}
				} catch (Exception e) {
					Bukkit.getLogger().severe("There has been an issue with a plugin using the CommandUtils class!");
				}
				break;
			} else {
				intString += arg.charAt(i);
			}
		}
		return total;
	}

	private static int getLimit(String arg) {
		if (hasTag(SelectorType.LIMIT, arg))
			for (String s : getTags(arg)) {
				if (hasTag(SelectorType.LIMIT, s)) {
					return getInt(s);
				}
			}
		if (hasTag(SelectorType.C, arg))
			for (String s : getTags(arg)) {
				if (hasTag(SelectorType.C, s)) {
					return getInt(s);
				}
			}
		return Integer.MAX_VALUE;
	}

	private static String getType(String arg) {
		if (hasTag(SelectorType.TYPE, arg))
			return arg.toLowerCase().split("=")[1].replace("!", "");
		return "Player";
	}

	private static String getName(String arg) {
		String reparg = arg.replace(" ", "_");
		return reparg.replace("!", "").split("=")[1];
	}

	private static World getW(String arg) {
		return Bukkit.getWorld(getString(arg));
	}

	private static String getScoreMinName(String arg) {
		return arg.split("=")[0].substring(0, arg.split("=")[0].length() - 1 - 4).replace("score_", "");
	}

	private static String getScoreName(String arg) {
		return arg.split("=")[0].replace("score_", "");
	}

	private static String getTeam(String arg) {
		return arg.toLowerCase().replace("!", "").split("=")[1];
	}

	private static float getValueAsFloat(String arg) {
		return Float.parseFloat(arg.replace("!", "").split("=")[1]);
	}

	private static int getValueAsInteger(String arg) {
		return Integer.parseInt(arg.replace("!", "").split("=")[1]);
	}

	private static GameMode getM(String arg) {
		String[] split = arg.replace("!", "").toLowerCase().split("=");
		String returnType = split[1];
		if (returnType.equalsIgnoreCase("0") || returnType.equalsIgnoreCase("s")
				|| returnType.equalsIgnoreCase("survival"))
			return GameMode.SURVIVAL;
		if (returnType.equalsIgnoreCase("1") || returnType.equalsIgnoreCase("c")
				|| returnType.equalsIgnoreCase("creative"))
			return GameMode.CREATIVE;
		if (returnType.equalsIgnoreCase("2") || returnType.equalsIgnoreCase("a")
				|| returnType.equalsIgnoreCase("adventure"))
			return GameMode.ADVENTURE;
		if (returnType.equalsIgnoreCase("3") || returnType.equalsIgnoreCase("sp")
				|| returnType.equalsIgnoreCase("spectator"))
			return GameMode.SPECTATOR;
		return null;
	}

	private static List<World> getAcceptedWorldsFullString(Location loc, String fullString) {
		String string = null;
		for (String tag : getTags(fullString)) {
			if (hasTag(SelectorType.World, tag)) {
				string = tag;
				break;
			}
		}
		if (string == null) {
			List<World> worlds = new ArrayList<World>();
			if (loc == null || loc.getWorld() == null) {
				worlds.addAll(Bukkit.getWorlds());
			} else {
				worlds.add(loc.getWorld());
			}
			return worlds;
		}
		return getAcceptedWorlds(string);
	}

	private static List<World> getAcceptedWorlds(String string) {
		List<World> worlds = new ArrayList<World>(Bukkit.getWorlds());
		if (isInverted(string)) {
			worlds.remove(getW(string));
		} else {
			worlds.clear();
			worlds.add(getW(string));
		}
		return worlds;
	}

	private static boolean isTeam(String arg, Entity e) {
		if (!(e instanceof Player))
			return false;
		for (Team t : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
			if ((t.getName().equalsIgnoreCase(getTeam(arg)) != isInverted(arg))) {
				if ((t.getEntries().contains(((Player) e).getName()) != isInverted(arg)))
					return true;
			}
		}
		return false;
	}

	private static boolean isWithinPitch(String arg, Entity e) {
		float pitch = getValueAsFloat(arg);
		return isWithinDoubleValue(isInverted(arg), arg, e.getLocation().getPitch());
	}

	private static boolean isWithinYaw(String arg, Entity e) {
		float pitch = getValueAsFloat(arg);
		return isWithinDoubleValue(isInverted(arg), arg, e.getLocation().getYaw());
	}

	private static boolean isWithinDistance(String arg, Location start, Entity e) {
		double distanceMin = 0;
		double distanceMax = Double.MAX_VALUE;
		String distance = arg.split("=")[1];
		if (e.getLocation().getWorld() != start.getWorld())
			return false;
		if (distance.contains("..")) {
			String[] temp = distance.split("\\.\\.");
			if (!temp[0].isEmpty()) {
				distanceMin = Integer.parseInt(temp[0]);
			}
			if (temp.length > 1 && !temp[1].isEmpty()) {
				distanceMax = Double.parseDouble(temp[1]);
			}
			double actDis = start.distanceSquared(e.getLocation());
			return actDis <= distanceMax * distanceMax && distanceMin * distanceMin <= actDis;
		} else {
			int mult = Integer.parseInt(distance);
			mult *= mult;
			return ((int) start.distanceSquared(e.getLocation())) == mult;
		}
	}

	private static boolean isWithinLevel(String arg, Entity e) {
		if (!(e instanceof Player))
			return false;
		double distanceMin = 0;
		double distanceMax = Double.MAX_VALUE;
		String distance = arg.split("=")[1];
		if (distance.contains("..")) {
			String[] temp = distance.split("..");
			if (!temp[0].isEmpty()) {
				distanceMin = Integer.parseInt(temp[0]);
			}
			if (temp[1] != null && !temp[1].isEmpty()) {
				distanceMax = Double.parseDouble(temp[1]);
			}
			double actDis = ((Player) e).getExpToLevel();
			return actDis <= distanceMax * distanceMax && distanceMin * distanceMin <= actDis;
		} else {
			return ((Player) e).getExpToLevel() == Integer.parseInt(distance);
		}
	}

	private static boolean isScore(String arg, Entity e) {
		if (!(e instanceof Player))
			return false;
		for (Objective o : Bukkit.getScoreboardManager().getMainScoreboard().getObjectives()) {
			if (o.getName().equalsIgnoreCase(getScoreName(arg))) {
				if ((o.getScore(((Player) e).getName()).getScore() <= getValueAsInteger(arg) != isInverted(arg)))
					return true;
			}
		}
		return false;
	}

	private static boolean isScoreWithin(String arg, Entity e) {
		if (!(e instanceof Player))
			return false;
		String[] scores = arg.split("\\{")[1].split("\\}")[0].split(",");
		for (int i = 0; i < scores.length; i++) {
			String[] s = scores[i].split("=");
			String name = s[0];

			for (Objective o : Bukkit.getScoreboardManager().getMainScoreboard().getObjectives()) {
				if (o.getName().equalsIgnoreCase(name)) {
					if (!isWithinDoubleValue(isInverted(arg), s[1], o.getScore(e.getName()).getScore()))
						return false;
				}
			}
		}
		return true;

	}

	private static boolean isHasTags(String arg, Entity e) {
		if (!(e instanceof Player))
			return false;
		return isInverted(arg) != e.getScoreboardTags().contains(getString(arg));

	}

	private static boolean isScoreMin(String arg, Entity e) {
		if (!(e instanceof Player))
			return false;
		for (Objective o : Bukkit.getScoreboardManager().getMainScoreboard().getObjectives()) {
			if (o.getName().equalsIgnoreCase(getScoreMinName(arg))) {
				if ((o.getScore(((Player) e).getName()).getScore() >= getValueAsInteger(arg) != isInverted(arg)))
					return true;
			}
		}
		return false;
	}

	private static boolean isRM(String arg, Location loc, Entity e) {
		if (loc.getWorld() != e.getWorld())
			return false;
		return isGreaterThan(arg, loc.distance(e.getLocation()));
	}

	private static boolean isR(String arg, Location loc, Entity e) {
		if (loc.getWorld() != e.getWorld())
			return false;
		return isLessThan(arg, loc.distance(e.getLocation()));

	}

	private static boolean isRXM(String arg, Entity e) {
		return isLessThan(arg, e.getLocation().getYaw());
	}

	private static boolean isRX(String arg, Entity e) {
		return isGreaterThan(arg, e.getLocation().getYaw());
	}

	private static boolean isRYM(String arg, Entity e) {
		return isLessThan(arg, e.getLocation().getPitch());
	}

	private static boolean isRY(String arg, Entity e) {
		return isGreaterThan(arg, e.getLocation().getPitch());
	}

	private static boolean isL(String arg, Entity e) {
		if (e instanceof Player) {
			isLessThan(arg, ((Player) e).getTotalExperience());
		}
		return false;
	}

	private static boolean isLM(String arg, Entity e) {
		if (e instanceof Player) {
			return isGreaterThan(arg, ((Player) e).getTotalExperience());
		}
		return false;
	}

	private static boolean isH(String arg, Entity e) {
		if (e instanceof Damageable)
			return isGreaterThan(arg, ((Damageable) e).getHealth());
		return false;
	}

	private static boolean isHM(String arg, Entity e) {
		if (e instanceof Damageable)
			return isLessThan(arg, ((Damageable) e).getHealth());
		return false;
	}

	private static boolean isM(String arg, Entity e) {
		if (getM(arg) == null)
			return true;
		if (e instanceof HumanEntity) {
			if ((isInverted(arg) != (getM(arg) == ((HumanEntity) e).getGameMode())))
				return true;
		}
		return false;
	}

	private static boolean isW(String arg, Location loc, Entity e) {
		if (getW(arg) == null) {
			return true;
		} else if ((isInverted(arg) != getAcceptedWorlds(arg).contains(getW(arg))))
			return true;
		return false;
	}

	private static boolean isName(String arg, Entity e) {
		if (getName(arg) == null)
			return true;
		if ((isInverted(arg) != (e.getCustomName() != null) && isInverted(arg) != (getName(arg)
				.equals(e.getCustomName().replace(" ", "_"))
				|| (e instanceof Player && ((Player) e).getName().replace(" ", "_").equalsIgnoreCase(getName(arg))))))
			return true;
		return false;
	}

	private static boolean isType(String arg, Entity e) {
		boolean invert = isInverted(arg);
		String type = getType(arg);
		if (invert != e.getType().name().equalsIgnoreCase(type))
			return true;
		return false;

	}

	private static boolean isInverted(String arg) {
		return arg.toLowerCase().split("!").length != 1;
	}

	private static int getInt(String arg) {
		int mult = Integer.parseInt(arg.split("=")[1]);
		return mult;
	}

	public static String getString(String arg) {
		return arg.split("=")[1].replaceAll("!", "");
	}

	private static boolean isLessThan(String arg, double value) {
		boolean inverted = isInverted(arg);
		double mult = Double.parseDouble(arg.split("=")[1]);
		return (value < mult) != inverted;
	}

	private static boolean isGreaterThan(String arg, double value) {
		boolean inverted = isInverted(arg);
		double mult = Double.parseDouble(arg.split("=")[1]);
		return (value > mult) != inverted;
	}

	private static boolean isWithinDoubleValue(boolean inverted, String arg, double value) {
		double min = -Double.MAX_VALUE;
		double max = Double.MAX_VALUE;
		if (arg.contains("..")) {
			String[] temp = arg.split("\\.\\.");
			if (!temp[0].isEmpty()) {
				min = Integer.parseInt(temp[0]);
			}
			if (temp.length > 1 && !temp[1].isEmpty()) {
				max = Double.parseDouble(temp[1]);
			}
			return (value <= max * max && min * min <= value) != inverted;
		} else {
			double mult = Double.parseDouble(arg);
			return (value == mult) != inverted;
		}
	}

	private static boolean hasTag(SelectorType type, String arg) {
		return arg.toLowerCase().startsWith(type.getName());
	}

	enum SelectorType {

		LEVEL("level="),
		DISTANCE("distance="),
		TYPE("type="),
		NAME("name="),
		TEAM("team="),
		LMax("lm="),
		L("l="),
		World("w="),
		m("m="),
		C("c="),
		HM("hm="),
		H("h="),
		RM("rm="),
		RYM("rym="),
		RX("rx="),
		SCORE_FULL("score="),
		SCORE_MIN("score_min"),
		SCORE_13("scores="),
		R("r="),
		RXM("rxm="),
		RY("ry="),
		TAG("tag="),
		X("x="),
		Y("y="),
		Z("z="),
		LIMIT("limit="),
		Y_ROTATION("y_rotation"),
		X_ROTATION("x_rotation");

		String name;

		SelectorType(String s) {
			this.name = s;
		}

		public String getName() {
			return name;
		}
	}
}
