package com.autonomy.abc.selenium.find.preview;

import com.autonomy.abc.selenium.element.DocumentViewer;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class InlinePreview extends DocumentViewer {
    private static final By LOCATOR = By.cssSelector(".preview-mode-wrapper:not(.hide) .preview-mode-container");
    private WebElement preview;

    private InlinePreview(final WebDriver driver){
        super(driver, driver.findElement(LOCATOR));
        preview = driver.findElement(LOCATOR);
    }

    public static InlinePreview make(final WebDriver driver){
        new WebDriverWait(driver, 10)
                .withMessage("Preview did not open")
                .until(ExpectedConditions.visibilityOfElementLocated(LOCATOR));
        final InlinePreview docPreviewer = new InlinePreview(driver);
        docPreviewer.waitForLoad();
        return docPreviewer;
    }

    private WebElement closeButton(){
        return findElement(By.tagName("i"));
    }

    @Override
    public void close(){
        closeButton().click();
        Waits.loadOrFadeWait();
    }

    public DetailedPreviewPage openPreview(){
        findElement(By.className("preview-mode-open-detail-button")).click();
        return new DetailedPreviewPage.Factory().create(getDriver());
    }

    public boolean loadingIndicatorExists() {
        return !findElements(By.className("view-server-loading-indicator")).isEmpty();
    }

    public WebElement loadingIndicator(){
        return findElement(By.className("view-server-loading-indicator"));
    }

    @Override
    public String getReference() {
        if (getField("URL") == null) {
            return getField("Reference");
        }
        return getField("URL");
    }

    @Override
    public void next(){ throw new UnsupportedOperationException("Idol-Find DocPreview has no 'next'");}

    @Override
    public void previous(){throw new UnsupportedOperationException("Idol-Find DocPreview has no 'previous'");}

    @Override
    public int getTotalDocumentsNumber() {throw new UnsupportedOperationException("Idol-Find DocPreview doesn't have number of docs");}

    @Override
    public boolean previewPresent(){return !findElements(By.className("preview-mode-document")).isEmpty();}

    public boolean docFillsMoreThanHalfOfPreview () {
        final int frameHeight = frame().getSize().height;
        final int previewHeight = preview.getSize().height;

        return previewHeight > frameHeight/2;
    }
}
