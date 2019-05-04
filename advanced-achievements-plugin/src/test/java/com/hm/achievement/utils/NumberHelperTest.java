package com.hm.achievement.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NumberHelperTest {

	@Test
	public void shouldComputeNextMultipleOf9() {
		assertEquals(18, NumberHelper.nextMultipleOf9(17));
		assertEquals(18, NumberHelper.nextMultipleOf9(18));
		assertEquals(27, NumberHelper.nextMultipleOf9(19));
	}

}
