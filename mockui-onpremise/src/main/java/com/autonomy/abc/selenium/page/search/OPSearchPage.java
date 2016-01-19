package com.autonomy.abc.selenium.page.search;

import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.autonomy.abc.selenium.language.OPLanguageDropdown;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class OPSearchPage extends SearchPage {
    public OPSearchPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected LanguageDropdown languageDropdown() {
        return new OPLanguageDropdown(findElement(By.cssSelector(".search-language")), getDriver());
    }
}
