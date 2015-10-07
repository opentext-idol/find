package com.autonomy.abc.selenium.page.keywords;

import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Created by mattwill on 04/09/2015.
 */
public class OPCreateNewKeywordsPage extends CreateNewKeywordsPage {
    public OPCreateNewKeywordsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public void selectLanguage(String language) {
            languagesSelectBox().click();
            loadOrFadeWait();
            final WebElement element = findElement(By.cssSelector("[data-step='type'] .dropdown-menu")).findElement(By.xpath(".//a[contains(text(), '" + language + "')]"));
            // IE doesn't want to click the dropdown elements
            final JavascriptExecutor executor = (JavascriptExecutor)getDriver();
            executor.executeScript("arguments[0].click();", element);
            loadOrFadeWait();
    }
}
