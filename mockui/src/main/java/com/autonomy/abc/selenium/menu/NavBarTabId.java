package com.autonomy.abc.selenium.menu;

public enum NavBarTabId {
    ANALYTICS("Analytics"),
    SEARCH("Search"),
    CONNECTIONS("Connections"),
    INDEXES("Indexes"),
    PROMOTIONS("Promotions"),
    KEYWORDS("Keywords"),
    GETTING_STARTED("Getting Started"),
    USER_MGMT("User Management");
    OVERVIEW("Overview");

    private final String tabName;

    NavBarTabId(final String name) {
        tabName = name;
    }

    public String toString() {
        return tabName;
    }
}
