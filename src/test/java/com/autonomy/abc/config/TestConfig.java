package com.autonomy.abc.config;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

public class TestConfig {

	private final int index;
	private final URL url;

	public TestConfig(final int index) throws MalformedURLException {
		this.index = index;
		this.url = new URL(System.getProperty("com.autonomy.hubUrl"));
	}

	public String getWebappUrl() {
		return "http://localhost:8080/searchoptimizer/p/";
	}

	public int getIndex() {
		return index;
	}

	public WebDriver createWebDriver(final String browser, final Platform platform) {
		final DesiredCapabilities capabilities;

		switch (browser) {
			case "firefox":
				capabilities = DesiredCapabilities.firefox();
				break;
			case "internet explorer":
				capabilities = DesiredCapabilities.internetExplorer();
				break;
			case "chrome":
				capabilities = DesiredCapabilities.chrome();
				final ChromeOptions options = new ChromeOptions();
				options.addArguments("--lang=en_GB");
				options.addArguments("--start-maximized");
				capabilities.setCapability(ChromeOptions.CAPABILITY, options);
				break;
			case "opera":
				capabilities = DesiredCapabilities.opera();
				break;
			default:
				throw new IllegalArgumentException("bad value for parameter browser: " + (browser == null ? "null" : browser));
		}

		capabilities.setBrowserName(browser);
		capabilities.setPlatform(platform);
		return new RemoteWebDriver(this.url, capabilities);
	}

}
