package com.autonomy.abc.selenium.page.admin;

import com.autonomy.abc.selenium.element.Dropdown;
import com.autonomy.abc.selenium.element.FormInput;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HSOUsersPage extends UsersPage {
    public HSOUsersPage(WebDriver driver) {
        super(driver);
    }

    public FormInput getUsernameInput(){
        return new FormInput(getDriver().findElement(By.id("create-users-username")), getDriver());
    }

    public FormInput getEmailInput(){
        return new FormInput(getDriver().findElement(By.id("create-users-380")), getDriver());
    }

    public Dropdown getUserLevelDropdown(){
        return new Dropdown(findElement(By.id("create-users-role")), getDriver());
    }
}
