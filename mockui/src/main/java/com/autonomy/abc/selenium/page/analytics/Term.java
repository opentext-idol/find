package com.autonomy.abc.selenium.page.analytics;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class Term {

    private String term;
    private int searches;
    private WebElement element;

    public Term(String term, int searches){
        this.searches = searches;
        this.term = term;
        this.element = null;
    }

    public Term(WebElement element){
        this.element = element;
        this.term = element.findElement(By.tagName("a")).getText();
        this.searches = Integer.parseInt(element.findElement(By.tagName("span")).getText());
    }

    public String getTerm() {
        return term;
    }

    public int getSearchCount() {
        return searches;
    }

    public WebElement getElement() {
        return element;
    }
}
