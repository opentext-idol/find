package com.autonomy.abc.selenium.find.preview;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.WebDriver;

public class DetailedPreviewPage extends AppElement implements AppPage {

    private DetailedPreviewPage(final WebDriver driver){
        super(new WebDriverWait(driver,30).until(ExpectedConditions.visibilityOfElementLocated(By.className("container-fluid"))),driver);
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.className("container-fluid")));
    }

    //navigation
    public void openOriginalDoc(){
        findElement(By.className("document-detail-open-original-link")).click();
        Waits.loadOrFadeWait();
    }

    public void goBackToSearch(){findElement(By.className("detail-view-back-button")).click();}

    //elements
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

    public WebElement ithTick(final int i){
        final String tickPercent = String.valueOf((i-1)*10);
        return findElement(By.xpath("//div[contains(@class,'slider-tick') and contains(@style,'left: "+tickPercent+"%')]"));
    }

    public String getSimilarDatesSummary(){
        return findElement(By.className("similar-dates-summary")).getText();
    }

    public int numberOfHeadersWithDocTitle(){
        final String title = getTitle();
        return findElements(By.xpath("//h1[contains(text(),'"+title+"')]")).size();
    }

    //metadata
    public String getReference(){ return getField("Reference");}
    public String getIndex() { return getField("Index");}
    public String getDatabase(){return getField("Database");}
    public String getTitle(){ return getField("Title");}
    public String getSummary(){ return getField("Summary");}
    public String getDate(){ return getField("Date");}

    private String getField(final String name) {
        try {
            return findElement(By.xpath(".//td[contains(text(), '" + name + "')]/following::td")).getText();
        } catch (final NoSuchElementException e) {
            return null;
        }
    }

    public static class Factory implements ParametrizedFactory<WebDriver, DetailedPreviewPage> {
        @Override
        public DetailedPreviewPage create(final WebDriver context) {
            return new DetailedPreviewPage(context);
        }
    }
}
