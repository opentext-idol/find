package com.autonomy.abc.selenium.page.admin;

import com.autonomy.abc.selenium.element.PasswordBox;
import com.autonomy.abc.selenium.users.NewUser;
import com.autonomy.abc.selenium.users.Role;
import com.autonomy.abc.selenium.users.User;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class UsersPage extends AppElement implements AppPage {

	private UsersPage(final WebDriver driver) {
		super(driver.findElement(By.cssSelector(".wrapper-content")), driver);
	}

	public static UsersPage make(final WebDriver driver) {
		UsersPage.waitForLoad(driver);
		return new UsersPage(driver);
	}

	public WebElement createUserButton() {
		return findElement(By.xpath(".//button[contains(text(), 'Create User')]"));
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

	public void selectRole(Role role) {
		ModalView.getVisibleModalView(getDriver()).findElement(By.xpath(".//option[text() = '" + role + "']")).click();
	}

	public void createNewUser(final String userName, final String password, final String userLevel) {
		loadOrFadeWait();
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

	// TODO: move to UserService.deleteOtherUsers()?
	public void deleteOtherUsers() {
		for (final WebElement deleteButton : getTable().findElements(By.cssSelector("button"))) {
			if (!isAttributePresent(deleteButton, "disabled")) {
				loadOrFadeWait();
				deleteButton.click();
				loadOrFadeWait();
				findElement(By.cssSelector(".popover-content .users-delete-confirm")).click();
			}
		}
	}

	public int countNumberOfUsers() {
		loadOrFadeWait();
		return getTable().findElements(By.cssSelector("tbody tr")).size();
	}

	// TODO: move to UserService.deleteUser(User)?
	public void deleteUser(final String userName) {
		loadOrFadeWait();
		deleteButton(userName).click();
		loadOrFadeWait();
		findElement(By.cssSelector(".popover-content .users-delete-confirm")).click();
		loadOrFadeWait();
	}

	public WebElement deleteButton(final String userName) {
		return getUserRow(userName).findElement(By.cssSelector(".users-deleteUser"));
	}

	public WebElement getTable() {
		return findElement(By.cssSelector("#users-current-admins"));
	}

	@Deprecated
	public WebElement getTableUserTypeLink(final String userName) {
		return getUserRow(userName).findElement(By.cssSelector(".role"));
	}

	public List<String> getUsernames() {
		List<String> usernames = new ArrayList<>();
		for (WebElement element : getTable().findElements(By.cssSelector("tbody .user"))) {
			usernames.add(element.getText().trim());
		}
		return usernames;
	}

	public Role getRoleOf(User user) {
		return Role.fromString(getTableUserTypeLink(user.getUsername()).getText());
	}

	public WebElement passwordLinkFor(User user) {
		return getTableUserPasswordLink(user.getUsername());
	}

	public PasswordBox passwordBoxFor(User user) {
		return new PasswordBox(getTableUserPasswordBox(user.getUsername()), getDriver());
	}

	public WebElement roleLinkFor(User user) {
		return getTableUserTypeLink(user.getUsername());
	}

	public void setRoleValueFor(User user, Role newRole) {
		selectTableUserType(user.getUsername(), newRole.toString());
	}

	public void cancelPendingEditFor(User user) {
		getUserRow(user.getUsername()).findElement(By.cssSelector(".editable-cancel")).click();
	}

	public void submitPendingEditFor(User user) {
		getUserRow(user.getUsername()).findElement(By.cssSelector(".editable-submit")).click();
	}

	public void changeRole(User user, Role newRole) {
		roleLinkFor(user).click();
		setRoleValueFor(user, newRole);
		submitPendingEditFor(user);
	}

	public User changeAuth(User user, NewUser replacementAuth) {
		return replacementAuth.replaceAuthFor(user, this);
	}

	public void selectTableUserType(final String userName, final String type) {
		getUserRow(userName).findElement(By.cssSelector(".input-admin")).findElement(By.xpath(".//*[text() = '" + type + "']")).click();
	}

	@Deprecated
	public WebElement getTableUserPasswordLink(final String userName) {
		return getUserRow(userName).findElement(By.cssSelector(".pw"));
	}

	@Deprecated
	public WebElement getTableUserPasswordBox(final String userName) {
		return getUserRow(userName).findElement(By.cssSelector("td:nth-child(2)"));
	}

	public WebElement getUserRow(final String userName) {
		return findElement(By.xpath(".//span[contains(text(), '" + userName + "')]/../.."));
	}

	public WebElement rowFor(final User user) {
		return getUserRow(user.getUsername());
	}

	public void changePassword(final String userName, final String newPassword) {
		getTableUserPasswordLink(userName).click();
		getTableUserPasswordBox(userName).clear();
		getTableUserPasswordBox(userName).sendKeys(newPassword);
		getUserRow(userName).findElement(By.cssSelector(".editable-submit")).click();
	}

	@Override
	public void waitForLoad() {
		waitForLoad(getDriver());
	}

	private static void waitForLoad(WebDriver driver) {
		new WebDriverWait(driver, 20).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".users-table")));
	}
}
