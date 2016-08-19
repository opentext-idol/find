package com.autonomy.abc.selenium.auth;

import org.openqa.selenium.WebDriver;

public interface GoesToAuthPage {
    void tryGoingToAuthPage(WebDriver driver) throws Exception;
    void cleanUp(WebDriver driver);
}
