package com.autonomy.abc.config;

import com.autonomy.abc.selenium.control.Resolution;
import com.autonomy.abc.selenium.util.Factory;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;

import java.net.URL;
import java.util.concurrent.TimeUnit;

class WebDriverFactory implements Factory<WebDriver> {
    private final Browser browser;
    private final URL url;
    private final Platform platform;
    private Resolution resolution;
    private int implicitWait;

    WebDriverFactory(TestConfig config) {
        browser = config.getBrowser();
        url = config.getHubUrl();
        platform = config.getPlatform();
        resolution = config.getResolution();
        implicitWait = config.getTimeout();
    }

    @Override
    public WebDriver create() {
        WebDriver driver = browser.createWebDriver(url, platform);
        driver.manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
        resolution.applyTo(driver.manage().window());
        return driver;
    }
}
