package com.autonomy.abc.selenium.page.search;

import com.autonomy.abc.selenium.element.Dropdown;
import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.autonomy.abc.selenium.page.keywords.KeywordsContainer;
import com.autonomy.abc.selenium.page.keywords.SynonymGroup;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.language.Language;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class SearchPage extends SearchBase implements AppPage {
	private final static Logger LOGGER = LoggerFactory.getLogger(SearchPage.class);
	public final static int RESULTS_PER_PAGE = 6;

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
		final String bracketedSearchResultsTotal = new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".page-heading span"))).getText();
		return Integer.parseInt(bracketedSearchResultsTotal.substring(1, bracketedSearchResultsTotal.length() - 1));
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

	// TODO: use languageDropdown
	public String getSelectedLanguage() {
		return findElement(By.cssSelector(".current-language-selection")).getText();
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
		loadOrFadeWait();
	}

	/* promoted results */
	// TODO: on-prem only
	public WebElement promotionsLabel() {
		return findElement(By.cssSelector(".promotions .promotion-name"));
	}

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

	public List<String> promotionsSummaryList(final boolean fullList) {
		waitForPromotionsLoadIndicatorToDisappear();
		final List<String> promotionsList = new ArrayList<>();

		if (!findElement(By.cssSelector(".show-more")).isDisplayed()) {
			promotionsList.addAll(getVisiblePromotedItems());
		} else {
			showMorePromotions();

			if (!fullList) {
				promotionsList.addAll(getVisiblePromotedItems());
			} else {
				promotionsList.addAll(getVisiblePromotedItems());

				if (promotionSummaryForwardButton().isDisplayed()) {
					promotionSummaryForwardToEndButton().click();
					loadOrFadeWait();
					promotionsList.addAll(getVisiblePromotedItems());
					final int numberOfPages = Integer.parseInt(promotionSummaryBackButton().getAttribute("data-page"));

					//starting at 1 because I add the results for the first page above
					for (int i = 1; i < numberOfPages; i++) {
						promotionSummaryBackButton().click();
						new WebDriverWait(getDriver(), 3).until(ExpectedConditions.visibilityOf(promotionsLabel()));

						promotionsList.addAll(getVisiblePromotedItems());
					}
				}
			}
		}

		return promotionsList;
	}

	private List<String> getVisiblePromotedItems() {
		return ElementUtil.getTexts(findElements(By.cssSelector(".promotions-list h3 a")));
	}

	public List<WebElement> getPromotedResults() {
		return findElements(By.cssSelector(".promotions-list h3"));
	}

	public WebElement getPromotedResult(int number) {
		return new WebDriverWait(getDriver(),60).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".promotions-list li:nth-child(" + String.valueOf(number) + ") h3")));
	}

	public List<String> getPromotedDocumentTitles(){
		List<String> titles = new ArrayList<>();
		for(WebElement promotion : getPromotedResults()){
			titles.add(promotion.getText());
		}
		return titles;
	}

	private List<String> getVisiblePromotionLabels() {
		final List<String> labelList = new LinkedList<>();

		for (final WebElement labelTitle : findElements(By.cssSelector(".promotions-list .search-result-title"))) {
			labelList.add(labelTitle.getText());
		}

		return labelList;
	}

	public List<String> getPromotionSummaryLabels() {
		loadOrFadeWait();
		final List<String> labelList = new ArrayList<>();

		if (!findElement(By.cssSelector(".show-more")).isDisplayed()) {
			labelList.addAll(getVisiblePromotionLabels());
		} else {
			showMorePromotions();
			labelList.addAll(getVisiblePromotionLabels());

			if (promotionSummaryForwardButton().isDisplayed()) {
				promotionSummaryForwardToEndButton().click();
				loadOrFadeWait();
				labelList.addAll(getVisiblePromotionLabels());
				final int numberOfPages = Integer.parseInt(promotionSummaryBackButton().getAttribute("data-page"));

				//starting at 1 because I add the results for the first page above
				for (int i = 1; i < numberOfPages; i++) {
					promotionSummaryBackButton().click();
					new WebDriverWait(getDriver(), 3).until(ExpectedConditions.visibilityOf(promotionsLabel()));

					labelList.addAll(getVisiblePromotionLabels());
				}
			}
		}

		return labelList;
	}

	public List<String> getPromotionLabels() {
		loadOrFadeWait();
		final List<String> labelList = new ArrayList<>();

		if (!findElement(By.cssSelector(".show-more")).isDisplayed()) {
			labelList.addAll(getPromotionTypeLabels());
		} else {
			showMorePromotions();
			labelList.addAll(getPromotionTypeLabels());

			if (promotionSummaryForwardButton().isDisplayed()) {
				promotionSummaryForwardToEndButton().click();
				loadOrFadeWait();
				labelList.addAll(getPromotionTypeLabels());
				final int numberOfPages = Integer.parseInt(promotionSummaryBackButton().getAttribute("data-page"));

				//starting at 1 because I add the results for the first page above
				for (int i = 1; i < numberOfPages; i++) {
					promotionSummaryBackButton().click();
					new WebDriverWait(getDriver(), 3).until(ExpectedConditions.visibilityOf(promotionsLabel()));

					labelList.addAll(getPromotionTypeLabels());
				}
			}
		}

		return labelList;
	}

	private List<String> getPromotionTypeLabels() {
		final List<String> labelList = new LinkedList<>();

		for (final WebElement labelTitle : findElements(By.className("promotion-name"))) {
			labelList.add(labelTitle.getText());
		}

		return labelList;
	}

	public boolean isPromotionsBoxVisible() {
		return !findElement(By.cssSelector(".promotions")).getAttribute("class").contains("hidden");
	}

	public void showMorePromotions() {
		showMorePromotionsButton().click();
		loadOrFadeWait();
	}

	public void showLessPromotions() {
		showLessPromotionsButton().click();
		loadOrFadeWait();
	}

	public WebElement showMorePromotionsButton() {
		return findElement(By.cssSelector(".show-more"));
	}

	public WebElement showLessPromotionsButton() {
		return findElement(By.cssSelector(".show-less"));
	}

	public WebElement promotionSummaryBackToStartButton() {
		return getParent(findElement(By.cssSelector(".promotions-pagination .hp-previous-chapter")));
	}

	public WebElement promotionSummaryBackButton() {
		return getParent(findElement(By.cssSelector(".promotions-pagination .hp-previous")));
	}

	public WebElement promotionSummaryForwardButton() {
		return getParent(findElement(By.cssSelector(".promotions-pagination .hp-next")));
	}

	public WebElement promotionSummaryForwardToEndButton() {
		return getParent(findElement(By.cssSelector(".promotions-pagination .hp-next-chapter")));
	}

	/* search results */
	public List<String> getSearchResultTitles(int numberOfResults){
        List<String> titles = new ArrayList<>();

        for(int i = 0; i < numberOfResults; i++){
            titles.add(getSearchResultTitle((i % 6) + 1));

            if((i + 1) % 6 == 0){
                forwardPageButton().click();
                loadOrFadeWait();
                waitForSearchLoadIndicatorToDisappear();
                loadOrFadeWait();
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
			getDocLogo(resultIndex).click();
		} else {
			getSearchResult(resultIndex).click();
		}
	}

	public WebElement getDocLogo(final int searchResultNumber) {
		return findElement(By.cssSelector(".search-results li:nth-child(" + String.valueOf(searchResultNumber) + ") .fa-file-o"));
	}

	public int countPinToPositionLabels() {
		return findElements(By.cssSelector(".injected-promotion .fa-thumb-tack")).size();
	}

	public void paginateWait() {
		try {
			waitForSearchLoadIndicatorToDisappear();
		} catch (final StaleElementReferenceException|NoSuchElementException n) {
			loadOrFadeWait();
		}
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

	// TODO: use keywordsContainer / deprecate
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
	private WebElement getContentTypeDiv() {
		return findElement(By.cssSelector("[data-field='content_type']"));
	}

	/**
	 *
	 * @param contentType		String to filter by
	 * @return					Number of results in filtered search
	 */
	public int filterByContentType(String contentType) {
		WebElement li = getContentTypeDiv().findElement(By.cssSelector("[data-value='" + contentType + "']"));

		if(!li.isDisplayed()){
			openParametricValuesList();
		}

		String spanResultCount = li.findElement(By.tagName("span")).getText().split(" ")[1];
		int resultCount = Integer.parseInt(spanResultCount.substring(1, spanResultCount.length() - 1));
		li.findElement(By.tagName("ins")).click();
		return resultCount;
	}

	private WebElement getAuthorDiv(){
		return findElement(By.cssSelector("[data-field='author']"));
	}

	public int filterByAuthor(String author) {
		WebElement li = getAuthorDiv().findElement(By.cssSelector("[data-value='" + author + "']"));
		String spanResultCount = li.findElement(By.tagName("span")).getText().split(" ")[1];
		int resultCount = Integer.parseInt(spanResultCount.substring(1, spanResultCount.length() - 1));
		li.findElement(By.tagName("ins")).click();
		return resultCount;
	}

	public void openParametricValuesList() {
		scrollIntoViewAndClick(findElement(By.cssSelector("[data-target='.collapsible-parametric-option']")));
		loadOrFadeWait();
	}

	/* helper methods */
	// TODO: move to service?
	public String createAPromotion() {
		promoteTheseDocumentsButton().click();
		searchResultCheckbox(1).click();
		final String promotedDocTitle = getSearchResultTitle(1);
		promoteTheseItemsButton().click();
		return promotedDocTitle;
	}

	public List<String> createAMultiDocumentPromotion(final int numberOfDocs) {
		promoteTheseDocumentsButton().click();
		loadOrFadeWait();
		List<String> promotedDocTitles = addToBucket(numberOfDocs);
		scrollIntoViewAndClick(promoteTheseItemsButton());
		loadOrFadeWait();
		return promotedDocTitles;
	}

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
				// TODO: does this need to be javascriptClick?
				forwardPageButton().click();
				waitForSearchLoadIndicatorToDisappear();
			}
		}
		return promotedDocTitles;
	}
}
