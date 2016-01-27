package com.autonomy.abc.selenium.search;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class FindSearchResult extends SearchResult {
    private final WebElement similarDocuments;
    private final String reference;

    public FindSearchResult(WebElement result){
        this.result = result;

        title = result.findElement(By.tagName("h4"));
        description = result.findElement(By.className("result-summary")).getText();
        icon = result.findElement(By.cssSelector(".content-type i"));

        similarDocuments = result.findElement(By.className("similar-documents-trigger"));
        reference = result.findElement(By.className("document-reference")).getText();
    }

    public String getReference() {
        return reference;
    }

    public WebElement getSimilarDocuments() {
        return similarDocuments;
    }
}
