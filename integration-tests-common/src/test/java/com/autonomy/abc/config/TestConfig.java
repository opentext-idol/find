package com.autonomy.abc.config;

import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.config.Browser;
import com.autonomy.abc.selenium.users.User;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestConfig {
	private final static File DEFAULT_CONFIG_LOCATION = new File("../config/default.json");
	private final static File USER_CONFIG_LOCATION = new File(System.getProperty("com.autonomy.configFile"));

	private final JsonConfig jsonConfig;
	private final int index;
	private final ApplicationType type;
	private final URL url;
	private final Platform platform;
	private final Browser browser;

	private TestConfig(final int index, final JsonConfig config) {
		this.jsonConfig = config;
		this.index = index;
		this.type = jsonConfig.getAppType();
		this.url = jsonConfig.getHubUrl();
		this.platform = Platform.WINDOWS;
		this.browser = jsonConfig.getBrowsers().get(index);
	}

	public String getWebappUrl() {
		return jsonConfig.getWebappUrl().toString();
	}

	public User getDefaultUser() {
		return jsonConfig.getDefaultUser();
	}

	public Browser getBrowser() {
		return browser;
	}

	public Platform getPlatform() {
		return platform;
	}

	public int getIndex() {
		return index;
	}

	public ApplicationType getType() {
		return type;
	}

	public WebDriver createWebDriver(final Platform platform) {
		return browser.createWebDriver(url, platform);
	}

	public static List<Object[]> readConfigs(final Collection<ApplicationType> applicationTypes) throws IOException {
		List<Object[]> configs = new ArrayList<>();
		JsonConfig defaultConfig = JsonConfig.readFile(DEFAULT_CONFIG_LOCATION);
		JsonConfig userSpecifiedConfig = JsonConfig.readFile(USER_CONFIG_LOCATION);
		JsonConfig jsonConfig = defaultConfig.overrideUsing(userSpecifiedConfig);
		System.out.println(defaultConfig);
		System.out.println(userSpecifiedConfig);
		System.out.println(jsonConfig);


		if (applicationTypes.contains(jsonConfig.getAppType())) {
			for (int i = 0; i < jsonConfig.getBrowsers().size(); i++) {
				TestConfig config = new TestConfig(i, jsonConfig);
				// for compatibility
				configs.add(new Object[]{
						config,
						config.getBrowser().toString(),
						config.getType(),
						config.getPlatform()
				});
			}
		}
		return configs;
	}

}


