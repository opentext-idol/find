package com.autonomy.abc.selenium.page.promotions;

import com.autonomy.abc.selenium.menubar.TopNavBar;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class CreateNewDynamicPromotionsPage extends CreateNewPromotionsBase {

	public CreateNewDynamicPromotionsPage(final TopNavBar topNavBar, final WebElement $el) {
		super($el, topNavBar.getDriver());
	}

	@Override
	public void navigateToPage()  {
		getDriver().get("promotions/create-dynamic");
	}

	public void createDynamicPromotion(final String type, final String trigger, final String applicationType) {
		spotlightType(type).click();
		continueButton(WizardStep.PROMOTION_TYPE).click();
		loadOrFadeWait();
		if (applicationType.equals("Hosted")) {
			continueButton(WizardStep.RESULTS).click();
			loadOrFadeWait();
		}
		addSearchTrigger(trigger);
		finishButton().click();
		loadOrFadeWait();
	}

	public WebElement dial() {
		return findElement(By.cssSelector(".dial"));
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
