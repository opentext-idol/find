package com.autonomy.abc.selenium.keywords;

import com.autonomy.abc.selenium.application.SOPageBase;
import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.autonomy.abc.selenium.language.WarningLanguageDropdown;
import org.openqa.selenium.WebDriver;

public class HsodKeywordsPage extends KeywordsPage {
    private HsodKeywordsPage(final WebDriver driver) {
        super(driver);
    }

    @Override
    protected LanguageDropdown languageDropdown() {
        return new WarningLanguageDropdown();
    }


    public static class Factory extends SOPageBase.SOPageFactory<HsodKeywordsPage> {
        public Factory() {
            super(HsodKeywordsPage.class);
        }

        @Override
        public HsodKeywordsPage create(final WebDriver context) {
            return new HsodKeywordsPage(context);
        }
    }
}
