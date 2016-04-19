package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.search.SearchResult;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class FindSearchResult extends SearchResult {
    FindSearchResult(WebElement result, WebDriver driver){
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

    public WebElement similarDocuments() {
        return findElement(By.className("similar-documents-trigger"));
    }

    private WebElement previewButton(){
        return findElement(By.className("preview-documents-trigger"));
    }

    public DocumentViewer openDocumentPreview(){
        previewButton().click();
        return DocumentViewer.make(getDriver());
    }
}
