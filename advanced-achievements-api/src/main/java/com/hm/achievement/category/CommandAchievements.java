package com.hm.achievement.category;

/**
 * Category driven by the /aach give command rather than statistics.
 *
 * @author Pyves
 */
public class CommandAchievements implements Category {

	public static final CommandAchievements COMMANDS = new CommandAchievements();

	private CommandAchievements() {
		// Not called externally.
	}

	@Override
	public String toString() {
		return "Commands";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toDBName() {
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toPermName() {
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toLangKey() {
		return "list-commands";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toLangDefault() {
		return "Other Achievements";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toConfigComment() {
		return "/aach give yourAch1 PLAYER can be used to give the yourAch1 achievement to PLAYER.";
	}
}
