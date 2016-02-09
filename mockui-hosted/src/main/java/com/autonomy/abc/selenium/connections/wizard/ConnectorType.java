package com.autonomy.abc.selenium.connections.wizard;

import org.openqa.selenium.By;

public enum ConnectorType {
    WEB("web_cloud"),
    FILESYSTEM("filesystem_onsite"),
    SHAREPOINT("sharepoint_onsite"),
    DROPBOX("dropbox_cloud");

    private final By locator;
    ConnectorType(String idLocator) {
        locator = By.id(idLocator);
    }

    By getLocator() {
        return locator;
    }
}
