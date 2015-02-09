package com.autonomy.abc.selenium.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class SearchBase extends KeywordsBase implements AppPage {

	public SearchBase(final WebElement element, final WebDriver driver) {
		super(element, driver);
	}

	public WebElement searchResultCheckbox(final int resultNumber) {
		return findElement(By.cssSelector(".search-results li:nth-child(" + String.valueOf(resultNumber) + ") .icheckbox_square-blue"));
	}

	public WebElement getResultsBoxByTitle(final String docTitle) {
		return findElement(By.xpath(".//h3[contains(text(), '" + docTitle + "')]/../../div/div/label/div"));
	}

	public WebElement searchResultCheckbox(final String docTitle) {
		return getResultsBoxByTitle(docTitle);
	}

	public String getSearchResultTitle(final int searchResultNumber) {
		return getSearchResult(searchResultNumber).getText();
	}

	public WebElement getSearchResult(final int searchResultNumber) {
		return findElement(By.cssSelector(".search-results li:nth-child(" + String.valueOf(searchResultNumber) + ") h3"));
	}

	public int promotedItemsCount() {
		return findElements(By.cssSelector(".promotions-bucket-document")).size();
	}

	public List<String> promotionsBucketList() {
		final List<String> bucketDocTitles = new ArrayList<>();
		for (final WebElement bucketDoc : findElements(By.cssSelector(".promotions-bucket-document"))) {
			bucketDocTitles.add(bucketDoc.getText());
		}
		return bucketDocTitles;
	}

	public List<WebElement> promotionsBucketWebElements() {
		return findElements(By.xpath(".//*[contains(@class, 'promotions-bucket-document')]/.."));
	}

	public String bucketDocumentTitle(final int bucketNumber) {
		return promotionsBucket().findElement(By.cssSelector(".promotions-bucket-document:nth-child(" + bucketNumber + ")")).getText();
	}

	public WebElement promotionsBucket() {
		return findElement(By.xpath(".//div[@class='promoted-items']/.."));
	}

	public void deleteDocFromWithinBucket(final String docTitle) {
		final String xpathString = cleanXpathString(docTitle);
		promotionsBucket().findElement(By.xpath(".//*[contains(text(), " + xpathString + ")]/../i")).click();
		loadOrFadeWait();
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
		loadOrFadeWait();
		return Integer.parseInt(findElement(By.cssSelector(".pagination-nav.centre")).findElement(By.cssSelector("li.active span")).getText());
	}

	public boolean isBackToFirstPageButtonDisabled() {
		return  getParent(backToFirstPageButton()).getAttribute("class").contains("disabled");
	}

	public WebElement languageButton() {
		return findElement(By.cssSelector(".search-page-controls .dropdown-toggle"));
	}

	public int countSearchResults() {
		return Integer.parseInt(findElement(By.cssSelector(".results-count")).getText());
	}

	public void emptyBucket() {
		for (final WebElement bucketItem : promotionsBucketWebElements()) {
			bucketItem.findElement(By.cssSelector(".remove-bucket-item")).click();
		}
	}

	public WebElement getPromotionBucketElementByTitle(final String docTitle) {
		return findElement(By.cssSelector(".promoted-items")).findElement(By.xpath(".//*[contains(text(), '" + docTitle + "')]"));
	}

	public WebElement getDatabasesTable() {
		return findElement(By.cssSelector(".search-databases-table"));
	}

	public void selectDatabase(final String databaseName) {
		if (!getSelectedDatabases().contains(databaseName) ) {
			getDatabasesTable().findElement(By.xpath(".//td[contains(text(), '" + databaseName + "')]/..")).click();
			loadOrFadeWait();
		}
	}

	public void deselectDatabase(final String databaseName) {
		final List<String> selectedDatabases = getSelectedDatabases();

		if (selectedDatabases.contains(databaseName)) {
			if (selectedDatabases.size() > 1) {
				getDatabasesTable().findElement(By.xpath(".//td[contains(text(), '" + databaseName + "')]/..")).click();
			} else {
				getDatabasesTable().findElement(By.xpath(".//td[contains(text(), 'All')]/..")).click();
			}

			loadOrFadeWait();
		}
	}

	public List<String> getSelectedDatabases() {
		final List<String> selected = new ArrayList<>();

		for (final WebElement tick : getDatabasesTable().findElements(By.xpath(".//i[@class='fa fa-check']/../.."))) {
			selected.add(tick.findElement(By.cssSelector("td:first-child")).getText());
		}

		return selected;
	}
}
