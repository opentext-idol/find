package com.autonomy.abc.selenium.indexes;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.page.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

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

        newIndexPage.inputIndexName(index.getName());
        newIndexPage.nextButton().click();
        newIndexPage.loadOrFadeWait();

        newIndexPage.inputIndexFields(index.getIndexFields());
        newIndexPage.inputParametricFields(index.getParametricFields());
        newIndexPage.nextButton().click();
        newIndexPage.loadOrFadeWait();

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
        for(WebElement index : getDriver().findElements(By.className("listItemTitle"))){
            String indexName = index.getText().split("\\(")[0].trim();
            if(indexName.equals("default_index")){
                continue;
            }
            deleteIndex(new Index(indexName));
        }
        return indexesPage;
    }
}
