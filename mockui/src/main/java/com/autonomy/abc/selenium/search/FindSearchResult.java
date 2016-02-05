package com.autonomy.abc.selenium.search;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class FindSearchResult extends SearchResult {
    public FindSearchResult(WebElement result){
        super(result);
    }

    @Override
    public WebElement title() {
        return findElement(By.tagName("h4"));
    }

    @Override
    public WebElement getIcon() {
        return findElement(By.cssSelector(".content-type i"));
    }

    public String getReference() {
        return findElement(By.className("document-reference")).getText();
    }

    public WebElement similarDocuments() {
        return findElement(By.className("similar-documents-trigger"));
    }
}
