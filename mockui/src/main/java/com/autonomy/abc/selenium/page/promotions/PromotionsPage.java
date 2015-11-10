package com.autonomy.abc.selenium.page.promotions;


import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PromotionsPage extends AppElement implements AppPage {

	public PromotionsPage(WebDriver driver) {
		super(new WebDriverWait(driver,30).until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper-content"))),driver);
		waitForLoad();
	}

	public WebElement promoteExistingButton() {
		return findElement(By.linkText("PROMOTE DOCUMENTS"));
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

	@Deprecated
	public void deletePromotion(String promotionContains){
		deletePromotion(getPromotionLinkWithTitleContaining(promotionContains));
	}

	@Deprecated
	private void deletePromotion(WebElement promotion){
		WebElement deleteButton = promotion.findElement(By.className("promotion-delete"));
		deleteButton.click();
		loadOrFadeWait();
		modalClick();
		loadOrFadeWait();
		new WebDriverWait(getDriver(),20).until(ExpectedConditions.stalenessOf(deleteButton));
	}

	private void modalClick() {
		getDriver().findElement(By.className("modal-action-button")).click();
	}

	public List<WebElement> promotionsList() {
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.invisibilityOfElementLocated(By.className("loading-indicator")));
		if (getText().contains("There are no promotions")) {
			return Collections.emptyList();
		}
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
			deletePromotion(promotion);
		}

		new WebDriverWait(getDriver(),Math.max(promotions.size() * 10,30)).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return findElements(By.className("promotion-delete")).size() == 0;
			}
		});
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

	@Override
	public void waitForLoad() {
		new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[text()='Promote documents']")));

		new WebDriverWait(getDriver(),30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				for (WebElement indicator : driver.findElements(By.className("loading-indicator"))) {
					if (indicator.isDisplayed()) {
						return false;
					}
				}

				return true;
			}
		});
	}
}
