package com.hm.achievement.utils;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.boss.BarColor;

public class ColorHelper {

	public static Color convertChatColorToColor(ChatColor chatColor) {
		switch (chatColor) {
			case AQUA:
				return Color.fromRGB(0x55, 0xFF, 0xFF);
			case BLACK:
				return Color.BLACK;
			case BLUE:
				return Color.fromRGB(0x55, 0x55, 0xFF);
			case GRAY:
				return Color.fromRGB(0xAA, 0xAA, 0xAA);
			case DARK_AQUA:
				return Color.fromRGB(0x00, 0xAA, 0xAA);
			case DARK_BLUE:
				return Color.fromRGB(0x00, 0x00, 0xAA);
			case DARK_GRAY:
				return Color.fromRGB(0x55, 0x55, 0x55);
			case DARK_GREEN:
				return Color.fromRGB(0x00, 0xAA, 0x00);
			case DARK_PURPLE:
				return Color.fromRGB(0xAA, 0x00, 0xAA);
			case DARK_RED:
				return Color.fromRGB(0xAA, 0x00, 0x00);
			case GOLD:
				return Color.fromRGB(0xFF, 0xAA, 0x00);
			case GREEN:
				return Color.fromRGB(0x55, 0xFF, 0x55);
			case LIGHT_PURPLE:
				return Color.fromRGB(0xFF, 0x55, 0xFF);
			case RED:
				return Color.fromRGB(0xFF, 0x55, 0x55);
			case WHITE:
				return Color.WHITE;
			case YELLOW:
				return Color.fromRGB(0xFF, 0xFF, 0x55);
			default:
				return Color.WHITE;
		}
	}

	public static BarColor convertChatColorToBarColor(ChatColor chatColor) {
		switch (chatColor) {
			case AQUA:
				return BarColor.GREEN;
			case BLACK:
				return BarColor.PURPLE;
			case BLUE:
				return BarColor.BLUE;
			case GRAY:
				return BarColor.WHITE;
			case DARK_AQUA:
				return BarColor.BLUE;
			case DARK_BLUE:
				return BarColor.BLUE;
			case DARK_GRAY:
				return BarColor.PURPLE;
			case DARK_GREEN:
				return BarColor.GREEN;
			case DARK_PURPLE:
				return BarColor.PURPLE;
			case DARK_RED:
				return BarColor.RED;
			case GOLD:
				return BarColor.YELLOW;
			case GREEN:
				return BarColor.GREEN;
			case LIGHT_PURPLE:
				return BarColor.PURPLE;
			case RED:
				return BarColor.RED;
			case WHITE:
				return BarColor.WHITE;
			case YELLOW:
				return BarColor.YELLOW;
			default:
				return BarColor.WHITE;
		}
	}

	private ColorHelper() {
		// Not called.
	}

}
