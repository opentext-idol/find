package com.autonomy.abc.selenium.element;

import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

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


}
