package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.application.SOPageBase;
import com.hp.autonomy.frontend.selenium.element.Dropdown;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.element.PasswordBox;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Waits;
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

	public abstract UserCreationModal userCreationModal();

	public int countNumberOfUsers() {
		Waits.loadOrFadeWait();
		return getTable().rows().size();
	}

	public WebElement deleteButton(User user){
		return getUserRow(user).findElement(By.cssSelector(".users-deleteUser"));
	}

	public UserTable getTable() {
		return new UserTable(findElement(By.cssSelector("#users-current-admins")), getDriver());
	}

	public List<String> getUsernames() {
		List<String> usernames = new ArrayList<>();
		for (UserTable.Row row : getTable()) {
			usernames.add(row.getUsername());
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
