package com.autonomy.abc.selenium.page;


import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.element.ModalView;
import com.autonomy.abc.selenium.menubar.SideNavBar;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.util.AbstractMainPagePlaceholder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
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

	public void openPromotionWithTitleContaining(final String promotionTitleSubstring) {
		findElement(By.xpath(".//h3/a[contains(text(), '" + promotionTitleSubstring + "')]")).click();
	}

	public void deletePromotion() {
		findElement(By.cssSelector(".promotion-view-delete")).click();
		final ModalView deleteModal = ModalView.getVisibleModalView(getDriver());
		deleteModal.findElement(By.cssSelector(".btn-danger")).click();
	}

	public WebElement spotlightButton() {
		return findElement(By.cssSelector(".promotion-view-name-dropdown button"));
	}

	public List<WebElement> promotionsList() {
		return findElements(By.cssSelector(".promotion-list-container .ibox-content a"));
	}

	public void deleteAllPromotions() {
		new SideNavBar(getDriver()).getTab("promotions").click();

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

		for (final WebElement trigger : findElements(By.cssSelector(".promotion-view-match-terms .clickable-label"))) {
			searchTriggerList.add(trigger.getAttribute("data-id"));
		}

		return searchTriggerList;
	}

	public void addSearchTrigger(final String searchTrigger) {
		findElement(By.cssSelector(".edit-promotion-match-terms input")).clear();
		findElement(By.cssSelector(".edit-promotion-match-terms input")).sendKeys(searchTrigger);
		waitUntilClickableThenClick(triggerAddButton());
		loadOrFadeWait();
	}

	public List <String> getPromotedList() {
		final List <String> docTitles = new ArrayList<>();
		for (final WebElement docTitle : findElements(By.cssSelector(".promoted-documents-list h3"))) {
			docTitles.add(docTitle.getText());
		}
		return docTitles;
	}

	public WebElement triggerAddButton() {
		return findElement(By.cssSelector(".edit-promotion-match-terms [type='submit']"));
	}

	public void removeSearchTrigger(final String searchTrigger) {
		loadOrFadeWait();
		waitUntilClickableThenClick(By.cssSelector("[data-id='" + searchTrigger + "'] .remove-match-term"));
		loadOrFadeWait();
	}

	public WebElement clickableSearchTrigger(final String triggerName) {
		return findElement(By.cssSelector(".promotion-view-match-terms [data-id='" + triggerName + "'] a"));
	}

	public WebElement triggerRemoveX(final String triggerName) {
		return findElement(By.cssSelector(".promotion-view-match-terms [data-id='" + triggerName + "'] .remove-match-term"));
	}

	public WebElement backButton() {
		return findElement(By.cssSelector("[data-route='promotions']"));
	}

	public String getPromotionTitle() {
		return findElement(By.cssSelector(".promotion-view-title")).getText();
	}

	public void createNewTitle(final String title) {
		final WebElement titleElement = findElement(By.cssSelector(".promotion-view-rename-form input"));
		titleElement.clear();
		titleElement.sendKeys(title);
		findElement(By.cssSelector(".promotion-view-rename-form [type='submit']")).click();
	}

	public String getPromotionType() {
		return findElement(By.cssSelector(".promotion-view-name")).getText();
	}

	public void changeSpotlightType(final String promotionType) {
		findElement(By.cssSelector(".promotion-view-name-dropdown .dropdown-toggle")).click();
		findElement(By.cssSelector(".promotion-view-name-dropdown [data-spotlight-type='" + promotionType + "']")).click();
		new WebDriverWait(getDriver(),3).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".promotion-view-name-dropdown .fa-pencil")));
	}

	public static class Placeholder extends AbstractMainPagePlaceholder<PromotionsPage> {

		public Placeholder(final AppBody body, final SideNavBar sideNavBar, final TopNavBar topNavBar) {
			super(body, sideNavBar, topNavBar, "promotions", "promotions", false);
		}

		@Override
		protected PromotionsPage convertToActualType(final WebElement element) {
			return new PromotionsPage(navBar, element);
		}

	}
}
