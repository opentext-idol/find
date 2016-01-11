package com.autonomy.abc.selenium.page.indexes;

import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
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

    private IndexesPage(WebDriver driver) {
        super(driver.findElement(By.className("wrapper-content")), driver);
    }

    public static IndexesPage make(WebDriver driver) {
        IndexesPage.waitForLoad(driver);
        return new IndexesPage(driver);
    }

    private static void waitForLoad(WebDriver driver) {
        new WebDriverWait(driver, 30)
                .withMessage("Indexes failed to load")
                .until(ExpectedConditions.invisibilityOfElementLocated(By.className("loadingIcon")));
    }

    @Override
    public void waitForLoad() {
        waitForLoad(getDriver());
    }

    public WebElement newIndexButton(){
        return getDriver().findElement(By.id("new-index-btn"));
    }

    public WebElement findIndex(String displayName) {
            return findElement(By.xpath(".//*[contains(text(),'" + displayName + "')]"));
    }

    public void deleteIndex(String displayName){
        ElementUtil.ancestor(findIndex(displayName), 9).findElement(By.tagName("button")).click();
        Waits.loadOrFadeWait();
        modalClick();
        Waits.loadOrFadeWait();
    }

    private void modalClick() {
        getDriver().findElement(By.cssSelector(".modal-footer [type=submit]")).click();
    }

    public List<WebElement> getIndexes() {
        return findElements(By.xpath("//*[contains(@ng-repeat,'index')]"));
    }

    public List<String> getIndexDisplayNames(){
        List<String> names = new ArrayList<>();

        for(WebElement index : getIndexes()){
            names.add(index.findElement(By.className("listItemTitle")).getText().split("\\(")[0].trim());
        }

        return names;
    }

    public int getNumberOfConnections(Index index) {
        return Integer.parseInt(findIndex(index.getName()).findElement(By.cssSelector(".listItemNormalText>.ng-scope")).getText().split(" ")[1]);
    }
}
