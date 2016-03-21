package com.autonomy.abc.config;

import com.autonomy.abc.config.json.JsonConfig;
import com.autonomy.abc.selenium.application.Application;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.control.Resolution;
import com.autonomy.abc.selenium.control.Session;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.users.AuthenticationStrategy;
import com.autonomy.abc.selenium.users.NewUser;
import com.autonomy.abc.selenium.users.NullAuthenticationStrategy;
import com.autonomy.abc.selenium.users.User;
import com.autonomy.abc.selenium.util.Factory;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;

import java.net.URL;
import java.util.UUID;

public class TestConfig {
	private final static int DEFAULT_TIMEOUT = 10;

	private final JsonConfig jsonConfig;
	private final Platform platform;
	private final Browser browser;

	public TestConfig(final JsonConfig config, final Browser browser) {
		this.jsonConfig = config;
		this.platform = Platform.WINDOWS;
		this.browser = browser;
	}

	public String getAppUrl(Application<?> application) {
		return getAppUrl(application.getName());
	}

	public String getAppUrl(String appName) {
		return jsonConfig.getAppUrl(appName.toLowerCase()).toString();
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

	public ApplicationType getType() {
		return jsonConfig.getAppType();
	}

	URL getHubUrl() {
		return jsonConfig.getHubUrl();
	}

	public Factory<WebDriver> getWebDriverFactory() {
		return new WebDriverFactory(this);
	}

	public ParametrizedFactory<Session, Window> getWindowFactory() {
		return new WindowFactory(this);
	}

	public NewUser generateNewUser() {
		return jsonConfig.generateRandomNewUser(UUID.randomUUID().toString().replaceAll("-", ""));
	}

	public AuthenticationStrategy getAuthenticationStrategy() {
		AuthenticationStrategy strategy = jsonConfig.getAuthenticationStrategy(getWebDriverFactory());
		return strategy == null ? NullAuthenticationStrategy.getInstance() : strategy;
	}

	@Override
	public String toString() {
		return "browser: " + getBrowser() + "; platform: " + getPlatform() + "; effective config: " + jsonConfig;
	}

	Resolution getResolution() {
		Resolution fromConfig = jsonConfig.getResolution();
		return fromConfig == null ? Resolution.MAXIMIZED : fromConfig;
	}

	public int getTimeout() {
		int timeout = jsonConfig.getTimeout();
		if (timeout >= 0) {
			return timeout;
		}
		return DEFAULT_TIMEOUT;
	}
}
