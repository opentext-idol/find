package com.autonomy.abc.config;

import com.autonomy.abc.selenium.util.Factory;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;

import java.net.URL;

public class WebDriverFactory implements Factory<WebDriver> {
    private final Browser browser;
    private final URL url;
    private final Platform platform;

    WebDriverFactory(TestConfig config) {
        browser = config.getBrowser();
        url = config.getHubUrl();
        platform = config.getPlatform();
    }

    @Override
    public WebDriver create() {
        return browser.createWebDriver(url, platform);
    }
}
