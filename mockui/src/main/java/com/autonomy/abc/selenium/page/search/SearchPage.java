package com.autonomy.abc.selenium.page.search;

import com.autonomy.abc.selenium.element.Dropdown;
import com.autonomy.abc.selenium.element.Pagination;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.autonomy.abc.selenium.page.keywords.KeywordsContainer;
import com.autonomy.abc.selenium.page.keywords.SynonymGroup;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public abstract class SearchPage extends SearchBase implements AppPage {
	public final static int RESULTS_PER_PAGE = 6;
	public final static int MAX_RESULTS = 2500;

	public SearchPage(final WebDriver driver) {
		// specify data-pagename to avoid invisible elements from other pages showing up
		super(new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".wrapper-content [data-pagename='search']"))), driver);
		waitForLoad();
	}

	@Override
	public void waitForLoad() {
		new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-pagename='search'] .search-page-contents")));
	}

	/* title */
	// "Results for _____ (123)"
	public String getHeadingSearchTerm() {
		WebElement heading = getDriver().findElement(By.cssSelector(".heading > b"));
		ElementUtil.scrollIntoView(heading, getDriver());
		return heading.getText();
	}

	// "Results for term (___)"
	public int getHeadingResultsCount() {
		((JavascriptExecutor) getDriver()).executeScript("scroll(0,0)");
		final String totalWithBrackets = new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".page-heading span"))).getText();
		final String totalNoBrackets = totalWithBrackets.substring(1, totalWithBrackets.length() - 1);

		if (totalNoBrackets.equalsIgnoreCase("more than " + MAX_RESULTS)) {
			return MAX_RESULTS + 1;
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

	public WebElement modifiedResultsCheckBox() {
		return findElement(By.className("search-type-toggle"));
	}

	public boolean modifiedResultsShown(){
		return findElement(By.className("search-type-toggle")).findElement(By.className("checkbox-input")).isSelected();
	}

	public void sortBy(final Sort sort) {
		sortDropdown().select(sort.toString());
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
		languageButton().click();
		final List<String> languageList = new ArrayList<>();

		for (final WebElement language : findElements(By.cssSelector(".search-page-controls [role='menuitem']"))) {
			languageList.add(language.getText());
		}

		return languageList;
	}

	/* promotions bucket */
	public List<String> promotionsBucketList() {
		return bucketList(this);
	}

	public WebElement promoteTheseItemsButton() {
		return findElement(By.xpath(".//*[contains(text(), 'Promote items')]"));
	}

	public void promotionsBucketClose() {
		promotionsBucket().findElement(By.cssSelector(".close-link")).click();
		Waits.loadOrFadeWait();
	}

	/* promoted results */
	public WebElement promotionsSummary() {
		return findElement(By.cssSelector(".promotions-summary"));
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
		return !findElement(By.cssSelector(".promotions")).getAttribute("class").contains("hidden");
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
		return findElement(By.cssSelector(".show-more"));
	}

	public WebElement showLessPromotionsButton() {
		return findElement(By.cssSelector(".show-less"));
	}

	public void switchPromotionPage(Pagination pagination) {
		promotionPaginationButton(pagination).click();
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
            titles.add(getSearchResultTitle((i % 6) + 1));

            if((i + 1) % 6 == 0 && (i + 1) != numberOfResults){
				switchResultsPage(Pagination.NEXT);
            }
        }

		return titles;
	}

	@Override
	public WebElement searchResultCheckbox(int resultNumber) {
		// TODO: find others like this, avoid repetition
		return new WebDriverWait(getDriver(),20).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-pagename='search'] .search-results li:nth-child(" + resultNumber + ") label")));
	}

	public void viewFrameClick(final boolean clickLogo, final int resultIndex) {
		if (clickLogo) {
			docLogo(resultIndex).click();
		} else {
			searchResult(resultIndex).click();
		}
	}

	public WebElement docLogo(final int searchResultNumber) {
		return findElement(By.cssSelector(".search-results li:nth-child(" + String.valueOf(searchResultNumber) + ") .fa-file-o"));
	}

	public int countPinToPositionLabels() {
		return findElements(By.cssSelector(".injected-promotion .fa-thumb-tack")).size();
	}

	/* keywords */
	public List<String> youSearchedFor() {
		WebElement searchTermsList = findElement(By.cssSelector(".search-terms-list"));
		ElementUtil.scrollIntoView(searchTermsList, getDriver());
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

	// TODO: use keywordsContainer
	public int countSynonymLists() {
		return (findElement(By.className("search-synonyms-keywords"))).findElements(By.className("add-synonym")).size();
	}

	// TODO: use keywordsContainer
	@Deprecated
	public List<String> getLeadSynonymsList() {
		final List<String> leadSynonyms = new ArrayList<>();
		for (final WebElement synonymGroup : findElements(By.cssSelector(".keywords-list > ul > li"))) {
			leadSynonyms.add(synonymGroup.findElement(By.cssSelector("li:first-child span span")).getText());
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

	/* parametric values */
	private WebElement contentTypeDiv() {
		return findElement(By.cssSelector("[data-field='content_type']"));
	}

	/**
	 * TODO - if possible take out
	 * @param contentType		String to filter by
	 * @return					Number of results in filtered search
	 */
	public int filterByContentType(String contentType) {
		WebElement li = contentTypeDiv().findElement(By.cssSelector("[data-value='" + contentType + "']"));

		if(!li.isDisplayed()){
			openParametricValuesList();
		}

		String spanResultCount = li.findElement(By.tagName("span")).getText().split(" ")[1];
		int resultCount = Integer.parseInt(spanResultCount.substring(1, spanResultCount.length() - 1));
		li.findElement(By.tagName("ins")).click();
		return resultCount;
	}

	public void openParametricValuesList() {
		ElementUtil.scrollIntoViewAndClick(findElement(By.cssSelector("[data-target='.collapsible-parametric-option']")), getDriver());
		Waits.loadOrFadeWait();
	}

	/* helper methods */
	public List<String> addToBucket(int finalNumberOfDocs) {
		final List<String> promotedDocTitles = new ArrayList<>();
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(promoteTheseItemsButton()));
		waitForSearchLoadIndicatorToDisappear(60);
		for (int i = 0; i < finalNumberOfDocs; i++) {
			final int checkboxIndex = i % RESULTS_PER_PAGE + 1;
			searchResultCheckbox(checkboxIndex).click();
			promotedDocTitles.add(getSearchResultTitle(checkboxIndex));

			// Change page when we have checked all boxes on the current page, if we have more to check
			if (i < finalNumberOfDocs - 1 && checkboxIndex == RESULTS_PER_PAGE) {
				switchResultsPage(Pagination.NEXT);
			}
		}
		return promotedDocTitles;
	}
}
