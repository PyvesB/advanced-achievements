package com.hm.achievement.utils;

import org.bukkit.ChatColor;
import org.bukkit.Color;

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

	/*
	 * Model bar colors as strings to ensure compatibility with Minecraft 1.7/1.8, where the class did not yet exist.
	 */
	public static String convertChatColorToBarColor(ChatColor chatColor) {
		switch (chatColor) {
			case AQUA:
				return "GREEN";
			case BLACK:
				return "PURPLE";
			case BLUE:
				return "BLUE";
			case GRAY:
				return "WHITE";
			case DARK_AQUA:
				return "BLUE";
			case DARK_BLUE:
				return "BLUE";
			case DARK_GRAY:
				return "PURPLE";
			case DARK_GREEN:
				return "GREEN";
			case DARK_PURPLE:
				return "PURPLE";
			case DARK_RED:
				return "RED";
			case GOLD:
				return "YELLOW";
			case GREEN:
				return "GREEN";
			case LIGHT_PURPLE:
				return "PURPLE";
			case RED:
				return "RED";
			case WHITE:
				return "WHITE";
			case YELLOW:
				return "YELLOW";
			default:
				return "WHITE";
		}
	}

	private ColorHelper() {
		// Not called.
	}

}
