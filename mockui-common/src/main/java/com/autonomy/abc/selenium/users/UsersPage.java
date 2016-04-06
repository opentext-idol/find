package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.application.SOPageBase;
import com.hp.autonomy.frontend.selenium.element.Dropdown;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.element.PasswordBox;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public abstract class UsersPage extends SOPageBase {

	protected UsersPage(final WebDriver driver) {
		super(driver.findElement(By.cssSelector(".wrapper-content")), driver);
	}
	
	public abstract User addNewUser(NewUser newUser, Role role);

	public WebElement createUserButton() {
		return findElement(By.id("create-user"));
	}

	public WebElement createButton() {
		return ModalView.getVisibleModalView(getDriver()).findElement(By.xpath(".//button[contains(text(), 'Create')]"));
	}

	public void addUsername(final String userName) {
		ModalView.getVisibleModalView(getDriver()).findElement(By.cssSelector("[name='create-users-username']")).clear();
		ModalView.getVisibleModalView(getDriver()).findElement(By.cssSelector("[name='create-users-username']")).sendKeys(userName);
	}

	public void addAndConfirmPassword(final String password, final String passwordConfirm) {
		final WebElement passwordElement = ModalView.getVisibleModalView(getDriver()).findElement(By.id("create-users-password"));
		passwordElement.clear();
		passwordElement.sendKeys(password);

		final WebElement passwordConfirmElement = ModalView.getVisibleModalView(getDriver()).findElement(By.id("create-users-passwordConfirm"));
		passwordConfirmElement.clear();
		passwordConfirmElement.sendKeys(passwordConfirm);
	}

	public void selectRole(Role role) {
		ModalView.getVisibleModalView(getDriver()).findElement(By.xpath(".//option[contains(text(),'" + role + "')]")).click();
	}

	@Deprecated
	/**
	 * @deprecated Use UserService instead
	 */
	public void createNewUser(final String userName, final String password, final String userLevel) {
		Waits.loadOrFadeWait();
		addUsername(userName);
		addAndConfirmPassword(password, password);
		ModalView.getVisibleModalView(getDriver()).findElement(By.xpath(".//option[text() = '" + userLevel + "']")).click();
		createButton().click();
		Waits.loadOrFadeWait();
	}

	public void closeModal() {
		ModalView.getVisibleModalView(getDriver()).findElement(By.cssSelector("[data-dismiss='modal']")).click();
		Waits.loadOrFadeWait();
	}

	public int countNumberOfUsers() {
		Waits.loadOrFadeWait();
		return getTable().findElements(By.cssSelector("tbody tr")).size();
	}

	public WebElement deleteButton(User user){
		return getUserRow(user).findElement(By.cssSelector(".users-deleteUser"));
	}

	public WebElement getTable() {
		return findElement(By.cssSelector("#users-current-admins"));
	}

	public List<String> getUsernames() {
		List<String> usernames = new ArrayList<>();
		for (WebElement element : getTable().findElements(By.cssSelector("tbody .user-username"))) {
			usernames.add(element.getText().trim());
		}
		return usernames;
	}

	public abstract WebElement roleLinkFor(User user);
	public abstract void setRoleValueFor(User user, Role newRole);

	public Role getRoleOf(User user) {
		return Role.fromString(roleLinkFor(user).getText());
	}

	public PasswordBox passwordBoxFor(User user) {
		return new PasswordBox(getUserRow(user).findElement(By.cssSelector("td:nth-child(2)")), getDriver());
	}

	public void selectTableUserType(final User user, final String type) {
		getUserRow(user).findElement(By.cssSelector(".input-admin")).findElement(By.xpath(".//*[text() = '" + type + "']")).click();
	}

	public abstract WebElement getUserRow(User user);

	@Override
	public void waitForLoad() {
		waitForLoad(getDriver());
	}

	private static void waitForLoad(WebDriver driver) {
		new WebDriverWait(driver, 20).until(ExpectedConditions.visibilityOfElementLocated(By.id("create-user")));
	}

	public FormInput userSearchFilter() {
		return new FormInput(findElement(By.className("users-search-filter")), getDriver());
	}

	public Dropdown userRoleFilter() {
		return new Dropdown(findElement(By.cssSelector(".users-filters-view .dropdown")), getDriver());
	}

	public int getUserCountInTitle() {
		String title = getDriver().findElement(By.tagName("h1")).getText();
		return Integer.parseInt(title.replaceAll("\\D+", ""));
	}
}
