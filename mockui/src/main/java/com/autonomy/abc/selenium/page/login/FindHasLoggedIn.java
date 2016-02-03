package com.autonomy.abc.selenium.page.login;

import com.autonomy.abc.selenium.page.HSODElementFactory;
import com.hp.autonomy.frontend.selenium.login.HasLoggedIn;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class FindHasLoggedIn implements HasLoggedIn {
    private HSODElementFactory elementFactory;

    public FindHasLoggedIn(HSODElementFactory elementFactory) {
        this.elementFactory = elementFactory;
    }

    @Override
    public boolean hasLoggedIn() {
        try {
            elementFactory.getFindPage();
            return true;
        } catch (Exception e) {
            if (signedInTextVisible(elementFactory.getDriver())) {
                throw new SSOFailureException();
            }
        }
        return false;
    }

    private boolean signedInTextVisible(WebDriver driver) {
        return driver.findElements(By.xpath("//*[text()='Signed in']")).size() > 0;
    }
}
