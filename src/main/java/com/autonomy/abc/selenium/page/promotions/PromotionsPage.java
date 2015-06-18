package com.autonomy.abc.selenium.page.promotions;


import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.element.ModalView;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.SideNavBar;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.AppPage;
import com.autonomy.abc.selenium.util.AbstractMainPagePlaceholder;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PromotionsPage extends AppElement implements AppPage {

	public PromotionsPage(final SideNavBar sideNavBar, final WebElement $el) {
		super($el, sideNavBar.getDriver());
	}

	@Override
	public void navigateToPage() {
		getDriver().get("promotions");
	}

	public WebElement newPromotionButton() {
		return findElement(By.cssSelector("[data-route='promotions/new']"));
	}

	public WebElement getPromotionLinkWithTitleContaining(final String promotionTitleSubstring) {
		return findElement(By.xpath(".//h3[contains(text(), '" + promotionTitleSubstring.replace("\"", "").split("\\s+")[0] + "')]/../../.."));
	}

	public void deletePromotion() {
		final WebElement extraFunctionsDropdown = findElement(By.cssSelector(".extra-functions .dropdown-toggle"));
		new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOf(extraFunctionsDropdown));
		extraFunctionsDropdown.click();
		loadOrFadeWait();
		findElement(By.cssSelector(".promotion-view-delete")).click();
		loadOrFadeWait();
		final ModalView deleteModal = ModalView.getVisibleModalView(getDriver());
		deleteModal.findElement(By.cssSelector(".btn-danger")).click();
		loadOrFadeWait();
		new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOf(newPromotionButton()));
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
		return findElement(By.cssSelector(".promotion-view-name-dropdown button"));
	}

	public List<WebElement> promotionsList() {
		return findElements(By.cssSelector(".promotion-list-container li a"));
	}

	public void deleteAllPromotions() {
		new SideNavBar(getDriver()).getTab(NavBarTabId.PROMOTIONS).click();

		if (getDriver().getCurrentUrl().contains("promotions/detail")) {
			backButton().click();
		}

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
		for (final WebElement docTitle : findElements(By.cssSelector(".promoted-documents-list h3"))) {
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

	public void removeSearchTrigger(final String searchTrigger) throws InterruptedException {
		loadOrFadeWait();
		waitUntilClickableThenClick(triggerRemoveButton(searchTrigger));
		Thread.sleep(3000);
	}

	public WebElement clickableSearchTrigger(final String triggerName) {
		return findElement(By.cssSelector(".promotion-view-match-terms [data-id='" + triggerName + "']"));
	}

	public WebElement triggerRemoveButton(final String triggerName) {
		return findElement(By.cssSelector(".promotion-view-match-terms [data-id='" + triggerName + "'] .remove-word"));
	}

	public WebElement backButton() {
		return findElement(By.cssSelector(".btn[data-route='promotions']"));
	}

	public String getPromotionTitle() {
		return findElement(By.cssSelector(".promotion-title-edit span")).getText();
	}

	public void createNewTitle(final String title) throws InterruptedException {
		final WebElement pencil = findElement(By.cssSelector(".promotion-title-edit .fa-pencil"));
		getParent(pencil).click();
		final WebElement titleElement = findElement(By.cssSelector(".promotion-title-edit input"));
		titleElement.clear();
		titleElement.sendKeys(title);
		findElement(By.cssSelector(".promotion-title-edit [type='submit']")).click();
		Thread.sleep(3000);
	}

	public String getPromotionType() {
		return findElement(By.cssSelector(".promotion-view-name")).getText();
	}

	public void changeSpotlightType(final String promotionType) {
		findElement(By.cssSelector(".promotion-view-name-dropdown .dropdown-toggle")).click();
		findElement(By.cssSelector(".promotion-view-name-dropdown [data-spotlight-type='" + promotionType + "']")).click();
		new WebDriverWait(getDriver(),3).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".promotion-view-name-dropdown .fa-lightbulb-o")));
	}

	public WebElement promotedDocument(final String title) {
		return findElement(By.xpath(".//ul[contains(@class, 'promoted-documents-list')]")).findElement(By.xpath(".//a[contains(text(), '" + title + "')]/../.."));
	}

	public String promotedDocumentSummary(final String title) {
		return promotedDocument(title).findElement(By.cssSelector("p")).getText();
	}

	public void deleteDocument(final String title) {
		promotedDocument(title).findElement(By.cssSelector(".remove-document-reference")).click();
		new WebDriverWait(getDriver(), 3).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".remove-document-reference.fa-spin")));
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

	public String getQueryText() {
		return findElement(By.cssSelector(".promotion-query-edit span")).getText();
	}

	public void editQueryText(final String newQueryText) {
		getParent(findElement(By.cssSelector(".promotion-query-edit .fa-pencil"))).click();
		findElement(By.cssSelector("[placeholder='New query']")).clear();
		findElement(By.cssSelector("[placeholder='New query']")).sendKeys(newQueryText);
		findElement(By.cssSelector(".promotion-query-edit [type='submit']")).click();
		loadOrFadeWait();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".promotion-query-edit .fa-pencil")));
	}

	public WebElement fieldTextAddButton() {
		return findElement(By.cssSelector(".promotion-field-text")).findElement(By.xpath(".//button[contains(text(), 'Field Text')]"));
	}

	public WebElement fieldTextInputBox() {
		return findElement(By.cssSelector(".promotion-field-text [placeholder='Field text']"));
	}

	public WebElement fieldTextTickConfirmButton() {
		return getParent(findElement(By.cssSelector(".promotion-field-text .fa-check")));
	}

	public WebElement fieldTextRemoveButton() {
		return getParent(findElement(By.cssSelector(".promotion-field-text .fa-remove")));
	}

	public WebElement fieldTextEditButton() {
		return getParent(findElement(By.cssSelector(".promotion-field-text .fa-pencil")));
	}

	public String fieldTextValue() {
		return findElement(By.cssSelector(".promotion-field-text .inline-edit-current-value")).getText();
	}

	public void addFieldText(final String fieldText) {
		fieldTextAddButton().click();
		loadOrFadeWait();
		fieldTextInputBox().sendKeys(fieldText);
		fieldTextTickConfirmButton().click();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(fieldTextRemoveButton()));
	}

	public static class Placeholder extends AbstractMainPagePlaceholder<PromotionsPage> {

		public Placeholder(final AppBody body, final SideNavBar sideNavBar, final TopNavBar topNavBar) {
			super(body, sideNavBar, topNavBar, "promotions", NavBarTabId.PROMOTIONS, false);
		}

		@Override
		protected PromotionsPage convertToActualType(final WebElement element) {
			return new PromotionsPage(navBar, element);
		}

	}
}
