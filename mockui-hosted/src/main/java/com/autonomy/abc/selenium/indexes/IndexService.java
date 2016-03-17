package com.autonomy.abc.selenium.indexes;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.hsod.HSODElementFactory;
import com.autonomy.abc.selenium.users.HSODUser;
import com.autonomy.abc.selenium.users.User;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class IndexService extends ServiceBase<HSODElementFactory> {
    private IndexesPage indexesPage;

    public IndexService(SearchOptimizerApplication<? extends HSODElementFactory> application) {
        super(application);
    }

    public IndexesPage goToIndexes() {
        indexesPage = getApplication().switchTo(IndexesPage.class);
        return indexesPage;
    }

    public IndexesDetailPage goToDetails(Index index) {
        goToIndexes();

        indexesPage.findIndex(index.getName()).click();
        return getElementFactory().getIndexesDetailPage();
    }

    public CreateNewIndexPage goToIndexWizard() {
        goToIndexes();
        indexesPage.newIndexButton().click();
        return getElementFactory().getCreateNewIndexPage();
    }

    public IndexesPage setUpIndex(Index index) {
        new IndexWizard(index, goToIndexWizard()).apply();
        new WebDriverWait(getDriver(), 30).until(GritterNotice.notificationContaining(index.getCreateNotification()));
        indexesPage = getElementFactory().getIndexesPage();
        return indexesPage;
    }

    public IndexesPage deleteIndex(Index index) {
        goToIndexes();

        indexesPage.deleteIndex(index.getDisplayName());
        waitForIndexDeletion(index);

        return indexesPage;
    }

    public IndexesPage deleteAllIndexes() {
        goToIndexes();

        for(String index : indexesPage.getIndexDisplayNames()){
            if(!index.equals(Index.DEFAULT.getDisplayName())) {
                try {
                    indexesPage.deleteIndex(index);
                    waitForIndexDeletion(new Index(null, index));
                } catch (WebDriverException e) {
                    LoggerFactory.getLogger(IndexService.class).error("Could not delete index '" + index + "' because of a " + e.getClass().getSimpleName());
                }
            }
        }

        return indexesPage;
    }

    private void waitForIndexDeletion(Index index) {
        new WebDriverWait(getDriver(),30).until(GritterNotice.notificationContaining(index.getDeleteNotification()));
    }

    public void deleteIndexViaAPICalls(Index index, User user, String apiUrl) {
        String apiKey = ((HSODUser) user).getApiKey();

        apiUrl += "/1/api/sync/deletetextindex/v1?index=" + index.getName() + "&";

        ((JavascriptExecutor) getDriver()).executeScript("window.open('your url','_blank');");
        List<String> windowHandles = new ArrayList<>(getDriver().getWindowHandles());

        try {
            getDriver().switchTo().window(windowHandles.get(1));
            getDriver().get(apiUrl + "apikey=" + apiKey);

            String confirm = getDriver().findElement(By.tagName("pre")).getText().split("\"")[5];

            getDriver().get(apiUrl + "confirm=" + confirm + "&apikey=" + apiKey);
        } finally {
            getDriver().close();
            getDriver().switchTo().window(windowHandles.get(0));
        }
    }
}
