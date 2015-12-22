package com.autonomy.abc.selenium.util;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class DriverUtil {

    public static List<String> createAndListWindowHandles(WebDriver driver) {
        final Set<String> windows = driver.getWindowHandles();
        final String handle = driver.getWindowHandle();
        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("window.open('hello', '', 'width=1000');");

        final Set<String> moreWindows = driver.getWindowHandles();
        moreWindows.removeAll(windows);
        final String secondHandle = ((String)moreWindows.toArray()[0]);

        return Arrays.asList(handle, secondHandle);
    }
}
