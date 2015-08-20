package com.autonomy.abc.selenium.page.login;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.config.Timeouts;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginHostedPage extends AppElement {
	private static Logger logger = LoggerFactory.getLogger(LoginHostedPage.class);

	public LoginHostedPage(final WebElement element, final WebDriver driver) {
		super(element, driver);
	}

    public LoginHostedPage(final WebDriver driver) {
        this(new WebDriverWait(driver, Timeouts.LOGIN_PAGE_LOAD).until(ExpectedConditions.visibilityOfElementLocated(By.className("login-body"))), driver);
    }

	public void loginWith(final AuthProvider provider) {
		provider.login(this);
		if (!hasLoggedIn()) {
			logger.warn("Initial login attempt failed, trying again");
			provider.login(this);
		}
	}

	private boolean hasLoggedIn() {
		try {
			new WebDriverWait(getDriver(), Timeouts.LOGIN_SUBMITTED).until(ExpectedConditions.visibilityOfElementLocated(By.className("navbar-static-top-blue")));
		} catch (TimeoutException e) {
			return false;
		}
		return true;
	}
}