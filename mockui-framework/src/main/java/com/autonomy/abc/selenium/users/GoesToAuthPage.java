package com.autonomy.abc.selenium.users;

import org.openqa.selenium.WebDriver;

public interface GoesToAuthPage {
    /**
     *  Method which goes to an email provider and finds the email from HoD
     *
     * @param driver        The driver to use for finding the email
     * @return              Whether the user needs to add his authentication method, or whether they're already signed up
     */
    boolean tryGoingToAuthPage(WebDriver driver);
    void cleanUp(WebDriver driver);
}
