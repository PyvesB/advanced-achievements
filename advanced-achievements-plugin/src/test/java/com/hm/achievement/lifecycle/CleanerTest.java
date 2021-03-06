package com.hm.achievement.lifecycle;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CleanerTest {

	@Mock
	private Cleanable cleanable1;
	@Mock
	private Cleanable cleanable2;

	@Test
	void shouldCleanAllPlayerData() {
		Set<Cleanable> cleanables = new HashSet<>();
		cleanables.add(cleanable1);
		cleanables.add(cleanable2);
		Cleaner underTest = new Cleaner(cleanables);

		underTest.run();

		verify(cleanable1).cleanPlayerData();
		verify(cleanable2).cleanPlayerData();
		verifyNoMoreInteractions(cleanable1, cleanable2);
	}

}
