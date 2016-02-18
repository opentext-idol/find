package com.autonomy.abc.config;

import com.autonomy.abc.selenium.util.Factory;
import com.autonomy.abc.selenium.util.ImplicitWaits;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;

import java.net.URL;

class WebDriverFactory implements Factory<WebDriver> {
    private final Browser browser;
    private final URL url;
    private final Platform platform;
    private Dimension maxSize;

    WebDriverFactory(TestConfig config) {
        browser = config.getBrowser();
        url = config.getHubUrl();
        platform = config.getPlatform();
        maxSize = config.getResolution();
    }

    @Override
    public WebDriver create() {
        WebDriver driver = browser.createWebDriver(url, platform);
        ImplicitWaits.setImplicitWait(driver);
        if (maxSize == null) {
            driver.manage().window().maximize();
        } else {
            driver.manage().window().setSize(maxSize);
        }
        return driver;
    }
}
