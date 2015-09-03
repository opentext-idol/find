package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import org.openqa.selenium.WebDriver;

public abstract class ElementFactory {

    private final WebDriver driver;

    protected ElementFactory(WebDriver driver){
        this.driver = driver;
    }

    public static ElementFactory from(ApplicationType applicationType, WebDriver driver){
        return (applicationType == applicationType.HOSTED) ? new HSOElementFactory(driver) : new OPElementFactory(driver);
    }

    public abstract PromotionsPage getPromotionsPage();

    public CreateNewPromotionsPage getCreateNewPromotionsPage(){
        return new CreateNewPromotionsPage(driver);
    }

    public WebDriver getDriver() {
        return driver;
    }
}
