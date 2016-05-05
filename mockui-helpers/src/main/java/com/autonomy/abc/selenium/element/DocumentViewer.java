package com.autonomy.abc.selenium.element;

import com.autonomy.abc.selenium.indexes.Index;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DocumentViewer extends AppElement implements AppPage {

    protected DocumentViewer(WebDriver driver, WebElement element){
        super(element,driver);
    }

    private DocumentViewer(WebDriver driver) {
        super(driver.findElement(By.id("colorbox")), driver);
    }

    public static DocumentViewer make(WebDriver driver) {
//        new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.id("cboxClose")));
        DocumentViewer documentViewer = new DocumentViewer(driver);
        documentViewer.waitForLoad();
        return documentViewer;
    }

    private WebElement closeButton() {
        return findElement(By.id("cboxClose"));
    }

    public void close() {
        closeButton().click();
        Waits.loadOrFadeWait();
    }

    /* use this only to check that button is displayed - click by using previous() */
    public WebElement prevButton() {
        return findElement(By.cssSelector("#cboxPrevious, .prevBtn"));
    }

    public void previous() {
        prevButton().click();
        waitForLoad();
    }

    /* use this only to check that button is displayed - click by using next() */
    public WebElement nextButton() {
        return findElement(By.cssSelector("#cboxNext, .nextBtn"));
    }

    public void next() {
        nextButton().click();
        waitForLoad();
    }

    public WebElement frame() {
        waitForDocumentLoad();
        return findElement(By.tagName("iframe"));
    }

    public String getField(String name) {
        try {
            return findElement(By.xpath(".//th[contains(text(), '" + name + "')]/../td")).getText();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public Index getIndex() {
        return new Index(getField("Index"));
    }

    public String getReference() {
        return getField("Reference");
    }

    public String getAuthor(){
        return getField("Author");
    }

    public String getContentType() {
        return getField("Content Type");
    }

    public int getCurrentDocumentNumber() {
        String[] current = findElement(By.id("cboxCurrent")).getText().split(" ");
        return Integer.parseInt(current[current.length - 3]);
    }

    public int getTotalDocumentsNumber() {
        String[] current = findElement(By.id("cboxCurrent")).getText().split(" ");
        return Integer.parseInt(current[current.length - 1]);
    }

    @Override
    public void waitForLoad() {
        Waits.loadOrFadeWait();
    }

    private void waitForDocumentLoad() {
        new WebDriverWait(getDriver(), 30).until(ExpectedConditions.invisibilityOfElementLocated(By.className("view-server-loading-indicator")));
    }

    public void openInNewTab() {
        findElement(By.xpath("//*[text()='Open in New Tab']")).click();
    }
}
