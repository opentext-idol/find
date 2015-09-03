package com.autonomy.abc.selenium.page.promotions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class CreateNewDynamicPromotionsPage extends CreateNewPromotionsBase {

	public CreateNewDynamicPromotionsPage(final WebDriver driver) {
		super(driver);
	}

	public abstract void createDynamicPromotion(final String type, final String trigger);

	public WebElement dial() {
		return findElement(By.cssSelector(".dial"));
	}

}
