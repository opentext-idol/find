package com.autonomy.abc.selenium.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class PageUtil {

    public static String getPageTitle(WebDriver driver) {
        return driver.findElement(By.cssSelector(".page-heading .heading")).getText();
    }

    public static boolean isModalShowing(WebDriver driver) {
        return !driver.findElements(By.cssSelector(".modal[aria-hidden='false']")).isEmpty();
    }
}
