package com.autonomy.abc.selenium.page.admin;

import com.autonomy.abc.selenium.users.Role;
import com.autonomy.abc.selenium.users.User;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HSODevelopersPage extends HSOUserManagement {
    public HSODevelopersPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public WebElement getUserRow(User user) {
        return getUserRowByUsername(user.getUsername());
    }

    @Override
    public void deleteUser(String userName) {
        throw new InvalidActionException("deleteUser");
    }

    @Override
    public void setRoleValueFor(User user, Role newRole) {
        throw new InvalidActionException("setRoleValueFor");
    }

    private class InvalidActionException extends RuntimeException {
        public InvalidActionException(String reason){
            super("Cannot perform " + reason + "() on a developer" );
        }
    }

    @Override
    protected WebElement getUserRowByUsername(String username) {
        return findElement(By.xpath("//*[contains(@class,'user-name') and contains(.,'" + username + "')]"));
    }
}
