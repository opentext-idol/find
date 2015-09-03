package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import org.openqa.selenium.WebDriver;

public abstract class PageFactory {

    private final WebDriver driver;
    private final ApplicationType applicationType;

    public PageFactory(WebDriver driver,ApplicationType at){
        this.driver = driver;
        this.applicationType = at;
    }

    public abstract PromotionsPage getPromotionsPage();

    public WebDriver getDriver() {
        return driver;
    }
}
