package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.WebDriver;

public class DetailedPreviewPage extends AppElement implements AppPage {

    protected DetailedPreviewPage(WebDriver driver){
        super(new WebDriverWait(driver,30).until(ExpectedConditions.visibilityOfElementLocated(By.className("container-fluid"))),driver);
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.className("container-fluid")));
    }

    //navigation
    public void openOriginalDoc(){findElement(By.className("document-detail-open-original-link")).click();}

    public void goBackToSearch(){findElement(By.className("detail-view-back-button")).click();}

    //elements
    public WebElement serverLoadingIndicator(){
        return findElement(By.className("view-server-loading-indicator"));
    }

    public WebElement loadingIndicator(){
        return findElement(By.className("loading-spinner"));
    }

    public WebElement frame(){return findElement(By.tagName("iframe"));}

    public WebElement similarDatesTab(){
        return findElement(By.xpath("//span[contains(text(),'Similar dates')]"));
    }

    public WebElement similarDocsTab(){
        return findElement(By.xpath("//span[contains(text(),'Similar documents')]"));
    }

    public WebElement ithTick(int i){
        return findElement(By.xpath("//div[contains(class(),'before-slider-div')]/../div[class='slider-track']/div[position()='"+i+"']"));
    }

    //metadata
    public String getReference(){ return getField("Reference");}
    public String getIndex(){ return getField("Index");}
    public String getTitle(){ return getField("Title");}
    public String getSummary(){ return getField("Summary");}
    public String getDate(){ return getField("Date");}

    public String getField(String name) {
        try {
            return findElement(By.xpath(".//td[contains(text(), '" + name + "')]/following::td")).getText();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public static class Factory implements ParametrizedFactory<WebDriver, DetailedPreviewPage> {
        public DetailedPreviewPage create(WebDriver context) {
            return new DetailedPreviewPage(context);
        }
    }
}
