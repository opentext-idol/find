package com.autonomy.abc.selenium.page.search;

import com.autonomy.abc.selenium.language.WarningLanguageDropdown;
import com.autonomy.abc.selenium.language.LanguageDropdown;
import org.openqa.selenium.WebDriver;

public class HSOSearchPage extends SearchPage {

    public HSOSearchPage(final WebDriver driver) {
        super(driver);
    }

    @Override
    protected LanguageDropdown languageDropdown() {
        return new WarningLanguageDropdown();
    }
}
