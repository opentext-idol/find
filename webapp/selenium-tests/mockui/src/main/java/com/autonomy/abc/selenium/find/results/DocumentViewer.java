package com.autonomy.abc.selenium.find.results;

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
    private static final int DOCUMENT_LOAD_TIMEOUT = 30;

    protected DocumentViewer(final WebDriver driver, final WebElement element){
        super(element,driver);
    }

    private WebElement closeButton() {
        return findElement(By.id("cboxClose"));
    }

    public void close() {
        closeButton().click();
        Waits.loadOrFadeWait();
    }

    public WebElement frame() {
        waitForDocumentLoad();
        return findElement(By.tagName("iframe"));
    }

    private String getField(final String name) {
        try {
            return findElement(By.xpath(".//th[contains(text(), '" + name + "')]/../td")).getText();
        } catch (final NoSuchElementException ignored) {
            return null;
        }
    }

    public Index getIndex() {
        return new Index(getField("Index"));
    }

    public String getIndexName() {
        return findElement(By.className("preview-mode-document-database")).getText();
    }

    public String getReference() {
        return findElement(By.className("preview-mode-document-url")).getText();
    }

    @Override
    public void waitForLoad() {
        Waits.loadOrFadeWait();
    }

    private void waitForDocumentLoad() {
        new WebDriverWait(getDriver(), DOCUMENT_LOAD_TIMEOUT).until(ExpectedConditions.invisibilityOfElementLocated(By.className("view-server-loading-indicator")));
    }

    public boolean previewPresent(){
        return  findElement(By.id("colorbox")).isDisplayed();
    }
}
