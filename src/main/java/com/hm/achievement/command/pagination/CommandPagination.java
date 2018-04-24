package com.hm.achievement.command.pagination;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Utility for paginating command messages.
 * <p>
 * Ignores length of given items,
 * user of this utility needs to make sure the items are not too long to wrap in the chat box.
 * <p>
 * Wrapping in the chat box is difficult to calculate since the Minecraft font is not monospaced
 * so 'w' and 'i' are different width, as well as unicode characters which are their own special category.
 *
 * @author Rsl1122
 */
public class CommandPagination {

	private final List<String> toPaginate;
	private final int size;
	private final int maxPage;

	public CommandPagination(List<String> toPaginate) {
		this.toPaginate = toPaginate;
		size = toPaginate.size();
		int leftovers = size % 18;
		// One command window can fit 20 lines, we're leaving 2 for header and footer.
		maxPage = (size - leftovers) / 18 + (leftovers > 0 ? 1 : 0);
	}

	public void sendPage(int page, CommandSender to) {
		sendPage(page, to::sendMessage);
	}

	public void sendPage(int page, MethodRef<String> to) {
		int pageToSend = page > maxPage ? maxPage : page;

		String header = "§7> §5Page " + pageToSend + "/" + maxPage;
		String footer = "§7>";

		to.call(header);

		int index = pageToSend - 1;
		// Handling case where empty list is given to CommandPagination
		int pageStart = index > 0 ? (index * 18) : 0;
		int nextPageStart = pageToSend * 18;

		for (int i = pageStart; i < Math.min(nextPageStart, size); i++) {
			to.call(toPaginate.get(i));
		}

		to.call(footer);
	}

	interface MethodRef<T> {
		void call(T value);
	}
}