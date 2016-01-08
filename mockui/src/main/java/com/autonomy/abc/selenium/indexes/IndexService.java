package com.autonomy.abc.selenium.indexes;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.page.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class IndexService {
    private Application application;
    private HSOElementFactory elementFactory;
    private IndexesPage indexesPage;

    public IndexService(Application application, HSOElementFactory elementFactory) {
        this.application = application;
        this.elementFactory = elementFactory;
    }

    protected WebDriver getDriver() {
        return elementFactory.getDriver();
    }

    protected AppBody getBody() {
        return application.createAppBody(getDriver());
    }

    public IndexesPage goToIndexes() {
        getBody().getSideNavBar().switchPage(NavBarTabId.INDEXES);
        indexesPage = elementFactory.getIndexesPage();
        return indexesPage;
    }

    public IndexesDetailPage goToDetails(Index index) {
        goToIndexes();

        indexesPage.findIndex(index.getName()).click();
        return elementFactory.getIndexesDetailPage();
    }

    public IndexesPage setUpIndex(Index index) {
        goToIndexes();
        indexesPage.newIndexButton().click();
        CreateNewIndexPage newIndexPage = elementFactory.getCreateNewIndexPage();

        String indexName = index.getName();
        String displayName = index.getDisplayName();

        newIndexPage.indexNameInput().setValue(indexName);
        if(!displayName.equals(indexName)) {
            newIndexPage.displayNameInput().setValue(displayName);
        }
        newIndexPage.nextButton().click();
        Waits.loadOrFadeWait();

        newIndexPage.setIndexFields(index.getIndexFields());
        newIndexPage.setParametricFields(index.getParametricFields());
        newIndexPage.nextButton().click();
        Waits.loadOrFadeWait();

        newIndexPage.finishButton().click();
        new WebDriverWait(getDriver(), 30).until(GritterNotice.notificationContaining(index.getCreateNotification()));

        indexesPage = elementFactory.getIndexesPage();
        return indexesPage;
    }

    public IndexesPage deleteIndex(Index index) {
        goToIndexes();

        indexesPage.deleteIndex(index.getName());

        new WebDriverWait(getDriver(),30).until(GritterNotice.notificationContaining("Index " + index.getName() + " successfully deleted"));

        return indexesPage;
    }

    public IndexesPage deleteAllIndexes() {
        goToIndexes();

        for(WebElement trashCan : indexesPage.findElements(By.className("fa-trash-o"))){
            if(ElementUtil.ancestor(trashCan, 1).isEnabled()){
                trashCan.click();
                Waits.loadOrFadeWait();
                getDriver().findElement(By.cssSelector(".modal-footer [type=submit]")).click();
                new WebDriverWait(getDriver(),30).until(GritterNotice.notificationContaining("successfully deleted"));
            }
        }

        return indexesPage;
    }

    public void deleteIndexViaAPICalls(Index index) {
        //TODO prod apikey, find a nice way y'know
        String apiKey = "9711e4b0-ec2c-409c-908b-d5e8ed20ebec";
        String url = "https://api.int.havenondemand.com/1/api/sync/deletetextindex/v1?index=" + index.getName() + "&";

        ((JavascriptExecutor) getDriver()).executeScript("window.open('your url','_blank');");

        List<String> windowHandles = new ArrayList<>(getDriver().getWindowHandles());
        getDriver().switchTo().window(windowHandles.get(1));

        getDriver().get(url + "apikey=" + apiKey);

        String confirm = getDriver().findElement(By.tagName("pre")).getText().split("\"")[5];

        getDriver().get(url + "confirm=" + confirm + "&apikey=" + apiKey);

        getDriver().close();
        getDriver().switchTo().window(windowHandles.get(0));
    }
}
