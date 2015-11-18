package com.autonomy.abc.selenium.keywords;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.util.Language;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class KeywordService extends ServiceBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(KeywordService.class);
    private KeywordsPage keywordsPage;
    private CreateNewKeywordsPage newKeywordsPage;

    public KeywordService(Application application, ElementFactory elementFactory) {
        super(application, elementFactory);
    }

    public KeywordsPage goToKeywords() {
        getBody().getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        keywordsPage = getElementFactory().getKeywordsPage();
        return keywordsPage;
    }

    public CreateNewKeywordsPage goToKeywordsWizard() {
        goToKeywords();
        keywordsPage.createNewKeywordsButton().click();
        newKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        return newKeywordsPage;
    }

    public SearchPage addSynonymGroup(String... synonyms) {
        return addSynonymGroup(Language.ENGLISH, synonyms);
    }

    public SearchPage addSynonymGroup(Language language, String... synonyms) {
        addKeywords(KeywordType.SYNONYMS, language, synonyms);
        SearchPage searchPage = getElementFactory().getSearchPage();
        searchPage.waitForSearchLoadIndicatorToDisappear();
        return searchPage;
    }

    public KeywordsPage addBlacklistTerms(String... blacklists) {
        return addBlacklistTerms(Language.ENGLISH, blacklists);
    }

    public KeywordsPage addBlacklistTerms(Language language, String... blacklists) {
        addKeywords(KeywordType.BLACKLIST, language, blacklists);
        new WebDriverWait(getDriver(), 30).until(GritterNotice.notificationContaining("to the blacklist"));
        // terms appear asynchronously - must wait until they have ALL been added
        new WebDriverWait(getDriver(), 20).until(GritterNotice.notificationsDisappear());
        return getElementFactory().getKeywordsPage();
    }

    private void addKeywords(KeywordType type, Language language, String... keywords) {
        goToKeywordsWizard();
        if (getApplication().getType().equals(ApplicationType.HOSTED) && !language.equals(Language.ENGLISH)) {
            LOGGER.warn("hosted mode does not support foreign keywords, using English instead");
            language = Language.ENGLISH;
        }
        new KeywordGroup(type, language, keywords).makeWizard(newKeywordsPage).apply();
    }

    public KeywordsPage deleteAll() {
        goToKeywords();
        for (final String language : keywordsPage.getLanguageList()) {
            try {
                tryDeleteAll(language);
            } catch (StaleElementReferenceException e) {
                return deleteAll();
            }
        }
        new WebDriverWait(getDriver(), 100).withMessage("deleting keywords").until(ExpectedConditions.textToBePresentInElement(keywordsPage, "No keywords found"));
        return keywordsPage;
    }

    private void tryDeleteAll(String language) throws StaleElementReferenceException {
        try {
            keywordsPage.selectLanguage(language);
        } catch (WebDriverException e) {
            /* language dropdown disabled */
        }
        List<WebElement> keywordGroups = keywordsPage.findElements(By.cssSelector(".keywords-list-container .keywords-sub-list"));
        for (WebElement group : keywordGroups) {
            removeKeywordGroup(group);
        }
    }

    private void removeKeywordGroup(WebElement group) {
        List<WebElement> removeBtns = group.findElements(By.cssSelector("li .remove-keyword"));
        if (removeBtns.size() > 1) {
            removeBtns.remove(0);
        }
        for (WebElement removeBtn : removeBtns) {
            removeBtn.click();
        }
    }

    public KeywordsPage deleteKeyword(String term) {
        goToKeywords();
        keywordsPage.deleteKeyword(term);
        return keywordsPage;
    }
}
