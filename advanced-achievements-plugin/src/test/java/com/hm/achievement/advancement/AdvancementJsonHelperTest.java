package com.hm.achievement.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hm.achievement.advancement.AchievementAdvancement.AchievementAdvancementBuilder;

/**
 * Class for testing advancement JSON strings.
 *
 * @author Pyves
 */
class AdvancementJsonHelperTest {

	@Test
	void shouldGenerateAdvancementJson() {
		AchievementAdvancement aa = new AchievementAdvancementBuilder().iconItem("minecraft:dirt")
				.iconData("0").title("Special Event Achievement!").description("You took part in the \"Special Event\"!")
				.parent("advancedachievements:advanced_achievements_parent").type(AdvancementType.TASK).build();

		assertEquals("{\n" +
				"  \"criteria\":{\n" +
				"    \"aach_handled\":{\n" +
				"      \"trigger\":\"minecraft:impossible\"\n" +
				"    }\n" +
				"  },\n" +
				"  \"requirements\":[\n" +
				"    [\n" +
				"      \"aach_handled\"\n" +
				"    ]\n" +
				"  ],\n" +
				"  \"display\":{\n" +
				"    \"icon\":{\n" +
				"      \"item\":\"minecraft:dirt\",\"data\":0\n" +
				"    },\n" +
				"    \"title\":\"Special Event Achievement!\",\n" +
				"    \"description\":\"You took part in the \\\"Special Event\\\"!\",\n" +
				"    \"frame\":\"task\",\n" +
				"    \"announce_to_chat\":false\n" +
				"  },\n" +
				"  \"parent\":\"advancedachievements:advanced_achievements_parent\"\n" +
				"}\n", AdvancementJsonHelper.toJson(aa));
	}

	@Test
	void shouldGenerateParentAdvancementJson() {
		AchievementAdvancement aa = new AchievementAdvancementBuilder().iconItem("minecraft:dirt")
				.iconData("0").title("Special Event Achievement!").description("You took part in the special event!")
				.background("minecraft:book").type(AdvancementType.GOAL).build();

		assertEquals("{\n" +
				"  \"criteria\":{\n" +
				"    \"aach_handled\":{\n" +
				"      \"trigger\":\"minecraft:impossible\"\n" +
				"    }\n" +
				"  },\n" +
				"  \"requirements\":[\n" +
				"    [\n" +
				"      \"aach_handled\"\n" +
				"    ]\n" +
				"  ],\n" +
				"  \"display\":{\n" +
				"    \"icon\":{\n" +
				"      \"item\":\"minecraft:dirt\",\"data\":0\n" +
				"    },\n" +
				"    \"title\":\"Special Event Achievement!\",\n" +
				"    \"description\":\"You took part in the special event!\",\n" +
				"    \"frame\":\"goal\",\n" +
				"    \"announce_to_chat\":false,\n" +
				"    \"background\":\"minecraft:book\"\n" +
				"  }\n" +
				"}\n", AdvancementJsonHelper.toJson(aa));
	}

	@Test
	void shouldGenerateHiddenParentAdvancementJson() {
		assertEquals("{\n" +
				"  \"criteria\":{\n" +
				"    \"aach_handled\":{\n" +
				"      \"trigger\":\"minecraft:impossible\"\n" +
				"    }\n" +
				"  },\n" +
				"  \"requirements\":[\n" +
				"    [\n" +
				"      \"aach_handled\"\n" +
				"    ]\n" +
				"  ],\n" +
				"  \"background\":\"minecraft:book\"\n" +
				"}\n", AdvancementJsonHelper.toHiddenJson("minecraft:book"));
	}

}
