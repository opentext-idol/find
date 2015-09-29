package com.autonomy.abc.selenium.page.search;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DocumentViewer extends AppElement implements AppPage {
    private DocumentViewer(WebDriver driver) {
        super(driver.findElement(By.id("colorbox")), driver);
    }

    public static DocumentViewer make(WebDriver driver) {
//        new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.id("cboxClose")));
        DocumentViewer documentViewer = new DocumentViewer(driver);
        documentViewer.loadOrFadeWait();
        documentViewer.waitForLoad();
        return documentViewer;
    }

    private WebElement closeButton() {
        return findElement(By.id("cboxClose"));
    }

    public void close() {
        closeButton().click();
        loadOrFadeWait();
    }

    private WebElement prevButton() {
        return findElement(By.id("cboxPrevious"));
    }

    public void previous() {
        prevButton().click();
        waitForLoad();
    }

    private WebElement nextButton() {
        return findElement(By.id("cboxNext"));
    }

    public void next() {
        nextButton().click();
        waitForLoad();
    }

    public WebElement frame() {
        return findElement(By.tagName("iframe"));
    }

    private String getRowText(int row) {
        return findElement(By.cssSelector("tr:nth-child(" + row + ") td.break-all")).getText();
    }

    public String getDomain() {
        return getRowText(1);
    }

    public String getIndex() {
        return getRowText(2);
    }

    public String getReference() {
        return getRowText(3);
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(), 30).until(ExpectedConditions.invisibilityOfElementLocated(By.className("view-server-loading-indicator")));
    }
}
