package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.MainTabBar;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

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
		return findElement(By.cssSelector(".page-title"));
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

	public void createAPromotion() {
		promoteButton().click();
		searchResultCheckbox(1).click();
		promoteTheseItemsButton().click();
	}

	public static class Placeholder {
		private final TopNavBar topNavBar;

		public Placeholder(final TopNavBar topNavBar) {
			this.topNavBar = topNavBar;
		}

		public SearchPage $searchPage(WebElement element) {
			return new SearchPage(topNavBar, element);
		}
	}
}