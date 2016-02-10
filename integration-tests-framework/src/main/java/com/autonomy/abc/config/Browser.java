package com.autonomy.abc.config;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public enum Browser {
    CHROME("chrome") {
        @Override
        DesiredCapabilities getCapabilities() {
            final DesiredCapabilities capabilities = DesiredCapabilities.chrome();
            final ChromeOptions options = new ChromeOptions();
            options.addArguments("--lang=en_GB");
            options.addArguments("--start-maximized");
            // avoids "Disable developer mode extensions" popup
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-popup-blocking");
            capabilities.setCapability(ChromeOptions.CAPABILITY, options);
            return capabilities;
        }
    },
    FIREFOX("firefox") {
        @Override
        DesiredCapabilities getCapabilities() {
            return DesiredCapabilities.firefox();
        }
    },
    IE("internet explorer") {
        @Override
        DesiredCapabilities getCapabilities() {
            final DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
            capabilities.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
            return capabilities;
        }
    };

    private static Map<String, Browser> lookup = new HashMap<>();
    static {
        for (Browser browser : Browser.values()) {
            lookup.put(browser.name.toLowerCase(), browser);
        }
    }

    private String name;

    Browser(String name) {
        this.name = name;
    }

    abstract DesiredCapabilities getCapabilities();

    public WebDriver createWebDriver(URL url, Platform platform) {
        final DesiredCapabilities capabilities = getCapabilities();
        final LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        capabilities.setBrowserName(this.toString());
        capabilities.setPlatform(platform);
        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        return new RemoteWebDriver(url, capabilities);
    }

    @Override
    public String toString() {
        return name;
    }

    public static Browser fromString(String name) {
        return lookup.get(name.toLowerCase());
    }
}
