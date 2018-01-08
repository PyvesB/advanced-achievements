package com.hm.achievement.lang;

public enum GuiLang implements Lang {
	LIST_ACHIEVEMENTS_IN_CATEGORY_SINGULAR("AMOUNT achievement"),
	LIST_ACHIEVEMENTS_IN_CATEGORY_PLURAL("AMOUNT achievements");

	private final String defaultMessage;

	GuiLang(String defaultMessage) {
		this.defaultMessage = defaultMessage;
	}


	@Override
	public String toLangKey() {
		return Lang.toPath(name());
	}

	@Override
	public String toLangDefault() {
		return defaultMessage;
	}
}
