package com.autonomy.abc.selenium.page.indexes;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class IndexesPage extends AppElement implements AppPage {

    public IndexesPage(WebDriver driver) {
        super(new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper"))), driver);
        waitForLoad();
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper-content")));
        new WebDriverWait(getDriver(),30).until(ExpectedConditions.invisibilityOfElementLocated(By.className("loadingIcon")));
    }

    public WebElement newIndexButton(){
        return findElement(By.cssSelector(".affix-container div:not(.affix-clone) div #new-index-btn"));
    }

    public WebElement findIndex(String indexName) {
        return findElement(By.xpath("//div[contains(@class,'hpebox-content')]//*[text()[contains(.,'" + indexName + "')]]"));
    }

    public void deleteIndex(String indexName){
        findIndex(indexName).findElement(By.xpath(".//../../../..//button")).click();
        loadOrFadeWait();
        modalClick();
        loadOrFadeWait();
    }

    private void modalClick() {
        getDriver().findElement(By.className("modal-action-button")).click();
    }

    public List<WebElement> getIndexes() {
        return findElements(By.xpath("//*[contains(@ng-repeat,'index')]"));
    }
}
