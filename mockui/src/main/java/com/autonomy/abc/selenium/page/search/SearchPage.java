package com.autonomy.abc.selenium.page.search;

import com.autonomy.abc.selenium.element.Checkbox;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class SearchPage extends SearchBase implements AppPage {
	public final static int RESULTS_PER_PAGE = 6;

	public SearchPage(final WebDriver driver) {
		super(new WebDriverWait(driver,30).until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper-content"))), driver);
		waitForLoad();
	}

	public List<String> promotionsBucketList() {
		return bucketList(this);
	}

	@Override
	public void waitForLoad() {
		// there can be multiple search-page-contents (some invisible) due to e.g. edit document references
		new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-pagename='search'] .search-page-contents")));
	}

	public WebElement modifiedResultsCheckBox() {
        return findElement(By.className("search-type-toggle"));
	}

    public boolean modifiedResultsShown(){
        return findElement(By.className("search-type-toggle")).findElement(By.className("checkbox-input")).isSelected();
    }

	public WebElement searchTitle() {
		return getDriver().findElement(By.cssSelector(".heading > b"));
	}

	public WebElement promoteTheseDocumentsButton() {
		return findElement(By.xpath(".//button[contains(text(), 'Promote documents')]"));
	}

	public WebElement promoteTheseItemsButton() {
		return findElement(By.xpath(".//*[contains(text(), 'Promote items')]"));
	}

	public void promotionsBucketClose() {
		promotionsBucket().findElement(By.cssSelector(".close-link")).click();
		loadOrFadeWait();
	}

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

	public void paginateWait() {
		try {
			waitForSearchLoadIndicatorToDisappear();
		} catch (final StaleElementReferenceException|NoSuchElementException n) {
			loadOrFadeWait();
		}
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

	public List<String> getSearchTermsList() {
		final List<String> searchTerms = new ArrayList<>();
		for (final WebElement searchTerm : findElements(By.cssSelector(".search-terms-list span"))) {
				searchTerms.add(searchTerm.getText());
		}
		return searchTerms;
	}

	public boolean isPromotionsBoxVisible() {
		return !findElement(By.cssSelector(".promotions")).getAttribute("class").contains("hidden");
	}

	public abstract void selectLanguage(final String language);

    @Deprecated
    public void selectLanguage(final String language, final String type){
        selectLanguage(language);
    }

	public WebElement promoteThisQueryButton() {
		//The space is deliberate!
		return findElement(By.xpath("//button[text()=' Promote query']"));
	}

	public List<String> promotionsSummaryList(final boolean fullList) {
		loadOrFadeWait();
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
		final List<String> promotionsList = new LinkedList<>();

		for (final WebElement promotionTitle : findElements(By.cssSelector(".promotions-list h3 a"))) {
			promotionsList.add(promotionTitle.getText());
		}

		return promotionsList;
	}

	public List<WebElement> getPromotedResults() {
		return findElements(By.cssSelector(".promotions-list h3"));
	}

	public WebElement getPromotedResult(int number) {
		return new WebDriverWait(getDriver(),60).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".promotions-list li:nth-child(" + String.valueOf(number) + ") h3")));
	}

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

	public List<String> getLanguageList() {
		languageButton().click();
		final List<String> languageList = new ArrayList<>();

		for (final WebElement language : findElements(By.cssSelector(".search-page-controls [role='menuitem']"))) {
			languageList.add(language.getText());
		}

		return languageList;
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

	public WebElement getDocLogo(final int searchResultNumber, WebDriverWait wait) {
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".search-results li:nth-child(" + String.valueOf(searchResultNumber) + ") .fa-file-o")));
	}

	public int countPinToPositionLabels() {
		return findElements(By.cssSelector(".injected-promotion .fa-thumb-tack")).size();
	}

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

	public List<Checkbox> indexList() {
		List<Checkbox> checkboxes = new ArrayList<>();
		for (WebElement element : findElements(By.cssSelector(".databases-list .checkbox"))) {
			checkboxes.add(new Checkbox(element, getDriver()));
		}
		return checkboxes;
	}

	public int countKeywords() {
		return findElements(By.cssSelector(".search-synonyms-keywords .remove-keyword")).size();
	}

	public void deleteSynonym(String synonym) throws InterruptedException {
		LoggerFactory.getLogger(SearchPage.class).info("Deleting '" + synonym + "'");
		getSynonymIcon(synonym).click();
		waitForRefreshIconToDisappear();
	}

	@Override
	public WebElement getSynonymIcon(String synonym) {
		return findElement(By.xpath("//div[@data-pagename='search']//span[text()='"+synonym+"']/../i"));
	}

	public List<String> getPromotedDocumentTitles(){
		List<String> titles = new ArrayList<>();
		for(WebElement promotion : getPromotedResults()){
			titles.add(promotion.getText());
		}
		return titles;
	}
}
