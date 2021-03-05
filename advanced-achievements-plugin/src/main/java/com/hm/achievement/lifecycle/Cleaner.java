package com.hm.achievement.lifecycle;

import java.util.Set;

import javax.inject.Inject;

public class Cleaner implements Runnable {

	private final Set<Cleanable> cleanables;

	@Inject
	public Cleaner(Set<Cleanable> cleanables) {
		this.cleanables = cleanables;
	}

	@Override
	public void run() {
		cleanables.forEach(Cleanable::cleanPlayerData);
	}

}
