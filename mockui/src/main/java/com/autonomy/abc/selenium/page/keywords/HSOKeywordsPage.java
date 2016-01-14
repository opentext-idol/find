package com.autonomy.abc.selenium.page.keywords;

import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.autonomy.abc.selenium.language.WarningLanguageDropdown;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.LoggerFactory;

public class HSOKeywordsPage extends KeywordsPage {
    public HSOKeywordsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public void selectLanguage(String language) {
        return;
    }

    @Override
    protected LanguageDropdown languageDropdown() {
        return new WarningLanguageDropdown();
    }
}
