package com.autonomy.abc.selenium.page.admin;

import com.autonomy.abc.selenium.AppElement;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
		return findElement(By.xpath(".//span[contains(text(), '" + userName + "')]/../.."));
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
