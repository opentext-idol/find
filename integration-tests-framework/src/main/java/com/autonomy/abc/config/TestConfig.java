package com.autonomy.abc.config;

import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.users.NewUser;
import com.autonomy.abc.selenium.users.User;
import com.autonomy.abc.selenium.util.Factory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestConfig {
	private final static String BASE_CONFIG_LOCATION = System.getProperty("com.autonomy.baseConfig");
	private final static String OVERRIDE_CONFIG_LOCATION = System.getProperty("com.autonomy.configFile");
	// system property set in the POM
	private final static boolean MAVEN = System.getProperty("com.autonomy.mavenFlag") != null;
	private final static Logger LOGGER = LoggerFactory.getLogger(TestConfig.class);

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

	public String getApiUrl() {
		return jsonConfig.getApiUrl().toString();
	}

	public User getDefaultUser() {
		return getUser("default");
	}

	public User getUser(String name){
		User user = jsonConfig.getUser(name);
		if (user == null) {
			throw new IllegalStateException("User with name " + name + " not found in config file");
		}
		return user;
	}

	public NewUser getNewUser(String name) {
		NewUser newUser = jsonConfig.getNewUser(name);
		if (newUser == null) {
			throw new IllegalStateException("NewUser with name " + name + " not found in config file");
		}
		return newUser;
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

	URL getHubUrl() {
		return url;
	}

	public WebDriverFactory getWebDriverFactory() {
		return new WebDriverFactory(this);
	}

	public Factory<NewUser> getNewUserFactory() {
		return new RandomNewUserFactory(this);
	}

	// used when running whole suite via mvn
	private static JsonConfig getMavenConfig(String path) throws IOException {
		if (path == null) {
			return null;
		}
		LOGGER.info("using config " + path);
		return JsonConfig.fromURL(TestConfig.class.getClassLoader().getResource(path));
	}

	// used when running single tests via IDE
	private static JsonConfig getLocalConfig(String path) throws IOException {
		if (path == null) {
			return null;
		}
		LOGGER.info("using config " + path);
		return JsonConfig.readFile(new File(path));
	}

	public static List<Object[]> readConfigs(final Collection<ApplicationType> applicationTypes) throws IOException {
		List<Object[]> configs = new ArrayList<>();

		JsonConfig defaultConfig;
		JsonConfig userSpecifiedConfig;
		if (MAVEN) {
			defaultConfig = getMavenConfig(BASE_CONFIG_LOCATION);
			userSpecifiedConfig = getMavenConfig(OVERRIDE_CONFIG_LOCATION);
		} else {
			String basePath = BASE_CONFIG_LOCATION == null ? "../config/hsod-dev.json" : BASE_CONFIG_LOCATION;
			defaultConfig = getLocalConfig(basePath);
			userSpecifiedConfig = getLocalConfig(OVERRIDE_CONFIG_LOCATION);
		}

		JsonConfig jsonConfig = defaultConfig.overrideUsing(userSpecifiedConfig);

		if (applicationTypes.contains(jsonConfig.getAppType())) {
			for (int i = 0; i < jsonConfig.getBrowsers().size(); i++) {
				TestConfig config = new TestConfig(i, jsonConfig);
				// for compatibility
				configs.add(new Object[]{
						config
				});
			}
		}
		return configs;
	}

	public static JsonNode getRawBaseConfig() throws IOException {
		if (MAVEN) {
			return new ObjectMapper().readTree(TestConfig.class.getClassLoader().getResource(BASE_CONFIG_LOCATION));
		} else {
			return new ObjectMapper().readTree(new File(BASE_CONFIG_LOCATION));
		}
	}

	@Override
	public String toString() {
		return "parameter-set: [" + getIndex() + "]; browser: " + getBrowser() + "; platform: " + getPlatform() + "; effective config: " + jsonConfig;
	}
}


