package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.menubar.TopNavBar;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class SearchPage extends SearchBase implements AppPage {

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
		return findElement(By.cssSelector(".page-title > strong"));
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

	public void paginateWait() {
		try {
			new WebDriverWait(getDriver(), 3).until(ExpectedConditions.visibilityOf(docLogo()));
		} catch (final StaleElementReferenceException e) {
			loadOrFadeWait();
		}
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
		return findElement(By.cssSelector(".promotions .promotion-name"));
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

	public WebElement docLogo() {
		return findElement(By.cssSelector(".fa-file-o"));
	}

	public boolean isPromotionsBoxVisible() {
		return findElement(By.cssSelector(".promotions")).getAttribute("class").contains("hidden");
	}

	public void selectLanguage(final String language) {
		findElement(By.cssSelector(".current-language-selection")).click();
		findElement(By.xpath(".//a[text()='" + language + "']")).click();
		loadOrFadeWait();
	}

	public String getSelectedLanguage() {
		return findElement(By.cssSelector(".current-language-selection")).getText();
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