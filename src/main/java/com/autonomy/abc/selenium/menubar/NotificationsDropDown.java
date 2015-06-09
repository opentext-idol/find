package com.autonomy.abc.selenium.menubar;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.page.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class NotificationsDropDown extends AppElement implements AppPage {

	public NotificationsDropDown(final TopNavBar topNavBar, final WebElement $el) {
		super($el, topNavBar.getDriver());
	}

	@Override
	public void navigateToPage() {

	}

	public int countNotifications() {
		return getDriver().findElements(By.cssSelector(".notification-message")).size();
	}

	public WebElement notificationNumber(final int index) {
		return findElement(By.cssSelector("li:nth-child(" + (index*2 - 1) + ") a"));
	}

	public static class Placeholder {
		private final TopNavBar topNavBar;

		public Placeholder(final TopNavBar topNavBar) {
			this.topNavBar = topNavBar;
		}

		public NotificationsDropDown $notificationsDropDown(final WebElement element) {
			return new NotificationsDropDown(topNavBar, element);
		}
	}
}
