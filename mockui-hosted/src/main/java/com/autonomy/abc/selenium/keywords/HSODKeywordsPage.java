package com.autonomy.abc.selenium.keywords;

import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.autonomy.abc.selenium.language.WarningLanguageDropdown;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import org.openqa.selenium.WebDriver;

public class HSODKeywordsPage extends KeywordsPage {
    private HSODKeywordsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected LanguageDropdown languageDropdown() {
        return new WarningLanguageDropdown();
    }


    public static class Factory implements ParametrizedFactory<WebDriver, HSODKeywordsPage> {
        @Override
        public HSODKeywordsPage create(WebDriver context) {
            return new HSODKeywordsPage(context);
        }
    }
}
