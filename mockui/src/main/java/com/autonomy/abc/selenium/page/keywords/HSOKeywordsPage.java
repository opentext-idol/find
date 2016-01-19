package com.autonomy.abc.selenium.page.keywords;

import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.autonomy.abc.selenium.language.WarningLanguageDropdown;
import org.openqa.selenium.WebDriver;

public class HSOKeywordsPage extends KeywordsPage {
    public HSOKeywordsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected LanguageDropdown languageDropdown() {
        return new WarningLanguageDropdown();
    }
}
