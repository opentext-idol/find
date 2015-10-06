package com.autonomy.abc.selenium.page.promotions;


import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import com.autonomy.abc.selenium.element.Editable;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.element.InlineEdit;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class PromotionsPage extends AppElement implements AppPage {

	public PromotionsPage(WebDriver driver) {
		super(new WebDriverWait(driver,30).until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper-content"))),driver);
		waitForLoad();
	}

	public WebElement promoteExistingButton() {
		return findElement(By.xpath(".//a[text()[contains(., 'Promote existing documents')]]"));
	}

	public WebElement getPromotionLinkWithTitleContaining(final String promotionTitleSubstring) {
		return findElement(By.xpath(".//h3[contains(text(), '" + promotionTitleSubstring.replace("\"", "").split("\\s+")[0] + "')]/../../.."));
	}

	// TODO: move to Promotions
	public void deletePromotion() {
		final WebElement extraFunctionsDropdown = findElement(By.cssSelector(".extra-functions .dropdown-toggle"));
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(extraFunctionsDropdown));
		extraFunctionsDropdown.click();
		loadOrFadeWait();
		findElement(By.cssSelector(".promotion-view-delete")).click();
		loadOrFadeWait();
		final ModalView deleteModal = ModalView.getVisibleModalView(getDriver());
		deleteModal.findElement(By.cssSelector(".btn-danger")).click();
		loadOrFadeWait();
		new WebDriverWait(getDriver(), 60).until(ExpectedConditions.visibilityOf(promoteExistingButton()));
	}

	public void schedulePromotion() {
		final WebElement extraFunctionsDropdown = findElement(By.cssSelector(".extra-functions .dropdown-toggle"));
		new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOf(extraFunctionsDropdown));
		extraFunctionsDropdown.click();

		final WebElement scheduleButton = findElement(By.xpath(".//a[contains(text(), 'Schedule')]"));
		new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOf(scheduleButton));
		scheduleButton.click();
	}

	public WebElement spotlightButton() {
		return findElement(By.cssSelector(".promotion-view-name-dropdown"));
	}

	public Editable promotionPosition() {
		return new InlineEdit(findElement(By.className("promotion-position-edit")), getDriver());
	}

	public List<WebElement> promotionsList() {
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.invisibilityOfElementLocated(By.className("loading-indicator")));
		return findElements(By.cssSelector(".promotion-list-container li a"));
	}

	public List<String> getPromotionTitles() {
		List<String> promotionTitles = new ArrayList<>();
		for (WebElement promotion : promotionsList()) {
			promotionTitles.add(promotion.findElement(By.tagName("h3")).getText());
		}
		return promotionTitles;
	}

	public void deleteAllPromotions() {
		for (final WebElement promotion : promotionsList()) {
			promotion.click();
			deletePromotion();
			loadOrFadeWait();
		}
	}

	public List<String> getSearchTriggersList() {
		final List<String> searchTriggerList = new ArrayList<>();
		loadOrFadeWait();

		for (final WebElement trigger : findElements(By.cssSelector(".promotion-view-match-terms .term"))) {
			searchTriggerList.add(trigger.getAttribute("data-id"));
		}

		return searchTriggerList;
	}

	public void addSearchTrigger(final String searchTrigger) {
		triggerTextBox().clear();
		triggerTextBox().sendKeys(searchTrigger);
		loadOrFadeWait();
		tryClickThenTryParentClick(triggerAddButton());
		loadOrFadeWait();
	}

	public List <String> getPromotedList() {
		final List <String> docTitles = new ArrayList<>();
		for (final WebElement docTitle : findElements(By.cssSelector(".promotion-list-container h3"))) {
			docTitles.add(docTitle.getText());
		}
		return docTitles;
	}

	public WebElement triggerTextBox() {
		return findElement(By.cssSelector(".promotion-match-terms [name='words']"));
	}

	public WebElement triggerAddButton() {
		return findElement(By.cssSelector(".promotion-match-terms")).findElement(By.xpath(".//i[contains(@class, 'fa-plus')]/.."));
	}

	public void removeSearchTrigger(final String searchTrigger) {
		loadOrFadeWait();
		waitUntilClickableThenClick(triggerRemoveButton(searchTrigger));
	}

	public WebElement clickableSearchTrigger(final String triggerName) {
		return findElement(By.cssSelector(".promotion-view-match-terms [data-id='" + triggerName + "']"));
	}

	public WebElement triggerRemoveButton(final String triggerName) {
		return findElement(By.cssSelector(".promotion-view-match-terms [data-id='" + triggerName + "'] .remove-word"));
	}

	public WebElement backButton() {
		return findElement(By.xpath(".//a[text()[contains(., 'Back')]]"));
	}

	public String getPromotionTitle() {
		return findElement(By.cssSelector(".promotion-title-edit span")).getText();
	}

	public Editable name() {
		return new InlineEdit(findElement(By.className("promotion-title-edit")), getDriver());
	}

	public void createNewTitle(final String title) throws InterruptedException {
		name().setValueAndWait(title);
	}

	public String getPromotionType() {
		return findElement(By.cssSelector(".promotion-view-name")).getText();
	}

	public void changeSpotlightType(final String promotionType) {
		// clear notifications
		new WebDriverWait(getDriver(), 5).until(GritterNotice.notificationsDisappear());
		findElement(By.cssSelector(".promotion-view-name-dropdown .clickable")).click();
		findElement(By.cssSelector(".promotion-view-name-dropdown [data-spotlight-type='" + promotionType + "']")).click();
		new WebDriverWait(getDriver(),3).until(GritterNotice.notificationAppears());
	}

	public WebElement promotedDocument(final String title) {
		return findElement(By.xpath(".//ul[contains(@class, 'promoted-documents-list')]")).findElement(By.xpath(".//a[contains(text(), '" + title + "')]/../.."));
	}

	public String promotedDocumentSummary(final String title) {
		return promotedDocument(title).findElement(By.cssSelector("p")).getText();
	}

	public void deleteDocument(final String title) {
		promotedDocument(title).findElement(By.cssSelector(".remove-document-reference.clickable")).click();
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".search-result-item .fa-spin")));
	}

	public WebElement addMorePromotedItemsButton() {
		return findElement(By.xpath(".//a[text()[contains(., 'Add More')]]"));

	}

		public List<String> getDynamicPromotedList(final boolean fullList) {
			loadOrFadeWait();
			final List<String> promotionsList = new ArrayList<>();

			if (!fullList) {
				promotionsList.addAll(getVisiblePromotionItems());
			} else {
				promotionsList.addAll(getVisiblePromotionItems());

				if (promotionSummaryForwardButton().isDisplayed()) {
					loadOrFadeWait();

					javascriptClick(promotionSummaryForwardToEndButton());
					loadOrFadeWait();
					promotionsList.addAll(getVisiblePromotionItems());
					final int numberOfPages = Integer.parseInt(promotionSummaryBackButton().getAttribute("data-page"));

					//starting at 1 because I add the results for the first page above
					for (int i = 1; i < numberOfPages; i++) {
						loadOrFadeWait();
						javascriptClick(promotionSummaryBackButton());
						loadOrFadeWait();
						new WebDriverWait(getDriver(), 6).until(ExpectedConditions.visibilityOf(docLogo()));

						promotionsList.addAll(getVisiblePromotionItems());
					}
				}
			}

			return promotionsList;
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
	private List<String> getVisiblePromotionItems() {
		final List<String> promotionsList = new LinkedList<>();

		for (final WebElement promotionTitle : findElements(By.cssSelector(".query-search-results .search-results h3 a"))) {
			promotionsList.add(promotionTitle.getText());
		}

		return promotionsList;
	}

	public WebElement docLogo() {
		return findElement(By.cssSelector(".fa-file-o"));
	}

	public String getLanguage() {
		return findElement(By.cssSelector(".promotion-language")).getText();
	}

	public WebElement promotionsSearchFilter() {
		return findElement(By.cssSelector(".search-filter")).findElement(By.xpath(".//input[contains(@placeholder, 'Search for promotions...')]"));
	}

	public WebElement promotionsCategoryFilterButton() {
		return findElement(By.cssSelector(".category-filter .dropdown-toggle"));
	}

	public String promotionsCategoryFilterValue() {
		return promotionsCategoryFilterButton().findElement(By.cssSelector(".filter-type-name")).getText();
	}

	public void selectPromotionsCategoryFilter(final String filterBy) {
		promotionsCategoryFilterButton().click();
		findElement(By.cssSelector(".type-filter")).findElement(By.xpath(".//a[contains(text(), '" + filterBy + "')]")).click();
		loadOrFadeWait();
	}

	public void clearPromotionsSearchFilter() {
		promotionsSearchFilter().clear();
		promotionsSearchFilter().sendKeys("a");
		promotionsSearchFilter().sendKeys(Keys.BACK_SPACE);
	}

	public Editable queryText() {
		return new InlineEdit(findElement(By.className("promotion-query-edit")), getDriver());
	}

	public String getQueryText() {
		return queryText().getValue();
	}

	public void editQueryText(final String newQueryText) {
		queryText().setValueAndWait(newQueryText);
	}


	@Override
	public void waitForLoad() {
		new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Promote existing documents")));

		new WebDriverWait(getDriver(),30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				for(WebElement indicator : driver.findElements(By.className("loading-indicator"))){
					if (indicator.isDisplayed()){
						return false;
					}
				}

				return true;
			}
		});
	}

	public void waitForTriggerUpdate() {
		new WebDriverWait(getDriver(), 15).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".promotion-match-terms .fa-spin")));
	}
}
