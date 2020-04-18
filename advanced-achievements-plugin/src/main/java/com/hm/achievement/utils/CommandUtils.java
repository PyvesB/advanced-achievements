package com.hm.achievement.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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

public class CommandUtils {

    /**
     * Use this if you are unsure if a player provided the "@a" tag. This will
     * allow multiple entities to be retrieved.
     *
     * This can return a null variable if no tags are included, or if a value
     * for a tag does not exist (I.e if the tag [type=___] contains an entity
     * that does not exist in the specified world)
     *
     * The may also be empty or null values at the end of the array. Once a null
     * value has been reached, you do not need to loop through any of the higher
     * indexes
     *
     * Currently supports the tags:
     *
     * @p , @a , @e , @r
     *
     *    Currently supports the selectors: [type=] [r=] [rm=] [c=] [w=] [m=]
     *    [name=] [l=] [lm=] [h=] [hm=] [rx=] [rxm=] [ry=] [rym=] [team=]
     *    [score_---=] [score_---_min=]
     *
     *    All selectors can be inverted.
     *
     * @param sender The
     *            command sender
     * @param arg The
     *            argument to test for
     * @return The entities that match the criteria
     */
    public static Entity[] getTargets(CommandSender sender, String arg) {
        Entity[] ents = null;
        Location loc = null;
        if (sender instanceof Player) {
            loc = ((Player) sender).getLocation();
        } else if (sender instanceof BlockCommandSender) {
            loc = ((BlockCommandSender) sender).getBlock().getLocation();
        } else if (sender instanceof CommandMinecart) {
            loc = ((CommandMinecart) sender).getLocation();
        }
        if (arg.startsWith("@a")) {
            if (loc != null) {
                int maxEnts = 0;
                for (World w : getAcceptedWorlds(loc, arg))
                    maxEnts += w.getEntities().size();
                ents = new Entity[maxEnts];
                int id = 0;
                int C = getC(arg);
                World world3 = null;

                for (World w : getAcceptedWorlds(loc, arg)) {
                    List<Entity> ea = w.getEntities();
                    for (int i = 0; i < w.getEntities().size(); i++) {
                        if (world3 == null || !world3.equals(w)) {
                            world3 = w;
                        }
                        if (id >= C)
                            break;
                        Entity e = ea.get(i);
                        boolean good = true;
                        for (int b = 0; b < getTags(arg).length; b++) {
                            if (!canBeAccepted(getTags(arg)[b], e, loc)) {
                                good = false;
                                break;
                            }
                        }
                        if (good) {
                            ents[id] = e;
                            id++;
                        }
                    }
                }
            }
        } else if (arg.startsWith("@p")) {
            ents = new Entity[1];
            double closestInt = -1;
            Entity closest = null;
            for (World w : getAcceptedWorlds(loc, arg)) {
                for (Player e : w.getPlayers()) {
                    if (e.getLocation().equals(loc))
                        continue;
                    if (closestInt == -1
                            || closestInt > e.getLocation().distance(loc)) {
                        boolean good = true;
                        for (int b = 0; b < getTags(arg).length; b++) {
                            if (!canBeAccepted(arg, e, loc)) {
                                good = false;
                                break;
                            }
                        }
                        if (good) {
                            closestInt = e.getLocation().distance(loc);
                            closest = e;
                        }
                    }
                }
            }
            ents[0] = closest;
        } else if (arg.startsWith("@e")) {
            ents = new Entity[1];
            double closestInt = -1;
            Entity closest = null;
            for (World w : getAcceptedWorlds(loc, arg)) {
                for (Entity e : w.getEntities()) {
                    if (e.getLocation().equals(loc))
                        continue;
                    boolean good = true;
                    if (closestInt == -1
                            || closestInt > e.getLocation().distance(loc)) {
                        for (int b = 0; b < getTags(arg).length; b++) {
                            if (!canBeAccepted(arg, e, loc)) {
                                good = false;
                                break;
                            }
                        }
                        if (good) {
                            closestInt = e.getLocation().distance(loc);
                            closest = e;
                        }
                    }
                }
            }
            ents[0] = closest;
        } else if (arg.startsWith("@r")) {
            Random r = ThreadLocalRandom.current();
            ents = new Entity[1];
            Entity entity = null;
            int tries = 0;
            while (entity == null && tries < 100) {
                tries++;
                if (hasType(arg)) {
                    Entity e = loc
                            .getWorld()
                            .getEntities()
                            .get(r.nextInt(loc.getWorld().getEntities().size()));
                    boolean good = true;
                    for (int b = 0; b < getTags(arg).length; b++) {
                        if (!canBeAccepted(arg, e, loc)) {
                            good = false;
                            break;
                        }
                    }
                    if (good)
                        entity = e;
                } else {
                    List<Player> onl = new ArrayList<Player>(
                            Bukkit.getOnlinePlayers());
                    Entity e = onl.get(r.nextInt(onl.size()));
                    boolean good = true;
                    for (int b = 0; b < getTags(arg).length; b++) {
                        if (!canBeAccepted(arg, e, loc)) {
                            good = false;
                            break;
                        }
                    }
                    if (good)
                        entity = e;
                }
            }
            ents[0] = entity;
        } else {
            ents = new Entity[1];
            ents[0] = Bukkit.getPlayer(arg);
        }
        return ents;
    }

