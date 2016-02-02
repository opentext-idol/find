package com.autonomy.abc.selenium.page.keywords;

import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.autonomy.abc.selenium.language.WarningLanguageDropdown;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import org.openqa.selenium.WebDriver;

public class HSOCreateNewKeywordsPage extends CreateNewKeywordsPage {

    public HSOCreateNewKeywordsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected LanguageDropdown languageDropdown() {
        return new WarningLanguageDropdown();
    }
    
    public static class Factory implements ParametrizedFactory<WebDriver, HSOCreateNewKeywordsPage> {
        @Override
        public HSOCreateNewKeywordsPage create(WebDriver context) {
            return new HSOCreateNewKeywordsPage(context);
        }
    }
}
