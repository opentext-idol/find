package com.autonomy.abc.selenium.page.login;

import com.autonomy.abc.selenium.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LoginHostedPage extends AppElement {
	public LoginHostedPage(final WebElement element, final WebDriver driver) {
		super(element, driver);
	}

	public void loginWith(final LoginProviders provider) {
		/*
		 * Try catches necessary because of inconsistent loading of IOD SSO page,
		 * some elements aren't loaded when the signal to selenium is given that the page is loaded.
		 */

		loadOrFadeWait();
		try {
			getDriver().findElement(By.xpath(".//a[text()[contains(., '" + provider.toString() + "')]]")).click();
		} catch (final Exception e) {
			try {
				Thread.sleep(5000);
				getDriver().findElement(By.xpath(".//a[text()[contains(., '" + provider.toString() + "')]]")).click();
			} catch (final InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		loadOrFadeWait();
	}

	public enum LoginProviders {

		GOOGLE("Google"),
		FACEBOOK("Facebook"),
		TWITTER("Twitter"),
		YAHOO("Yahoo"),
		HP_PASSPORT("HP Passport"),
		OPEN_ID("Open ID"),
		API_KEY("APIKey");

		private final String provider;

		LoginProviders(final String name) {
			provider = name;
		}

		public String toString() {
			return provider;
		}
	}
}
