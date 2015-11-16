package com.autonomy.abc.selenium.page.indexes;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class IndexesPage extends AppElement implements AppPage {

    public IndexesPage(WebDriver driver) {
        super(new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper"))), driver);
        waitForLoad();
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(),30)
                .withMessage("Index page failed to load")
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper-content")));
        new WebDriverWait(getDriver(),30)
                .withMessage("Indexes failed to load")
                .until(ExpectedConditions.invisibilityOfElementLocated(By.className("loadingIcon")));
    }

    public WebElement newIndexButton(){
        return findElement(By.id("new-index-btn"));
    }

    public WebElement findIndex(String indexName) {
        return findElement(By.id(indexName));
    }

    public void deleteIndex(String indexName){
        findIndex(indexName).findElement(By.cssSelector(".delete-action-button-container button")).click();
        loadOrFadeWait();
        modalClick();
        loadOrFadeWait();
    }

    private void modalClick() {
        getDriver().findElement(By.cssSelector(".modal-footer [type=submit]"));
    }

    public List<WebElement> getIndexes() {
        return findElements(By.xpath("//*[contains(@ng-repeat,'index')]"));
    }

    public List<String> getIndexNames(){
        List<String> names = new ArrayList<>();

        for(WebElement index : getIndexes()){
            names.add(index.findElement(By.className("listItemTitle")).getText().split("\\(")[0].trim());
        }

        return names;
    }
}
