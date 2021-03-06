package com.hm.achievement.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

class ColorHelperTest {

	@ParameterizedTest
	@EnumSource(value = ChatColor.class, mode = Mode.EXCLUDE, names = { "MAGIC", "BOLD", "STRIKETHROUGH", "UNDERLINE",
			"ITALIC", "RESET" })
	void shouldConvertChatColorsToColor(ChatColor chatColor) {
		Color color = ColorHelper.convertChatColorToColor(chatColor);

		java.awt.Color javaColor = chatColor.asBungee().getColor();
		assertEquals(Color.fromRGB(javaColor.getRed(), javaColor.getGreen(), javaColor.getBlue()), color);
	}

	@ParameterizedTest
	@EnumSource(value = ChatColor.class, mode = Mode.INCLUDE, names = { "MAGIC", "BOLD", "STRIKETHROUGH", "UNDERLINE",
			"ITALIC", "RESET" })
	void shouldConvertChatFormatsToWhite(ChatColor chatColor) {
		Color color = ColorHelper.convertChatColorToColor(chatColor);

		assertEquals(Color.WHITE, color);
	}

}
