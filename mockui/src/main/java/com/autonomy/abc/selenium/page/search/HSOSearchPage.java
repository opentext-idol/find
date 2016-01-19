package com.autonomy.abc.selenium.page.search;

import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.autonomy.abc.selenium.language.WarningLanguageDropdown;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HSOSearchPage extends SearchPage {

    public HSOSearchPage(final WebDriver driver) {
        super(driver);
    }

    @Override
    protected LanguageDropdown languageDropdown() {
        return new WarningLanguageDropdown();
    }

}
