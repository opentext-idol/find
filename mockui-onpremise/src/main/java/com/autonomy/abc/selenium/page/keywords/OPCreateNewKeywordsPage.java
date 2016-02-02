package com.autonomy.abc.selenium.page.keywords;

import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.autonomy.abc.selenium.language.OPLanguageDropdown;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class OPCreateNewKeywordsPage extends CreateNewKeywordsPage {
    private OPCreateNewKeywordsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected LanguageDropdown languageDropdown() {
        return new OPLanguageDropdown(findElement(By.cssSelector(".wizard-steps .language-select-view-container")), getDriver());
    }

    public static class Factory implements ParametrizedFactory<WebDriver, OPCreateNewKeywordsPage> {
        public OPCreateNewKeywordsPage create(WebDriver context) {
            return new OPCreateNewKeywordsPage(context);
        }
    }
}
