package com.autonomy.abc.selenium.page;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AngularWizard extends AppElement implements AppPage {
    public AngularWizard(WebDriver driver) {
        super(new WebDriverWait(driver,30).until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper-content"))), driver);
    }

    @Override
    public void waitForLoad() {

    }

    public WebElement nextButton(){
        return findElement(By.xpath("//a[text()[contains(.,'Next')]]"));
    }

    public WebElement prevButton(){
        return findElement(By.xpath("//a[text()[contains(.,'Previous')]]"));
    }

    public WebElement cancelButton(){
        return findElement(By.xpath("//a[text()[contains(.,'Cancel')]]"));
    }

    public WebElement finishButton(){
        return findElement(By.xpath("//a[text()[contains(.,'Finish')]]"));
    }
}