    /**
     * Returns one entity. Use this if you know the player will not provide the
     * '@a' tag.
     *
     * This can return a null variable if no tags are included, or if a value
     * for a tag does not exist (I.e if the tag [type=___] contains an entity
     * that does not exist in the specified world)
     *
     * @param sender Who
     *            sent the command
     * @param arg The
     *            argument
     * @return The first entity retrieved.
     */
    public static Entity getTarget(CommandSender sender, String arg) {
        return getTargets(sender, arg)[0];
    }

    /**
     * Returns an integer. Use this to support "~" by providing what it will
     * mean.
     *
     * E.g. rel="x" when ~ should be turn into the entity's X coord.
     *
     * Currently supports "x", "y" and "z".
     *
     *
     * @param arg The
     *            argument
     * @param rel What
     *            the int should represent
     * @param e The
     *            entity to get the relative int from.
     * @return the int
     */
    public static int getIntRelative(String arg, String rel, Entity e) {
        int relInt = 0;
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

    private static boolean canBeAccepted(String arg, Entity e, Location loc) {
        if (isType(arg, e))
            return true;
        if (isR(arg, loc, e))
            return true;
        if (isName(arg, e))
            return true;
        if (isRM(arg, loc, e))
            return true;
        if (isH(arg, e))
            return true;
        if (isHM(arg, e))
            return true;
        if (isM(arg, e))
            return true;
        if (isL(arg, e))
            return true;
        if (isLM(arg, e))
            return true;
        if (isW(arg, loc, e))
            return true;
        if (isRX(arg, e))
            return true;
        if (isRXM(arg, e))
            return true;
        if (isRY(arg, e))
            return true;
        if (isTeam(arg, e))
            return true;
        if (isScore(arg, e))
            return true;
        if (isScoreMin(arg, e))
            return true;
        if (isRYM(arg, e))
            return true;
        return false;
    }

    private static String[] getTags(String arg) {
        if(!arg.contains("["))
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
            if (arg.charAt(i) == '+' || arg.charAt(i) == '-'
                    || arg.charAt(i) == '*' || arg.charAt(i) == '/') {
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
                    mode = (short) ((arg.charAt(i) == '+') ? 0 : ((arg
                            .charAt(i) == '-') ? 1
                            : ((arg.charAt(i) == '*') ? 2
                            : ((arg.charAt(i) == '/') ? 3 : -1))));
                } catch (Exception e) {
                    Bukkit.getLogger()
                            .severe("There has been an issue with a plugin using the CommandUtils class!");
                }

            } else if (args.length() == i || arg.charAt(i) == ' '
                    || arg.charAt(i) == ',' || arg.charAt(i) == ']') {
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
                    Bukkit.getLogger()
                            .severe("There has been an issue with a plugin using the CommandUtils class!");
                }
                break;
            } else {
                intString += arg.charAt(i);
            }
        }
        return total;
    }

    private static String getType(String arg) {
        if (hasType(arg))
            return arg.toLowerCase().split("=")[1].replace("!", "");
        return "Player";
    }

    private static int getC(String arg) {
        if (!hasC(arg))
            return Integer.MAX_VALUE;
        return Integer.parseInt(arg.toLowerCase().replace("!", "").split("=")[1]);
    }

    private static int getR(String arg) {
        if (!hasR(arg))
            return Integer.MAX_VALUE;
        return Integer.parseInt(arg.toLowerCase().replace("!", "").split("=")[1]);
    }

    private static int getRM(String arg) {
        if (!hasRM(arg))
            return 1;
        return Integer.parseInt(arg.toLowerCase().replace("!", "").split("=")[1]);
    }

    private static int getRX(String arg) {
        if (!hasRX(arg))
            return -Integer.MAX_VALUE;
        return Integer.parseInt(arg.toLowerCase().replace("!", "").split("=")[1]);
    }

    private static int getRXM(String arg) {
        if (!hasRXM(arg))
            return -8;
        return Integer.parseInt(arg.toLowerCase().replace("!", "").split("=")[1]);
    }

    private static int getRY(String arg) {
        if (!hasRY(arg))
            return -Integer.MAX_VALUE;
        return Integer.parseInt(arg.toLowerCase().replace("!", "").split("=")[1]);
    }

    private static int getRYM(String arg) {
        if (!hasRYM(arg))
            return -8;
        return Integer.parseInt(arg.toLowerCase().replace("!", "").split("=")[1]);
    }

    private static int getH(String arg) {
        if (!hasH(arg))
            return -1;
        return Integer.parseInt(arg.toLowerCase().replace("!", "").split("=")[1]);
    }

    private static int getHM(String arg) {
        if (!hasHM(arg))
            return Integer.MAX_VALUE;
        return Integer.parseInt(arg.toLowerCase().replace("!", "").split("=")[1]);
    }

    private static int getL(String arg) {
        if (!hasL(arg))
            return -1;
        return Integer.parseInt(arg.toLowerCase().replace("!", "").split("=")[1]);
    }

    private static int getLM(String arg) {
        if (!hasLM(arg))
            return Integer.MAX_VALUE;
        return Integer.parseInt(arg.toLowerCase().replace("!", "").split("=")[1]);
    }

    private static String getName(String arg) {
        String reparg = arg.replace(" ", "_");
        if (!hasName(reparg))
            return null;
        return reparg.replace("!", "").split("=")[1];
    }

    private static World getW(String arg) {
        if (!hasW(arg))
            return null;
        return Bukkit.getWorld(arg.replace("!", "").split("=")[1]);
    }

    private static String getScoreMinName(String arg) {
        if (!hasScoreMin(arg))
            return null;
        return arg.split("=")[0].substring(0,
                arg.split("=")[0].length() - 1 - 4).replace("score_", "");
    }

    private static String getScoreName(String arg) {
        if (!hasScore(arg))
            return null;
        return arg.split("=")[0].replace("score_", "");
    }

    private static String getTeam(String arg) {
        if (!hasTeam(arg))
            return null;
        return arg.toLowerCase().replace("!", "").split("=")[1];
    }

    private static int getScoreMin(String arg) {
        if (!hasScoreMin(arg))
            return -8;
        return Integer.parseInt(arg.replace("!", "").split("=")[1]);
    }

    private static int getScore(String arg) {
        if (!hasScore(arg))
            return Integer.MAX_VALUE;
        return Integer.parseInt(arg.replace("!", "").split("=")[1]);
    }

    private static GameMode getM(String arg) {
        if (!hasM(arg))
            return null;
        String[] split = arg.replace("!", "").toLowerCase().split("=");
        String returnType = split[1];
        if (returnType.equalsIgnoreCase("0")
                || returnType.equalsIgnoreCase("s")
                || returnType.equalsIgnoreCase("survival"))
            return GameMode.SURVIVAL;
        if (returnType.equalsIgnoreCase("1")
                || returnType.equalsIgnoreCase("c")
                || returnType.equalsIgnoreCase("creative"))
            return GameMode.CREATIVE;
        if (returnType.equalsIgnoreCase("2")
                || returnType.equalsIgnoreCase("a")
                || returnType.equalsIgnoreCase("adventure"))
            return GameMode.ADVENTURE;
        if (returnType.equalsIgnoreCase("3")
                || returnType.equalsIgnoreCase("sp")
                || returnType.equalsIgnoreCase("spectator"))
            return GameMode.SPECTATOR;
        return null;
    }

    private static List<World> getAcceptedWorlds(Location loc, String string) {
        List<World> worlds = new ArrayList<World>(Bukkit.getWorlds());
        if (getW(string) == null) {
        } else if (isInverted(string)) {
            worlds.remove(getW(string));
        } else {
            worlds.clear();
            worlds.add(getW(string));
        }
        return worlds;
    }

    private static boolean isTeam(String arg, Entity e) {
        if (!hasTeam(arg))
            return true;
        if (!(e instanceof Player))
            return false;
        for (Team t : Bukkit.getScoreboardManager().getMainScoreboard()
                .getTeams()) {
            if ((t.getName().equalsIgnoreCase(getTeam(arg)) != isInverted(arg))) {
                if ((t.getEntries().contains(((Player) e).getName()) != isInverted(arg)))
                    return true;
            }
        }
        return false;
    }

    private static boolean isScore(String arg, Entity e) {
        if (!hasScore(arg))
            return true;
        if (!(e instanceof Player))
            return false;
        for (Objective o : Bukkit.getScoreboardManager().getMainScoreboard()
                .getObjectives()) {
            if (o.getName().equalsIgnoreCase(getScoreName(arg))) {
                if ((o.getScore(((Player) e).getName()).getScore() <= getScore(arg) != isInverted(arg)))
                    return true;
            }
        }
        return false;
    }

    private static boolean isScoreMin(String arg, Entity e) {
        if (!hasScoreMin(arg))
            return true;
        if (!(e instanceof Player))
            return false;
        for (Objective o : Bukkit.getScoreboardManager().getMainScoreboard()
                .getObjectives()) {
            if (o.getName().equalsIgnoreCase(getScoreMinName(arg))) {
                if ((o.getScore(((Player) e).getName()).getScore() >= getScoreMin(arg) != isInverted(arg)))
                    return true;
            }
        }
        return false;
    }

    private static boolean isRM(String arg, Location loc, Entity e) {
        if (!hasRM(arg))
            return true;
        if (isInverted(arg) != (getRM(arg) < loc.distance(e.getLocation())))
            return true;
        return false;
    }

    private static boolean isR(String arg, Location loc, Entity e) {
        if (!hasR(arg))
            return true;
        if (isInverted(arg) != (getR(arg) > loc.distance(e.getLocation())))
            return true;
        return false;

    }

    private static boolean isRXM(String arg, Entity e) {
        if (isInverted(arg) != (getRXM(arg) < e.getLocation().getYaw()))
            return true;
        return false;
    }

    private static boolean isRX(String arg, Entity e) {
        if (isInverted(arg) != (getRX(arg) > e.getLocation().getYaw()))
            return true;
        return false;
    }

    private static boolean isRYM(String arg, Entity e) {
        if (isInverted(arg) != (getRYM(arg) < e.getLocation().getPitch()))
            return true;
        return false;
    }

    private static boolean isRY(String arg, Entity e) {
        if (isInverted(arg) != (getRY(arg) > e.getLocation().getPitch()))
            return true;
        return false;
    }

    private static boolean isL(String arg, Entity e) {
        if (!hasL(arg))
            return true;
        if (e instanceof Player) {
            if (isInverted(arg) != (getL(arg) < ((Player) e)
                    .getTotalExperience()))
                return true;
        }
        return false;
    }

    private static boolean isLM(String arg, Entity e) {
        if (!hasLM(arg))
            return true;
        if (e instanceof Player) {
            if (isInverted(arg) != (getLM(arg) > ((Player) e)
                    .getTotalExperience()))
                return true;

        }
        return false;
    }

    private static boolean isH(String arg, Entity e) {
        if (!hasH(arg))
            return true;
        if (e instanceof Damageable) {
            if (isInverted(arg) != (getH(arg) > ((Damageable) e).getHealth()))
                return true;
        }
        return false;
    }

    private static boolean isHM(String arg, Entity e) {
        if (!hasHM(arg))
            return true;
        if (e instanceof Damageable) {
            if (isInverted(arg) != (getHM(arg) < ((Damageable) e).getHealth()))
                return true;
        }
        return false;
    }

    private static boolean isM(String arg, Entity e) {
        if (getM(arg) == null)
            return true;
        if (e instanceof HumanEntity) {
            if ((isInverted(arg) != (getM(arg) == ((HumanEntity) e)
                    .getGameMode())))
                return true;
        }
        return false;
    }

    private static boolean isW(String arg, Location loc, Entity e) {
        if (getW(arg) == null) {
            return true;
        } else if ((isInverted(arg) != getAcceptedWorlds(loc, arg).contains(
                getW(arg))))
            return true;
        return false;
    }

    private static boolean isName(String arg, Entity e) {
        if (getName(arg) == null)
            return true;
        if ((isInverted(arg) != (e.getCustomName() != null) && isInverted(arg) != (getName(
                arg).equals(e.getCustomName().replace(" ", "_")) || (e instanceof Player && ((Player) e)
                .getName().replace(" ", "_").equalsIgnoreCase(getName(arg))))))
            return true;
        return false;
    }

    private static boolean isType(String arg, Entity e) {
        if (!hasType(arg))
            return true;
        boolean invert = isInverted(arg);
        String type = getType(arg);
        if (invert != e.getType().name().equalsIgnoreCase(type))
            return true;
        return false;

    }

    private static boolean isInverted(String arg) {
        return arg.toLowerCase().split("!").length != 1;
    }

    private static boolean hasR(String arg) {
        return arg.toLowerCase().startsWith("r=");
    }

    private static boolean hasScoreMin(String arg) {
        boolean startW = arg.startsWith("score_");
        if (!startW)
            return false;
        String[] split = arg.split("=");
        if (split[0].endsWith("_min"))
            return true;
        return false;
    }

    private static boolean hasScore(String arg) {
        boolean startW = arg.startsWith("score_");
        if (!startW)
            return false;
        String[] split = arg.split("=");
        if (!split[0].endsWith("_min"))
            return true;
        return false;
    }

    private static boolean hasRX(String arg) {
        return arg.toLowerCase().startsWith("rx=");
    }

    private static boolean hasRXM(String arg) {
        return arg.toLowerCase().startsWith("rxm=");
    }

    private static boolean hasRY(String arg) {
        return arg.toLowerCase().startsWith("ry=");
    }

    private static boolean hasRYM(String arg) {
        return arg.toLowerCase().startsWith("rym=");
    }

    private static boolean hasRM(String arg) {
        return arg.toLowerCase().startsWith("rm=");
    }

    private static boolean hasH(String arg) {
        return arg.toLowerCase().startsWith("h=");
    }

    private static boolean hasHM(String arg) {
        return arg.toLowerCase().startsWith("hm=");
    }

    private static boolean hasC(String arg) {
        return arg.toLowerCase().startsWith("c=");
    }

    private static boolean hasM(String arg) {
        return arg.toLowerCase().startsWith("m=");
    }

    private static boolean hasW(String arg) {
        return arg.toLowerCase().startsWith("w=");
    }

    private static boolean hasL(String arg) {
        return arg.toLowerCase().startsWith("l=");
    }

    private static boolean hasLM(String arg) {
        return arg.toLowerCase().startsWith("lm=");
    }

    private static boolean hasName(String arg) {
        return arg.toLowerCase().startsWith("name=");
    }

    private static boolean hasTeam(String arg) {
        return (arg.toLowerCase().startsWith("team="));
    }

    private static boolean hasType(String arg) {
        return arg.toLowerCase().startsWith("type=");
    }
}