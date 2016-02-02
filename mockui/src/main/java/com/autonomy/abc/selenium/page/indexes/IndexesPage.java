package com.autonomy.abc.selenium.page.indexes;

import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
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

    @Deprecated
    public static IndexesPage make(WebDriver driver) {
        return new IndexesPage.Factory().create(driver);
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

    private WebElement indexRow(String displayName){
        return ElementUtil.ancestor(findIndex(displayName), 9);
    }

    public void deleteIndex(String displayName){
        indexRow(displayName).findElement(By.tagName("button")).click();
        Waits.loadOrFadeWait();
        modalClick();
        Waits.loadOrFadeWait();
    }

    private void modalClick() {
        getDriver().findElement(By.cssSelector(".modal-footer [type=submit]")).click();
    }

    public List<WebElement> indexes() {
        return findElements(By.xpath("//*[contains(@ng-repeat,'index')]"));
    }

    public List<String> getIndexDisplayNames(){
        List<String> names = new ArrayList<>();

        for(WebElement index : indexes()){
            names.add(index.findElement(By.className("listItemTitle")).getText().split("\\(")[0].trim());
        }

        return names;
    }

    public int getNumberOfConnections(Index index) {
        return Integer.parseInt(indexRow(index.getDisplayName()).findElement(By.cssSelector(".listItemNormalText>.ng-scope")).getText().split(" ")[1]);
    }

    public static class Factory implements ParametrizedFactory<WebDriver, IndexesPage> {
        @Override
        public IndexesPage create(WebDriver context) {
            IndexesPage.waitForLoad(context);
            return new IndexesPage(context);
        }
    }
}
