package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.element.SOCheckbox;
import com.autonomy.abc.selenium.indexes.Index;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.text.ParseException;
import java.util.Date;

public class SOSearchResult extends SearchResult {
    public SOSearchResult(WebElement result, WebDriver driver){
        super(result, driver);
    }

    @Override
    public WebElement title() {
        return findElement(By.cssSelector("h3 a"));
    }

    @Override
    public WebElement getIcon() {
        return findElement(By.cssSelector(".result-icon a"));
    }

    public Index getIndex() {
        return new Index(findElement(By.className("index")).getText().split(":")[1].trim());
    }

    public float getWeight() {
        return Float.parseFloat(findElement(By.className("weight")).getText().split(" ")[1]);
    }

    public WebElement trashCan() {
        return findElement(By.className("hp-trash"));
    }

    public Date getDate() {
        Date date = null;
        final String dateString = findElement(By.cssSelector(".date")).getText();
        if (!dateString.isEmpty()) {
            try {
                date = SearchBase.RESULT_DATE_FORMAT.parse(dateString.split(", ")[1]);
            } catch (ParseException e) {
                /* NOOP */
            }
        }
        return date;
    }

    public SOCheckbox getCheckbox(){
        return new SOCheckbox(findElement(By.className("checkbox")), getDriver());
    }

    @Override
    public DocumentViewer openDocumentPreview() {
        title().click();
        return DocumentViewer.make(getDriver());
    }
}
