package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.autonomy.abc.selenium.language.WarningLanguageDropdown;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import org.openqa.selenium.WebDriver;

public class HSODSearchPage extends SearchPage {

    private HSODSearchPage(final WebDriver driver) {
        super(driver);
    }

    @Override
    protected LanguageDropdown languageDropdown() {
        return new WarningLanguageDropdown();
    }

    public static class Factory implements ParametrizedFactory<WebDriver, HSODSearchPage> {
        @Override
        public HSODSearchPage create(WebDriver context) {
            return new HSODSearchPage(context);
        }
    }

}
