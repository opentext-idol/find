package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.application.SOPageBase;
import com.autonomy.abc.selenium.users.table.UserTable;
import com.autonomy.abc.selenium.users.table.UserTableRow;
import com.hp.autonomy.frontend.selenium.element.Dropdown;
import com.hp.autonomy.frontend.selenium.element.FormInput;
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

public abstract class UsersPage<T extends UserTableRow> extends SOPageBase {

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

	public abstract UserTable<T> getTable();

	public List<String> getUsernames() {
		List<String> usernames = new ArrayList<>();
		for (UserTableRow row : getTable()) {
			usernames.add(row.getUsername());
		}
		return usernames;
	}

	public Role getRoleOf(User user) {
		return getUserRow(user).getRole();
	}

	public T getUserRow(User user) {
		return getTable().rowFor(user);
	}

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
