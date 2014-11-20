package com.autonomy.abc.selenium.page;


import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.element.ModalView;
import com.autonomy.abc.selenium.menubar.MainTabBar;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.util.AbstractMainPagePlaceholder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PromotionsPage extends AppElement implements AppPage {

	public PromotionsPage(final MainTabBar mainTabBar, final WebElement $el) {
		super($el, mainTabBar.getDriver());
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
		if (getDriver().getCurrentUrl().contains("promotions/detail")) {
			findElement(By.cssSelector(".btn[data-route='promotions']")).click();
		}

		final MainTabBar mainTabBar = new MainTabBar(getDriver());
		mainTabBar.promotionsTab().click();

		for (final WebElement promotion : promotionsList()) {
			promotion.click();
			deletePromotion();
			modalLoadOrFadeWait();
		}
	}

	public static class Placeholder extends AbstractMainPagePlaceholder<PromotionsPage> {

		public Placeholder(final AppBody body, final MainTabBar mainTabBar, final TopNavBar topNavBar) {
			super(body, mainTabBar, topNavBar, "promotions", "promotions", false);
		}

		@Override
		protected PromotionsPage convertToActualType(final WebElement element) {
			return new PromotionsPage(tabBar, element);
		}

	}
}
