package com.autonomy.abc.selenium.util;

import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;

public class DriverUtil {

    // TODO: get rid of this, only used as a nasty hack
    @Deprecated
    public static boolean isAlertPresent(WebDriver driver) {
        try {
            driver.switchTo().alert();
            return true;
        } catch (final NoAlertPresentException ex) {
            return false;
        }
    }
}
