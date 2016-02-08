package com.autonomy.abc.selenium.page.admin;

import com.autonomy.abc.selenium.users.HSOUser;
import com.autonomy.abc.selenium.users.NewUser;
import com.autonomy.abc.selenium.users.Role;
import com.autonomy.abc.selenium.users.User;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HSODevelopersPage extends HSOUserManagementPage {
    private HSODevelopersPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public User addNewUser(NewUser newUser, Role role) {
        throw new UnsupportedOperationException("Cannot add new developers to a tenancy");
    }

    @Override
    public WebElement getUserRow(User user) {
        return getUserRowByUsername(user.getUsername());
    }

    public User getUser(int index) {
        WebElement row = findElement(By.cssSelector(".users-table tbody tr:nth-of-type(" + (index+1) + ")"));
        String username = row.findElement(By.className("user-name")).getText();
        Role role = Role.fromString(row.findElement(By.className("user-role")).getText());
        return new HSOUser(username, null, role);
    }

    @Override
    public void deleteUser(String userName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRoleValueFor(User user, Role newRole) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected WebElement getUserRowByUsername(String username) {
        return findElement(By.xpath("//*[contains(@class,'user-name') and contains(.,'" + username + "')]"));
    }

    public void editUsername(User dev, String devUsername) {
        editUsernameLink(dev).click();
        editUsernameInput(dev).setAndSubmit(devUsername);
    }

    public static class Factory implements ParametrizedFactory<WebDriver, HSODevelopersPage> {
        public HSODevelopersPage create(WebDriver context) {
            return new HSODevelopersPage(context);
        }
    }
}
