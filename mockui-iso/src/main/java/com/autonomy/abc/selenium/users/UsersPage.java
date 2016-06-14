package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.application.SOPageBase;
import com.autonomy.abc.selenium.users.table.UserTable;
import com.autonomy.abc.selenium.users.table.UserTableRow;
import com.hp.autonomy.frontend.selenium.element.Dropdown;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public abstract class UsersPage<T extends UserTableRow> extends SOPageBase {

	protected UsersPage(final WebDriver driver) {
		super(driver.findElement(By.cssSelector(".wrapper-content")), driver);
	}

	public int getUserCountInTitle() {
		final String title = getDriver().findElement(By.tagName("h1")).getText();
		return Integer.parseInt(title.replaceAll("\\D+", ""));
	}

	public WebElement createUserButton() {
		return findElement(By.id("create-user"));
	}

	public abstract UserCreationModal userCreationModal();

	public abstract User addNewUser(NewUser newUser, Role role);

	public FormInput userSearchFilter() {
		return new FormInput(findElement(By.className("users-search-filter")), getDriver());
	}

	public Dropdown userRoleFilter() {
		return new Dropdown(findElement(By.cssSelector(".users-filters-view .dropdown")), getDriver());
	}

	public abstract UserTable<T> getTable();

	public int countNumberOfUsers() {
		return getTable().rows().size();
	}

	public List<String> getUsernames() {
		return getTable().getUsernames();
	}

	public T getUserRow(final User user) {
		return getTable().rowFor(user);
	}

	public Role getRoleOf(final User user) {
		return getUserRow(user).getRole();
	}

	@Override
	public void waitForLoad() {
		waitForLoad(getDriver());
	}

	private static void waitForLoad(final WebDriver driver) {
		new WebDriverWait(driver, 20).until(ExpectedConditions.visibilityOfElementLocated(By.id("create-user")));
	}
}
