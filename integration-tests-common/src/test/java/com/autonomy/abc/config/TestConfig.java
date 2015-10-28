package com.autonomy.abc.config;

import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.users.NewUser;
import com.autonomy.abc.selenium.users.User;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestConfig {
	private final static File BASE_CONFIG_LOCATION;
	private final static File USER_CONFIG_LOCATION;
	private final static Logger LOGGER = LoggerFactory.getLogger(TestConfig.class);

	static {
		String baseConfig = System.getProperty("com.autonomy.baseConfig");
		if (baseConfig == null) {
			// running in IDE
			BASE_CONFIG_LOCATION = new File("../config/default.json");
		} else {
			// running via mvn, or specified config
			BASE_CONFIG_LOCATION = new File(baseConfig);
		}
		LOGGER.info("Using base config " + BASE_CONFIG_LOCATION);
		String configFile = System.getProperty("com.autonomy.configFile");
		USER_CONFIG_LOCATION = (configFile == null) ? null : new File(configFile);
		LOGGER.info("...overridden by " + USER_CONFIG_LOCATION);
	}

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

	public String getFindUrl() {
		return jsonConfig.getFindUrl().toString();
	}

	public User getDefaultUser() {
		return jsonConfig.getDefaultUser();
	}

	public NewUser getNewUser(String name) {
		return jsonConfig.getNewUser(name);
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
		JsonConfig defaultConfig = JsonConfig.readFile(BASE_CONFIG_LOCATION);
		JsonConfig userSpecifiedConfig = JsonConfig.readFile(USER_CONFIG_LOCATION);
		JsonConfig jsonConfig = defaultConfig.overrideUsing(userSpecifiedConfig);
		LOGGER.info("Effective config: " + jsonConfig);

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


