package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.element.ModalView;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.SideNavBar;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.util.AbstractMainPagePlaceholder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class UsersPage extends AppElement implements AppPage {

	public UsersPage(final TopNavBar topNavBar, final WebElement $el) {
		super($el, topNavBar.getDriver());
	}

	@Override
	public void navigateToPage() { getDriver().get("users"); }

	public WebElement createUserButton() {
		return findElement(By.cssSelector("#create-user"));
	}

	public WebElement createButton() {
		return ModalView.getVisibleModalView(getDriver()).findElement(By.xpath(".//button[contains(text(), 'Create')]"));
	}

	public void addUsername(final String userName) {
		ModalView.getVisibleModalView(getDriver()).findElement(By.cssSelector("[name='create-users-username']")).clear();
		ModalView.getVisibleModalView(getDriver()).findElement(By.cssSelector("[name='create-users-username']")).sendKeys(userName);
	}

	public void clearPasswords() {
		ModalView.getVisibleModalView(getDriver()).findElement(By.cssSelector("[name='create-users-password']")).clear();
		ModalView.getVisibleModalView(getDriver()).findElement(By.cssSelector("[name='create-users-passwordConfirm']")).clear();
	}

	public void addAndConfirmPassword(final String password, final String passwordConfirm) {
		final WebElement passwordElement = ModalView.getVisibleModalView(getDriver()).findElement(By.cssSelector("[name='create-users-password']"));
		passwordElement.clear();
		passwordElement.sendKeys(password);

		final WebElement passwordConfirmElement = ModalView.getVisibleModalView(getDriver()).findElement(By.cssSelector("[name='create-users-passwordConfirm']"));
		passwordConfirmElement.clear();
		passwordConfirmElement.sendKeys(passwordConfirm);
	}

	public void createNewUser(final String userName, final String password, final String userLevel) {
		addUsername(userName);
		addAndConfirmPassword(password, password);
		ModalView.getVisibleModalView(getDriver()).findElement(By.xpath(".//option[text() = '" + userLevel + "']")).click();
		createButton().click();
		loadOrFadeWait();
	}

	public void closeModal() {
		ModalView.getVisibleModalView(getDriver()).findElement(By.cssSelector("[data-dismiss='modal']")).click();
		loadOrFadeWait();
	}

	public void deleteOtherUsers() {
		for (final WebElement deleteButton : getTable().findElements(By.cssSelector("button"))) {
			if (!deleteButton.getAttribute("class").contains("disabled")) {
				loadOrFadeWait();
				deleteButton.click();
				loadOrFadeWait();
				findElement(By.cssSelector(".popover-content .users-delete-confirm")).click();
			}
		}
	}

	public int countNumberOfUsers() {
		return getTable().findElements(By.cssSelector("tbody tr")).size();
	}

	public void deleteUser(final String userName) {
		loadOrFadeWait();
		deleteButton(userName).click();
		loadOrFadeWait();
		findElement(By.cssSelector(".popover-content .users-delete-confirm")).click();
	}

	public WebElement deleteButton(final String userName) {
		return getUserRow(userName).findElement(By.cssSelector("button"));
	}

	public WebElement getTable() {
		return findElement(By.cssSelector("#users-current-admins"));
	}

	public WebElement getTableUserTypeLink(final String userName) {
		return getUserRow(userName).findElement(By.cssSelector(".role"));
	}

	public void selectTableUserType(final String userName, final String type) {
		getUserRow(userName).findElement(By.cssSelector(".input-admin")).findElement(By.xpath(".//*[text() = '" + type + "']")).click();
	}

	public WebElement getTableUserPasswordLink(final String userName) {
		return getUserRow(userName).findElement(By.cssSelector(".pw"));
	}

	public WebElement getTableUserPasswordBox(final String userName) {
		return getUserRow(userName).findElement(By.cssSelector("[type='password']"));
	}

	public WebElement getUserRow(final String userName) {
		return findElement(By.xpath(".//span[contains(text(), '" + userName + "')]/../../.."));
	}

	public void changePassword(final String userName, final String newPassword) {
		getTableUserPasswordLink(userName).click();
		getTableUserPasswordBox(userName).clear();
		getTableUserPasswordBox(userName).sendKeys(newPassword);
		getUserRow(userName).findElement(By.cssSelector(".editable-submit")).click();
	}

	public String getSignedInUserName() {
		return findElement(By.cssSelector(".profile-element strong")).getText();
	}

	public static class Placeholder extends AbstractMainPagePlaceholder<UsersPage> {

		public Placeholder(final AppBody body, final SideNavBar mainTabBar, final TopNavBar topNavBar) {
			super(body, mainTabBar, topNavBar, "users", NavBarTabId.USERS_PAGE, false);
		}

		@Override
		protected UsersPage convertToActualType(final WebElement element) {
			return new UsersPage(topNavBar, element);
		}

	}
}
