package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.autonomy.abc.selenium.language.WarningLanguageDropdown;
import org.openqa.selenium.WebDriver;

public class HsodSearchPage extends SearchPage {

    private HsodSearchPage(final WebDriver driver) {
        super(driver);
    }

    @Override
    protected LanguageDropdown languageDropdown() {
        return new WarningLanguageDropdown();
    }

    public static class Factory extends SOPageFactory<HsodSearchPage> {
        public Factory() {
            super(HsodSearchPage.class);
        }

        @Override
        public HsodSearchPage create(final WebDriver context) {
            return new HsodSearchPage(context);
        }
    }

}
