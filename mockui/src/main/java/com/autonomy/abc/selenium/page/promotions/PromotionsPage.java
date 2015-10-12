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
	PromotionsDetailPage promotionsDetailPage;

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

	public WebElement promotionDeleteButton(final String title) {
		return promotionDeleteButton(getPromotionLinkWithTitleContaining(title));
	}

	public WebElement promotionDeleteButton(final WebElement promotion) {
		return promotion.findElement(By.className("promotion-delete"));
	}

	// use PromotionActionFactory.makeDeletePromotion(...)
	@Deprecated
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

	// use PromotionsDetailPage.spotlightTypeDropdown()
	@Deprecated
	public WebElement spotlightButton() {
		return findElement(By.cssSelector(".promotion-view-name-dropdown"));
	}

	// use PromotionsDetailPage.pinPosition()
	@Deprecated
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

	@Deprecated
	public void deleteAllPromotions() {
		List<WebElement> promotions = promotionsList();

		for(WebElement promotion : promotions){
			promotion.findElement(By.className("promotion-delete")).click();
			loadOrFadeWait();
			getDriver().findElement(By.className("modal-action-button")).click();
			loadOrFadeWait();
		}

		new WebDriverWait(getDriver(),promotions.size() * 10).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return findElements(By.className("promotion-delete")).size() == 0;
			}
		});
	}

	// use PromotionsDetailPage.getTriggerList()
	@Deprecated
	public List<String> getSearchTriggersList() {
		final List<String> searchTriggerList = new ArrayList<>();
		loadOrFadeWait();

		for (final WebElement trigger : findElements(By.cssSelector(".promotion-view-match-terms .term"))) {
			searchTriggerList.add(trigger.getAttribute("data-id"));
		}

		return searchTriggerList;
	}

	// use PromotionsDetailPage.triggerAddBox()
	@Deprecated
	public void addSearchTrigger(final String searchTrigger) {
		triggerTextBox().clear();
		triggerTextBox().sendKeys(searchTrigger);
		loadOrFadeWait();
		tryClickThenTryParentClick(triggerAddButton());
		loadOrFadeWait();
	}

	// use PromotionsDetailPage.getPromotedTitles()
	@Deprecated
	public List <String> getPromotedList() {
		final List <String> docTitles = new ArrayList<>();
		for (final WebElement docTitle : findElements(By.cssSelector(".promoted-documents-list h3"))) {
			docTitles.add(docTitle.getText());
		}
		return docTitles;
	}

	// use PromotionsDetailPage.triggerAddBox()
	@Deprecated
	public WebElement triggerTextBox() {
		return findElement(By.cssSelector(".promotion-match-terms [name='words']"));
	}

	// use PromotionsDetailPage.triggerAddBox()
	@Deprecated
	public WebElement triggerAddButton() {
		return findElement(By.cssSelector(".promotion-match-terms")).findElement(By.xpath(".//i[contains(@class, 'fa-plus')]/.."));
	}

	// use PromotionsDetailPage.trigger()
	@Deprecated
	public void removeSearchTrigger(final String searchTrigger) {
		loadOrFadeWait();
		waitUntilClickableThenClick(triggerRemoveButton(searchTrigger));
	}

	// use PromotionsDetailPage.trigger()
	@Deprecated
	public WebElement clickableSearchTrigger(final String triggerName) {
		return findElement(By.cssSelector(".promotion-view-match-terms [data-id='" + triggerName + "']"));
	}

	// use PromotionsDetailPage.trigger()
	@Deprecated
	public WebElement triggerRemoveButton(final String triggerName) {
		return findElement(By.cssSelector(".promotion-view-match-terms [data-id='" + triggerName + "'] .remove-word"));
	}

	// use PromotionsDetailPage.backButton()
	@Deprecated
	public WebElement backButton() {
		return findElement(By.xpath(".//a[text()[contains(., 'Back')]]"));
	}

	// use PromotionsDetailPage.promotionTitle()
	@Deprecated
	public String getPromotionTitle() {
		return findElement(By.cssSelector(".promotion-title-edit span")).getText();
	}

	// use PromotionsDetailPage.promotionTitle()
	@Deprecated
	public Editable name() {
		return new InlineEdit(findElement(By.className("promotion-title-edit")), getDriver());
	}

	// use PromotionsDetailPage.promotionTitle()
	@Deprecated
	public void createNewTitle(final String title) throws InterruptedException {
		name().setValueAndWait(title);
	}

	// use PromotionDetailsPage.getPromotionType()
	@Deprecated
	public String getPromotionType() {
		return findElement(By.cssSelector(".promotion-view-name")).getText();
	}

	// use PromotionDetailsPage.spotlightTypeDropdown()
	@Deprecated
	public void changeSpotlightType(final String promotionType) {
		// clear notifications
		new WebDriverWait(getDriver(), 5).until(GritterNotice.notificationsDisappear());
		findElement(By.cssSelector(".promotion-view-name-dropdown .clickable")).click();
		findElement(By.cssSelector(".promotion-view-name-dropdown [data-spotlight-type='" + promotionType + "']")).click();
		new WebDriverWait(getDriver(),3).until(GritterNotice.notificationAppears());
	}

	// use PromotionsDetailPage.promotedDocument(...)
	@Deprecated
	public WebElement promotedDocument(final String title) {
		return findElement(By.xpath(".//ul[contains(@class, 'promoted-documents-list')]")).findElement(By.xpath(".//a[contains(text(), '" + title + "')]/../.."));
	}

	// use PromotionsDetailPage.removablePromotedDocument(...)
	@Deprecated
	public void deleteDocument(final String title) {
		promotedDocument(title).findElement(By.cssSelector(".remove-document-reference.clickable")).click();
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".search-result-item .fa-spin")));
	}

	@Deprecated
	public WebElement addMorePromotedItemsButton() {
		return findElement(By.xpath(".//a[text()[contains(., 'Add More')]]"));

	}

	@Deprecated
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

	@Deprecated
	public Editable queryText() {
		return new InlineEdit(findElement(By.className("promotion-query-edit")), getDriver());
	}

	@Deprecated
	public String getQueryText() {
		return queryText().getValue();
	}

	@Deprecated
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
