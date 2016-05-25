package com.autonomy.abc.selenium.keywords;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.application.IsoElementFactory;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.search.SearchPage;
import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class KeywordService extends ServiceBase<IsoElementFactory> {
    private final static Logger LOGGER = LoggerFactory.getLogger(KeywordService.class);
    private KeywordsPage keywordsPage;
    private CreateNewKeywordsPage newKeywordsPage;

    public KeywordService(IsoApplication<? extends IsoElementFactory> application) {
        super(application);
    }

    public KeywordsPage goToKeywords() {
        keywordsPage = getApplication().switchTo(KeywordsPage.class);
        return keywordsPage;
    }

    public CreateNewKeywordsPage goToKeywordsWizard() {
        goToKeywords();
        keywordsPage.createNewKeywordsButton().click();
        newKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        return newKeywordsPage;
    }

    public SearchPage addSynonymGroup(String... synonyms) {
        return addSynonymGroup(Arrays.asList(synonyms));
    }

    public SearchPage addSynonymGroup(Iterable<String> synonyms) {
        return addSynonymGroup(Language.ENGLISH, synonyms);
    }

    public SearchPage addSynonymGroup(Language language, String... synonyms) {
        return addSynonymGroup(language, Arrays.asList(synonyms));
    }
    //bad because assumes redirected to search page which not be if no docs in that language
    public SearchPage addSynonymGroup(Language language, Iterable<String> synonyms) {
        addKeywords(KeywordWizardType.SYNONYMS, language, synonyms);
        SearchPage searchPage = getElementFactory().getSearchPage();
        searchPage.waitForSearchLoadIndicatorToDisappear();
        return searchPage;
    }

    public KeywordsPage addBlacklistTerms(String... blacklists) {
        return addBlacklistTerms(Arrays.asList(blacklists));
    }

    public KeywordsPage addBlacklistTerms(Iterable<String> blacklists) {
        return addBlacklistTerms(Language.ENGLISH, blacklists);
    }

    public KeywordsPage addBlacklistTerms(Language language, String... blacklists) {
        return addBlacklistTerms(language, Arrays.asList(blacklists));
    }

    public KeywordsPage addBlacklistTerms(Language language, Iterable<String> blacklists) {
        addKeywords(KeywordWizardType.BLACKLIST, language, blacklists);
        FluentWait<WebDriver> wait = new WebDriverWait(getDriver(), 40)
                .withMessage("adding " + blacklists + " to the blacklist");
        wait.until(GritterNotice.notificationContaining("to the blacklist"));
        // terms appear asynchronously - must wait until they have ALL been added
        wait.until(GritterNotice.notificationsDisappear());
        return getElementFactory().getKeywordsPage();
    }

    // this does not wait at the end, generally not the one you want
    public void addKeywords(KeywordWizardType type, Language language, Iterable<String> keywords) {
        goToKeywordsWizard();
        if (getApplication().isHosted() && !language.equals(Language.ENGLISH)) {
            LOGGER.warn("hosted mode does not support foreign keywords, using English instead");
            language = Language.ENGLISH;
        }
        new KeywordGroup(type, language, keywords).makeWizard(newKeywordsPage).apply();
    }

    public KeywordsPage deleteAll(KeywordFilter type) {
        goToKeywords();
        keywordsPage.filterView(type);
        int count = 0;
        for (final Language language : keywordsPage.getLanguageList()) {
            int current = keywordsPage.countKeywords();
            if (current > 0) {
                count += current;
                try {
                    tryDeleteAll(language);
                } catch (StaleElementReferenceException e) {
                    return deleteAll(type);
                }
            }
        }
        waitUntilNoKeywords(10 * (count + 1));
        return keywordsPage;
    }

    private void tryDeleteAll(Language language) throws StaleElementReferenceException {
        try {
            keywordsPage.selectLanguage(language);
        } catch (WebDriverException e) {
            /* language dropdown disabled */
        }
        List<WebElement> keywordGroups = keywordsPage.allKeywordGroups();
        for (WebElement group : keywordGroups) {
            removeKeywordGroupAsync(group);
        }
    }

    private void removeKeywordGroupAsync(WebElement group) {
        List<WebElement> removeButtons = keywordsPage.removeButtons(group);
        if (removeButtons.size() > 1) {
            removeButtons.remove(0);
        }
        for (WebElement removeButton : removeButtons) {
            new WebDriverWait(getDriver(),20).until(ExpectedConditions.invisibilityOfElementLocated(By.className("fa-spin")));
            removeButton.click();
        }
    }

    private void waitUntilNoKeywords(int timeout) {
        new WebDriverWait(getDriver(), timeout)
                .withMessage("deleting keywords")
                .until(ExpectedConditions.textToBePresentInElement(keywordsPage, "No keywords found"));
    }

    public void removeKeywordGroup(WebElement group) {
        removeKeywordGroupAsync(group);
        keywordsPage.waitForRefreshIconToDisappear();
    }

    public KeywordsPage deleteKeyword(String term) {
        goToKeywords();
        keywordsPage.deleteKeyword(term);
        return keywordsPage;
    }
}
