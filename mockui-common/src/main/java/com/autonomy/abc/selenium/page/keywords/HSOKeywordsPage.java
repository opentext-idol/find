package com.autonomy.abc.selenium.page.keywords;

import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.autonomy.abc.selenium.language.WarningLanguageDropdown;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import org.openqa.selenium.WebDriver;

public class HSOKeywordsPage extends KeywordsPage {
    private HSOKeywordsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected LanguageDropdown languageDropdown() {
        return new WarningLanguageDropdown();
    }


    public static class Factory implements ParametrizedFactory<WebDriver, HSOKeywordsPage> {
        @Override
        public HSOKeywordsPage create(WebDriver context) {
            return new HSOKeywordsPage(context);
        }
    }
}
