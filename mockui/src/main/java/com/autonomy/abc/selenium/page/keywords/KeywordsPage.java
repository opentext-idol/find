package com.autonomy.abc.selenium.page.keywords;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class KeywordsPage extends KeywordsBase {

    public KeywordsPage(WebDriver driver) {
        super(new WebDriverWait(driver,30).until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper-content"))), driver);
        waitForLoad();
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.className("keywords-container")));
    }

    public WebElement createNewKeywordsButton() {
        return findElement(By.xpath(".//div[contains(@class,'keywords-controls')]//a[contains(text(), 'New')]"));
    }

    public WebElement createNewKeywordsButton(WebDriverWait wait) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//div[contains(@class,'keywords-controls')]//a[contains(text(), 'New')]")));
    }

    @Deprecated
    public void deleteAllSynonyms() throws InterruptedException {
        loadOrFadeWait();
        filterView(KeywordsFilter.SYNONYMS);
        WebDriverWait wait = new WebDriverWait(getDriver(),40);	//TODO Possibly too long?

        for (final String language : getLanguageList()) {
            selectLanguage(language);
            final int numberOfSynonymGroups = getDriver().findElements(By.cssSelector(".keywords-list .keywords-sub-list")).size();

            if (numberOfSynonymGroups >= 2) {
                for (int i = 0; i <= numberOfSynonymGroups; i++) {
                    LoggerFactory.getLogger(KeywordsPage.class).info("Deleting");
                    if (findElements(By.cssSelector(".keywords-list .keywords-sub-list")).size() > 2) {
                        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".keywords-list .keywords-sub-list li:first-child .remove-keyword"))).click();	//TODO check works
                        waitForRefreshIconToDisappear();
                    } else {
                        if (findElements(By.cssSelector(".keywords-list .keywords-sub-list")).size() == 2) {
                            findElement(By.cssSelector(".keywords-list .keywords-sub-list li:first-child .remove-keyword")).click();
                        }

                        loadOrFadeWait();
                        break;
                    }
                }
            }
        }
    }

    public void deleteKeywords() {
        filterView(KeywordsFilter.ALL_TYPES);

        for(final String language : getLanguageList()){
            selectLanguage(language);

            List<WebElement> synonymGroups = getDriver().findElements(By.cssSelector(".keywords-list .keywords-sub-list"));

            for(int i = 1; i <= synonymGroups.size(); i++){
                for(int j = getDriver().findElement(By.cssSelector(".keywords-list li:nth-child(1) ul")).findElements(By.tagName("li")).size(); j > 0; j--) {
                    WebElement cross = getDriver().findElement(By.cssSelector(".keywords-list li:nth-child(1) ul li:nth-child("+j+") i"));

                    if(!cross.getAttribute("class").contains("fa-spin")) {
                        cross.click();
                    }

                }

                waitForRefreshIconToDisappear();
            }
        }
    }

    public int countSynonymLists() {
        return (findElement(By.className("keywords-list"))).findElements(By.cssSelector(".add-synonym")).size();
    }

    @Deprecated
    @Override
    public WebElement leadSynonym(final String synonym) {
        return synonymInGroup(synonym);
    }

    @Override
    public WebElement synonymInGroup(String synonym){
        return findElement(By.xpath(".//div[contains(@class, 'keywords-list')]/ul/li/ul[contains(@class, 'keywords-sub-list')]/li[@data-term='" + synonym + "']"));
    }

    private int getNumberOfLanguages() {
        return findElements(By.cssSelector(".scrollable-menu li")).size();
    }

    public abstract void deleteAllBlacklistedTerms() throws InterruptedException;

    public void filterView(final KeywordsFilter filter) {
        WebDriverWait wait = new WebDriverWait(getDriver(),5);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".keywords-filters .dropdown-toggle"))).click();
        findElement(By.xpath("//*[contains(@class,'keywords-filters')]//a[text()='"+ filter.toString() +"']")).click();
    }

    public int countKeywords() {
        return findElements(By.cssSelector(".keywords-list .remove-keyword")).size();
    }

    public WebElement getSynonymGroup(String synonym) {
        return getSynonymIcon(synonym).findElement(By.xpath(".//../../.."));
    }

    public enum KeywordsFilter {
        ALL_TYPES("All Types"),
        BLACKLIST("Blacklist"),
        SYNONYMS("Synonyms");

        private final String filterName;

        KeywordsFilter(final String name) {
            filterName = name;
        }

        public String toString() {
            return filterName;
        }

    }

    @Deprecated
    public int countSynonymGroupsWithLeadSynonym(final String synonym) {
        return findElement(By.cssSelector(".keywords-list")).findElements(By.xpath(".//ul[contains(@class, 'keywords-sub-list')]/li[1][@data-term='" + synonym + "']")).size();
    }

    public int countSynonymGroupsWithSynonym(final String synonym) {
        return findElement(By.cssSelector(".keywords-container .keywords-list")).findElements(By.xpath(".//ul[contains(@class,'keywords-sub-list')]/li[@data-term='"+synonym+"']")).size();
    }

    public WebElement searchFilterTextBox() {
        return findElement(By.className("keywords-search-filter"));
    }

    public abstract void selectLanguage(final String language);

    public String getSelectedLanguage() {
        return selectLanguageButton().getText();
    }

    public WebElement selectLanguageButton() {
        return new WebDriverWait(getDriver(),20).until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".keywords-filters .current-language-selection")));
    }

    public List<String> getLanguageList() {
        final List<String> languages = new ArrayList<>();

        if (isAttributePresent(getParent(selectLanguageButton()), "disabled")) {
            languages.add(getSelectedLanguage());
            return languages;
        } else {
            selectLanguageButton().click();
            loadOrFadeWait();

            for (final WebElement language : findElements(By.cssSelector(".keywords-filters .scrollable-menu a"))) {
                languages.add(language.getText());
            }

            selectLanguageButton().click();
            return languages;
        }
    }

    @Deprecated
    public List<String> getLeadSynonymsList() {
        final List<String> leadSynonyms = new ArrayList<>();
        for (final WebElement synonymGroup : findElements(By.cssSelector(".keywords-list > ul > li"))) {
            leadSynonyms.add(synonymGroup.findElement(By.cssSelector("li:first-child span span")).getText());
        }
        return leadSynonyms;
    }

}

