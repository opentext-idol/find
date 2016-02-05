package com.autonomy.abc.selenium.search;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class FindSearchResult extends SearchResult {
    public FindSearchResult(WebElement result){
        super(result);
    }

    @Override
    public WebElement title() {
        return result.findElement(By.tagName("h4"));
    }

    @Override
    public WebElement getIcon() {
        return result.findElement(By.cssSelector(".content-type i"));
    }

    public String getReference() {
        return result.findElement(By.className("document-reference")).getText();
    }

    public WebElement similarDocuments() {
        return result.findElement(By.className("similar-documents-trigger"));
    }
}
