package com.autonomy.abc.selenium.util;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.List;
import java.util.NoSuchElementException;

public class Predicates {
    private static boolean areAllInvisible(List<? extends WebElement> elements) {
        for (WebElement element : elements) {
            try {
                if (element.isDisplayed()) {
                    return false;
                }
            } catch (NoSuchElementException e) {
                /* not in DOM => not visible */
            } catch (StaleElementReferenceException e) {
                /* stale => not visible */
            }
        }
        return true;
    }

    public static ExpectedCondition<Boolean> invisibilityOfAllElementsLocated(final By locator) {
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return areAllInvisible(input.findElements(locator));
            }

            @Override
            public String toString() {
                return "invisibility of all elements located by " + locator;
            }
        };
    }
}
