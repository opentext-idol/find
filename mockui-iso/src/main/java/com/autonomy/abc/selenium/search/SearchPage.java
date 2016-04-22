package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.element.SOCheckbox;
import com.autonomy.abc.selenium.keywords.KeywordsContainer;
import com.autonomy.abc.selenium.keywords.SynonymGroup;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.SortBy;
import com.hp.autonomy.frontend.selenium.element.Checkbox;
import com.hp.autonomy.frontend.selenium.element.Dropdown;
import com.hp.autonomy.frontend.selenium.element.Pagination;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public abstract class SearchPage extends SearchBase implements LanguageFilter.Filterable {
	public final static int RESULTS_PER_PAGE = 6;
	public final static int MAX_RESULTS = 2500;

	public SearchPage(final WebDriver driver) {
		// specify data-pagename to avoid invisible elements from other pages showing up
		super(waitForLoad(driver), driver);
	}

	private static WebElement waitForLoad(WebDriver driver) {
		return new WebDriverWait(driver, 30)
				.withMessage("loading search page")
				.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".wrapper-content [data-pagename='search']")));
	}

	@Override
	public void waitForLoad() {
		waitForLoad(getDriver());
	}

	/* title */
	// "Results for _____ (123)"
	public String getHeadingSearchTerm() {
		WebElement heading = getDriver().findElement(By.cssSelector(".heading > b"));
		DriverUtil.scrollIntoView(getDriver(), heading);
		return heading.getText();
	}

	// "Results for term (___)"
	// WARN: results count will not be displayed if search had an error
	public int getHeadingResultsCount() {
		((JavascriptExecutor) getDriver()).executeScript("scroll(0,0)");
		final String totalWithBrackets = new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".page-heading span"))).getText();
		final String totalNoBrackets = totalWithBrackets.substring(1, totalWithBrackets.length() - 1);

		if (StringUtils.containsIgnoreCase(totalNoBrackets, "more than ")) {
			return Integer.parseInt(totalNoBrackets.split(" ")[2]) + 1;
		}
		return Integer.parseInt(totalNoBrackets);
	}

	/* page controls */
	public WebElement promoteTheseDocumentsButton() {
		return findElement(By.xpath(".//button[contains(text(), 'Promote documents')]"));
	}

	public WebElement promoteThisQueryButton() {
		//The space is deliberate!
		return findElement(By.xpath("//button[text()=' Promote query']"));
	}

	public Checkbox modifiedResults() {
		return new SOCheckbox(findElement(By.className("search-type-toggle")), getDriver());
	}

	public void sortBy(final SortBy sortBy) {
		sortDropdown().select(sortBy.toString());
	}

	private Dropdown sortDropdown() {
		WebElement dropdownContainer = new WebDriverWait(getDriver(),10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".search-results-sort")));
		return new Dropdown(dropdownContainer, getDriver());
	}

	public void selectLanguage(final Language language) {
		languageDropdown().select(language);
		waitForSearchLoadIndicatorToDisappear();
	}

	protected abstract LanguageDropdown languageDropdown();

	// TODO: use LanguageDropdown
	public WebElement languageButton() {
		return findElement(By.cssSelector(".search-language .dropdown-toggle"));
	}

	public Language getSelectedLanguage() {
		return languageDropdown().getSelected();
	}

	public List<String> getLanguageList() {
		languageDropdown().open();
		final List<String> languageList = new ArrayList<>();

		for (final WebElement language : findElements(By.cssSelector(".search-page-controls [role='menuitem']"))) {
			languageList.add(language.getText());
		}

		return languageList;
	}

	/* promotions bucket */
	public WebElement promoteTheseItemsButton() {
		return findElement(By.xpath(".//*[contains(text(), 'Promote items')]"));
	}

	public void openPromotionsBucket() {
		promoteTheseDocumentsButton().click();
		Waits.loadOrFadeWait();
	}

	public void closePromotionsBucket() {
		promotionsBucket().findElement(By.className("close-link")).click();
		Waits.loadOrFadeWait();
	}

	/* promoted results */
	public List<IsoSearchResult> getPromotedResults() {
		waitForPromotionsLoadIndicatorToDisappear();
		List<IsoSearchResult> results = new ArrayList<>();
		for(WebElement result : findElements(By.cssSelector(".promotions-list li"))){
			results.add(new IsoSearchResult(result, getDriver()));
		}
		return results;
	}

	public IsoSearchResult getPromotedResult(final int resultNumber) {
		waitForPromotionsLoadIndicatorToDisappear();
		return new IsoSearchResult(findElement(By.cssSelector(".promotions-list li:nth-child(" + resultNumber + ")")), getDriver());
	}

	public WebElement promotionsSummary() {
		return findElement(By.className("promotions-summary"));
	}

	public int getPromotionSummarySize() {
		int summaryItemsTotal = 0;

		for (final WebElement searchResult : findElements(By.cssSelector(".promotions-list li"))) {
			if (searchResult.isDisplayed()) {
				summaryItemsTotal++;
			}
		}

		return summaryItemsTotal;
	}

	public List<String> getPromotedDocumentTitles(final boolean fullList) {
		waitForPromotionsLoadIndicatorToDisappear();
		final List<String> promotionsList = new ArrayList<>();

		if (showMorePromotionsButton().isDisplayed()) {
			showMorePromotions();
		}

		if (fullList && ElementUtil.isEnabled(promotionPaginationButton(Pagination.FIRST))) {
			DriverUtil.scrollIntoViewAndClick(getDriver(), promotionPaginationButton(Pagination.FIRST));
		}

		promotionsList.addAll(getVisiblePromotedDocumentTitles());

		if (fullList) {
			while (ElementUtil.isEnabled(promotionPaginationButton(Pagination.NEXT))) {
				switchPromotionPage(Pagination.NEXT);
				promotionsList.addAll(getVisiblePromotedDocumentTitles());
			}
		}
		return promotionsList;
	}

	private List<String> getVisiblePromotedDocumentTitles() {
		return ElementUtil.getTexts(findElements(By.cssSelector(".promotions-list h3 a")));
	}

	public WebElement promotedDocumentTitle(int number) {
		return promotedResult(number).findElement(By.tagName("h3"));
	}

	public WebElement promotedResult(int number) {
		return new WebDriverWait(getDriver(),60).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".promotions-list li:nth-child(" + String.valueOf(number) + ")")));
	}

	public boolean isPromotionsBoxVisible() {
		return !findElement(By.className("promotions")).getAttribute("class").contains("hidden");
	}

	public void showMorePromotions() {
		showMorePromotionsButton().click();
		Waits.loadOrFadeWait();
	}

	public void showLessPromotions() {
		showLessPromotionsButton().click();
		Waits.loadOrFadeWait();
	}

	public WebElement showMorePromotionsButton() {
		return findElement(By.className("show-more"));
	}

	public WebElement showLessPromotionsButton() {
		return findElement(By.className("show-less"));
	}

	public void switchPromotionPage(Pagination pagination) {
		DriverUtil.scrollIntoViewAndClick(getDriver(), promotionPaginationButton(pagination));
		waitForPromotionsLoadIndicatorToDisappear();
	}

	public WebElement promotionPaginationButton(Pagination pagination) {
		return pagination.findInside(promotionsPagination());
	}

	private WebElement promotionsPagination() {
		return findElement(By.className("promotions-pagination"));
	}

	/* search results */
	public List<String> getSearchResultTitles(int numberOfResults){
        List<String> titles = new ArrayList<>();

        for(int i = 0; i < numberOfResults; i++){
            titles.add(getSearchResult((i % 6) + 1).getTitleString());

            if((i + 1) % 6 == 0 && (i + 1) != numberOfResults){
				switchResultsPage(Pagination.NEXT);
            }
        }

		return titles;
	}

	@Override
	protected WebElement searchResultCheckboxElement(int resultNumber) {
		// TODO: find others like this, avoid repetition
		return new WebDriverWait(getDriver(),20).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-pagename='search'] .search-results li:nth-child(" + resultNumber + ") label")));
	}

	public int countPinToPositionLabels() {
		return findElements(By.cssSelector(".injected-promotion .fa-thumb-tack")).size();
	}

	/* keywords */
	public List<String> youSearchedFor() {
		WebElement searchTermsList = findElement(By.className("search-terms-list"));
		DriverUtil.scrollIntoView(getDriver(), searchTermsList);
		return ElementUtil.getTexts(searchTermsList.findElements(By.tagName("span")));
	}

	public WebElement blacklistLink() {
		return findElement(By.xpath(".//a[text() = 'blacklist']"));
	}

	public WebElement createSynonymsLink() {
		return findElement(By.xpath(".//a[text() = 'create synonyms']"));
	}

	public List<String> getSynonymGroupSynonyms(String synonym) {
		return synonymGroupContaining(synonym).getSynonyms();
	}

	public SynonymGroup synonymGroupContaining(String term) {
		return keywordsContainer().synonymGroupContaining(term);
	}

	public int countKeywords() {
		return keywordsContainer().keywords().size();
	}

	public int countSynonymLists() {
		return keywordsContainer().synonymGroups().size();
	}

	public List<String> getFirstSynonymInGroup() {
		final List<String> leadSynonyms = new ArrayList<>();
		for(SynonymGroup group : keywordsContainer().synonymGroups()){
			leadSynonyms.add(group.getSynonyms().get(0));
		}
		return leadSynonyms;
	}

	public void addSynonymToGroup(String newSynonym, SynonymGroup group) {
		group.add(newSynonym);
	}

	public void deleteSynonym(String toDelete, SynonymGroup group) {
		group.remove(toDelete);
	}

	public void deleteSynonym(String toDelete) {
		deleteSynonym(toDelete, synonymGroupContaining(toDelete));
	}

	public List<String> getBlacklistedTerms() {
		return ElementUtil.getTexts(keywordsContainer().blacklistTerms());
	}

	private KeywordsContainer keywordsContainer() {
		return new KeywordsContainer(findElement(By.cssSelector(".search-results-synonyms .keywords-list-container")), getDriver());
	}

	public String getKeywordError() {
		WebElement firstError = findElement(By.className("keywords-invalid"));
		if (firstError.isDisplayed()) {
			return firstError.getText();
		}
		try {
			return findElement(By.className("search-synonyms-error")).getText();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	/* parametric values */
	public SOCheckbox parametricTypeCheckbox(String category, String field) {
		WebElement checkbox = findElement(By.cssSelector("[data-field='" + category.toLowerCase().replace(" ","_") + "'] [data-value='" + field.toUpperCase() + "']"));
		return new SOCheckbox(checkbox, getDriver());
	}

	public void openParametricValuesList() {
		DriverUtil.scrollIntoViewAndClick(getDriver(), findElement(By.cssSelector("[data-target='.collapsible-parametric-option']")));
		Waits.loadOrFadeWait();
	}
}
