package com.autonomy.abc.selenium.users;

import org.openqa.selenium.WebDriver;

public interface GoesToAuthPage {
    void tryGoingToAuthPage(WebDriver driver) throws Exception;
    void cleanUp(WebDriver driver);
}
