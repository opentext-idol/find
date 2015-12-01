package com.autonomy.abc.selenium.page.keywords;

import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.autonomy.abc.selenium.language.WarningLanguageDropdown;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.LoggerFactory;

public class HSOKeywordsPage extends KeywordsPage {
    public HSOKeywordsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public void deleteAllBlacklistedTerms() throws InterruptedException {
        filterView(KeywordFilter.BLACKLIST);

        for (final String language : getLanguageList()) {
            loadOrFadeWait();

            (LoggerFactory.getLogger(KeywordsPage.class)).warn("Cannot select language for blacklists yet");

            for (final WebElement blacklisted : findElements(By.cssSelector(".blacklisted-word .remove-keyword"))) {
                scrollIntoView(blacklisted, getDriver());
                blacklisted.click();
                waitForRefreshIconToDisappear();
            }
        }
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
