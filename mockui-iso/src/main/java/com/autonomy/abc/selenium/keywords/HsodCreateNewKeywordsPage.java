package com.autonomy.abc.selenium.keywords;

import com.autonomy.abc.selenium.application.SOPageBase;
import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.autonomy.abc.selenium.language.WarningLanguageDropdown;
import org.openqa.selenium.WebDriver;

public class HsodCreateNewKeywordsPage extends CreateNewKeywordsPage {

    private HsodCreateNewKeywordsPage(final WebDriver driver) {
        super(driver);
    }

    @Override
    protected LanguageDropdown languageDropdown() {
        return new WarningLanguageDropdown();
    }
    
    public static class Factory extends SOPageBase.SOPageFactory<HsodCreateNewKeywordsPage> {
        public Factory() {
            super(HsodCreateNewKeywordsPage.class);
        }

        @Override
        public HsodCreateNewKeywordsPage create(final WebDriver context) {
            return new HsodCreateNewKeywordsPage(context);
        }
    }
}
