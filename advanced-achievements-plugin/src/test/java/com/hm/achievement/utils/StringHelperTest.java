package com.hm.achievement.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Class for testing the text helpers.
 *
 * @author Pyves
 */
@ExtendWith(MockitoExtension.class)
class StringHelperTest {

	@Mock
	private Player player;
	@Mock
	private World world;

	@Test
	void shouldRemoveFormattingCodes() {
		String result = StringHelper.removeFormattingCodes("&0&1§2&3&4&5&6&7&8&9This &a&b§c&d&eis&f &ksome&l&m&n&o te&rxt!");

		assertEquals("This is some text!", result);
	}

	@Test
	void shouldNotRemoveInvalidFormattingCodes() {
		String result = StringHelper.removeFormattingCodes("Incorrect formatting codes: &h& z");

		assertEquals("Incorrect formatting codes: &h& z", result);
	}

	@Test
	void shouldReturnClosestMatchingString() {
		List<String> possibleMatches = Arrays.asList("nothing", "something", "random text", "amasing");
		String result = StringHelper.getClosestMatch("somaeThing", possibleMatches);

		assertEquals("something", result);
	}

	@Test
	void shouldReplacePlayerPlaceholders() {
		when(player.getName()).thenReturn("Pyves");
		when(player.getLocation()).thenReturn(new Location(world, 1, 5, 8));
		when(player.getWorld()).thenReturn(world);
		when(world.getName()).thenReturn("Nether");

		String result = StringHelper.replacePlayerPlaceholders(
				"Player PLAYER is in the PLAYER_WORLD at position PLAYER_X PLAYER_Y PLAYER_Z", player);

		assertEquals("Player Pyves is in the Nether at position 1 5 8", result);
	}

}
