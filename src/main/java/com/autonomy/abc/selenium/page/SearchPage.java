package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class SearchPage extends AppElement implements AppPage {

	public SearchPage(final TopNavBar topNavBar, final WebElement $el) {
		super($el, topNavBar.getDriver());
	}

	@Override
	public void navigateToPage() {
		getDriver().get("search");
	}

	public WebElement showHideUnmodifiedResults() {
		return findElement(By.cssSelector(".results-toggle"));
	}

	public WebElement searchTitle() {
		return findElement(By.cssSelector(".page-title > span"));
	}

	public WebElement promoteButton() {
		return findElement(By.cssSelector(".promotions-bucket-button"));
	}

	public WebElement promotionsBucket() {
		return findElement(By.xpath(".//div[@class='promoted-items']/.."));
	}

	public WebElement promoteTheseItemsButton() {
		return findElement(By.cssSelector(".promote-these-items"));
	}

	public void promotionsBucketClose() {
		promotionsBucket().findElement(By.cssSelector(".close-link")).click();
	}

	public WebElement searchResultCheckbox(final int resultNumber) {
		return findElement(By.cssSelector(".search-results li:nth-child(" + String.valueOf(resultNumber) + ") .icheckbox_square-blue"));
	}

	public int promotedItemsCount() {
		return findElements(By.cssSelector(".promoted-items .fa")).size();
	}

	public WebElement backToFirstPageButton() {
		return getParent(findElement(By.cssSelector(".pagination-nav.centre")).findElement(By.xpath(".//i[contains(@class, 'fa-angle-double-left')]")));
	}

	public WebElement backPageButton() {
		return findElement(By.cssSelector(".pagination-nav.centre")).findElement(By.xpath(".//i[contains(@class, 'fa-angle-left')]/.."));
	}

	public WebElement forwardToLastPageButton() {
		return findElement(By.cssSelector(".pagination-nav.centre")).findElement(By.xpath(".//i[contains(@class, 'fa-angle-double-right')]/.."));
	}

	public WebElement forwardPageButton() {
		return findElement(By.cssSelector(".pagination-nav.centre")).findElement(By.xpath(".//i[contains(@class, 'fa-angle-right')]/.."));
	}

	public boolean isPageActive(final int pageNumber) {
		try {
			return findElement(By.cssSelector(".pagination-nav.centre")).findElement(By.xpath(".//span[text()='" + String.valueOf(pageNumber) + "']/..")).getAttribute("class").contains("active");
		} catch (final NoSuchElementException e){
			return false;
		}
	}

	public int getCurrentPageNumber() {
		return Integer.parseInt(findElement(By.cssSelector(".pagination-nav.centre")).findElement(By.cssSelector("li.active")).getText());
	}

	public boolean isBackToFirstPageButtonDisabled() {
		return  getParent(backToFirstPageButton()).getAttribute("class").contains("disabled");
	}

	public String getSearchResultTitle(final int searchResultNumber) {
		return findElement(By.cssSelector(".search-results li:nth-child(" + String.valueOf(searchResultNumber) + ") a")).getText();
	}

	public String bucketDocumentTitle(final int bucketNumber) {
		final Actions mouseOver = new Actions(getDriver());
		mouseOver.moveToElement(promotionsBucket().findElement(By.cssSelector(".promotions-bucket-document:nth-child(" + String.valueOf(bucketNumber) + ")"))).perform();
		return promotionsBucket().findElement(By.cssSelector(".promotions-bucket-document .tooltip")).getText();
	}

	public String createAPromotion() {
		promoteButton().click();
		searchResultCheckbox(1).click();
		final String promotedDocTitle = getSearchResultTitle(1);
		promoteTheseItemsButton().click();
		return promotedDocTitle;
	}

	public List<String> createAMultiDocumentPromotion(final int finalNumberOfDocs) {
		promoteButton().click();
		final int checkboxesPerPage = 6;
		final List<String> promotedDocTitles = new ArrayList<>();

		for (int i = 0; i < finalNumberOfDocs; i++) {
			final int checkboxIndex = i % checkboxesPerPage + 1;
			searchResultCheckbox(checkboxIndex).click();
			promotedDocTitles.add(getSearchResultTitle(checkboxIndex));

			// Change page when we have checked all boxes on the current page, if we have more to check
			if (i < finalNumberOfDocs - 1 && checkboxIndex == checkboxesPerPage) {
				forwardPageButton().click();
				loadOrFadeWait();
			}
		}

		promoteTheseItemsButton().click();
		return promotedDocTitles;
	}

	public void showMoreButton() {
		findElement(By.cssSelector(".show-more")).click();
		loadOrFadeWait();
	}

	public void showLessButton() {
		findElement(By.cssSelector(".show-less")).click();
		loadOrFadeWait();
	}

	public WebElement promotionsLabel() {
		return findElement(By.cssSelector(".promotions .total-promotions"));
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

	public static class Placeholder {
		private final TopNavBar topNavBar;

		public Placeholder(final TopNavBar topNavBar) {
			this.topNavBar = topNavBar;
		}

		public SearchPage $searchPage(final WebElement element) {
			return new SearchPage(topNavBar, element);
		}
	}
}