package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.autonomy.abc.selenium.language.WarningLanguageDropdown;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.WebDriver;

public class HSODSearchPage extends SearchPage {

    private HSODSearchPage(final WebDriver driver) {
        super(driver);
    }

    @Override
    protected LanguageDropdown languageDropdown() {
        return new WarningLanguageDropdown();
    }

    public static class Factory extends SOPageFactory<HSODSearchPage> {
        public Factory() {
            super(HSODSearchPage.class);
        }

        @Override
        public HSODSearchPage create(WebDriver context) {
            return new HSODSearchPage(context);
        }
    }

}
