package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.menubar.TopNavBar;
import org.openqa.selenium.WebElement;

public class CreateNewDynamicPromotionsPage extends CreateNewPromotionsBase {

	public CreateNewDynamicPromotionsPage(final TopNavBar topNavBar, final WebElement $el) {
		super($el, topNavBar.getDriver());
	}

	@Override
	public void navigateToPage()  {
		getDriver().get("promotions/create-dynamic");
	}

	public void createDynamicPromotion(final String type, final String trigger) {
		spotlightType(type).click();
		continueButton("spotlightType").click();
		loadOrFadeWait();
		addSearchTrigger(trigger);
		finishButton().click();
		loadOrFadeWait();
	}

	public static class Placeholder {

		private final TopNavBar topNavBar;

		public Placeholder(final TopNavBar topNavBar) {
			this.topNavBar = topNavBar;
		}

		public CreateNewDynamicPromotionsPage $createNewDynamicPromotionsPage(final WebElement element) {
			return new CreateNewDynamicPromotionsPage(topNavBar, element);
		}
	}
}
