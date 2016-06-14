package com.autonomy.abc.selenium.find.results;

import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.find.preview.DocumentPreviewer;
import com.autonomy.abc.selenium.query.QueryResult;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class FindResult extends QueryResult {
    FindResult(final WebElement result, final WebDriver driver){
        super(result, driver);
    }

    @Override
    public WebElement title() {
        return findElement(By.tagName("h4"));
    }

    @Override
    public WebElement icon() {
        return findElement(By.cssSelector(".content-type i"));
    }

    public String getReference() {
        return findElement(By.className("document-reference")).getText();
    }

    public String getDate(){return findElement(By.className("document-date")).getText();}

    public WebElement similarDocuments() {
        return findElement(By.className("similar-documents-trigger"));
    }

    private WebElement previewButton(){
        return findElement(By.className("preview-documents-trigger"));
    }

    private Boolean previewButtonExists(){ return findElements(By.className("preview-documents-trigger")).size()>0;}

    public DocumentViewer openDocumentPreview(){
        if (previewButtonExists()){
            previewButton().click();
            return DocumentViewer.make(getDriver());
        }
        title().click();
        return DocumentPreviewer.make(getDriver());
    }
}
