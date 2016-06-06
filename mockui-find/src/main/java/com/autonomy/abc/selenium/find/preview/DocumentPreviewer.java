package com.autonomy.abc.selenium.find.preview;

import com.autonomy.abc.selenium.element.DocumentViewer;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * TODO: Is this needed any more? InlinePreview and DetailedPreviewPage
 * are both significantly different from the ISO preview...
 */
public class DocumentPreviewer extends DocumentViewer {

    private DocumentPreviewer(WebDriver driver){
        super(driver,driver.findElement(By.className("preview-mode-container")));
    }

    public static DocumentPreviewer make(WebDriver driver){
        DocumentPreviewer docPreviewer = new DocumentPreviewer(driver);
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

    @Override
    public void openInNewTab(){
        findElement(By.xpath("//button[text()='Open']")).click();
    }

    @Override
    public void next(){ throw new UnsupportedOperationException("Idol-Find DocPreview has no 'next'");}

    @Override
    public void previous(){throw new UnsupportedOperationException("Idol-Find DocPreview has no 'previous'");}

    @Override
    public int getTotalDocumentsNumber() {throw new UnsupportedOperationException("Idol-Find DocPreview doesn't have number of docs");}

    @Override
    public boolean previewPresent(){return findElements(By.className("preview-mode-contents")).size()>0;}

}
