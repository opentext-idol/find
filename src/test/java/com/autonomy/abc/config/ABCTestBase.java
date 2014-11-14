package com.autonomy.abc.config;

import com.autonomy.abc.selenium.menubar.MainTabBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.util.I18nRequestHandler;
import com.autonomy.abc.selenium.util.ImplicitWaits;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;


@Ignore
@RunWith(Parameterized.class)
public abstract class ABCTestBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(ABCTestBase.class);

	public final TestConfig config;
	private final String browser;
	private final Platform platform;
	private I18nRequestHandler i18nHandler;
	private WebDriver driver;
	public AppBody body;
	protected MainTabBar tabBar;

	public ABCTestBase(final TestConfig config, final String browser, final Platform platform) {
		this.config = config;
		this.browser = browser;
		this.platform = platform;
	}

	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() throws MalformedURLException {
		final List<Object[]> parameters = new ArrayList<>();

		parameters.add(new Object[] {
				new TestConfig(parameters.size()),
				"firefox",
				Platform.WINDOWS
		});

		parameters.add(new Object[]{
				new TestConfig(parameters.size()),
				"internet explorer",
				Platform.WINDOWS
		});

		parameters.add(new Object[]{
				new TestConfig(parameters.size()),
				"chrome",
				Platform.WINDOWS
		});

		return parameters;
	}

	@Before
	public void baseSetUp() throws MalformedURLException {
		LOGGER.info("parameter-set: [" + config.getIndex() + "]; browser: " + browser + "; platform: " + platform);
		driver = config.createWebDriver(browser, platform);
		ImplicitWaits.setImplicitWait(driver);
		i18nHandler = new I18nRequestHandler(driver);
		driver.get(config.getWebappUrl());
		getDriver().manage().window().maximize();
		abcLogin("richard", "q");
		body = new AppBody(driver);
		tabBar = new MainTabBar(driver);
	}

	@After
	public void baseTearDown() {
		driver.quit();
	}

	public WebDriver getDriver() {
		return driver;
	}

	public TestConfig getConfig() {
		return config;
	}

	public void abcLogin(final String userName, final String password) {
		driver.findElement(By.cssSelector("[name='username']")).clear();
		driver.findElement(By.cssSelector("[name='username']")).sendKeys(userName);
		driver.findElement(By.cssSelector("[name='password']")).clear();
		driver.findElement(By.cssSelector("[name='password']")).sendKeys(password);
		driver.findElement(By.cssSelector("[type='submit']")).click();
	}
}

