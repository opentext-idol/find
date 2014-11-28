package com.autonomy.abc.selenium.menubar;


public enum NavBarTabId {
	PROMOTIONS("promotions"),
	OVERVIEW("overview"),
	KEYWORDS("keywords"),
	ABOUT_PAGE("about"),
	USERS_PAGE("users");

	private final String tabName;

	NavBarTabId(final String name) {
		tabName = name;
	}

	public String toString() {
		return tabName;
	}
}
