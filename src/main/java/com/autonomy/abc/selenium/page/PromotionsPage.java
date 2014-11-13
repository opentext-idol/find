package com.autonomy.abc.selenium.page;


import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.MainTabBar;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.util.AbstractMainPagePlaceholder;
import com.autonomy.idoladmin.selenium.util.AbstractWebElementPlaceholder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PromotionsPage extends AppElement implements AppPage {

	public PromotionsPage(final MainTabBar mainTabBar, final WebElement $el) {
		super($el, mainTabBar.getDriver());
	}

	@Override
	public void navigateToPage() { getDriver().get("promotions"); }

	public WebElement newPromotionButton() {
		return findElement(By.cssSelector("[data-route='promotions/new']"));
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
