package com.autonomy.abc.selenium.page.search;

import com.autonomy.abc.selenium.page.AppPage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class SearchPage extends SearchBase implements AppPage {

	public SearchPage(final WebDriver driver) {
		super(new WebDriverWait(driver,30).until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper-content"))), driver);
		waitForLoad();
	}

	public List<String> promotionsBucketList() {
		return bucketList(this);
	}

	@Override
	public void waitForLoad() {
		new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.className("search-page-contents")));
	}

	public WebElement modifiedResultsCheckBox() {
        return findElement(By.className("search-type-toggle"));
	}

    public boolean modifiedResultsShown(){
        return findElement(By.className("search-type-toggle")).findElement(By.className("checkbox-input")).isSelected();
    }

	public WebElement searchTitle() {
		return getDriver().findElement(By.cssSelector(".heading > strong"));
	}

	public WebElement promoteTheseDocumentsButton() {
		return findElement(By.xpath(".//button[contains(text(), 'Promote these documents')]"));
	}

	public WebElement promoteTheseItemsButton() {
		return findElement(By.xpath(".//*[contains(text(), 'Promote these items')]"));
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

	public List<String> createAMultiDocumentPromotion(final int finalNumberOfDocs) {
		promoteTheseDocumentsButton().click();
		loadOrFadeWait();
		final int checkboxesPerPage = 6;
		final List<String> promotedDocTitles = new ArrayList<>();

		for (int i = 0; i < finalNumberOfDocs; i++) {
			final int checkboxIndex = i % checkboxesPerPage + 1;
			searchResultCheckbox(checkboxIndex).click();
			promotedDocTitles.add(getSearchResultTitle(checkboxIndex));

			// Change page when we have checked all boxes on the current page, if we have more to check
			if (i < finalNumberOfDocs - 1 && checkboxIndex == checkboxesPerPage) {
				loadOrFadeWait();
				javascriptClick(forwardPageButton());
				loadOrFadeWait();
				waitForSearchLoadIndicatorToDisappear();
				loadOrFadeWait();
			}
		}

		promoteTheseItemsButton().click();
		loadOrFadeWait();
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
		return getParent(findElement(By.cssSelector(".fa-angle-double-left")));
	}

	public WebElement promotionSummaryBackButton() {
		return getParent(findElement(By.cssSelector(".fa-angle-left")));
	}

	public WebElement promotionSummaryForwardButton() {
		return getParent(findElement(By.cssSelector(".fa-angle-right")));
	}

	public WebElement promotionSummaryForwardToEndButton() {
		return getParent(findElement(By.cssSelector(".fa-angle-double-right")));
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
		return findElement(By.xpath(".//button[contains(text(), 'Promote this query')]"));
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

}