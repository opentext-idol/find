package com.autonomy.abc.selenium.page.login;

import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.page.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class LoginOnPremisePage extends AppElement implements AppPage {

	public LoginOnPremisePage(final TopNavBar topNavBar, final WebElement $el) {
		super($el, topNavBar.getDriver());
	}

	@Override
	public void navigateToPage() { getDriver().get("searchoptimizer/login/index.html");
	}

	public WebElement usernameInput() {
		return findElement(By.cssSelector("[placeholder='Username']"));
	}

	public WebElement passwordInput() {
		return findElement(By.cssSelector("[placeholder='Password']"));
	}

	public WebElement loginButton() {
		return findElement(By.xpath(".//button"));
	}

	public void login(final String userName, final String password) {
		usernameInput().clear();
		usernameInput().sendKeys(userName);
		passwordInput().clear();
		passwordInput().sendKeys(password);
		loginButton().click();
		loadOrFadeWait();
	}

	public static class Placeholder {
		private final TopNavBar topNavBar;

		public Placeholder(final TopNavBar topNavBar) {
			this.topNavBar = topNavBar;
		}

		public LoginOnPremisePage $loginPage(final WebElement element) {
			return new LoginOnPremisePage(topNavBar, element);
		}
	}
}
