package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.element.SOCheckbox;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.query.QueryResult;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.text.ParseException;
import java.util.Date;

public class IsoSearchResult extends QueryResult {
    public IsoSearchResult(WebElement result, WebDriver driver){
        super(result, driver);
    }

    @Override
    public WebElement title() {
        return findElement(By.cssSelector("h3 a"));
    }

    @Override
    public WebElement icon() {
        return findElement(By.cssSelector(".result-icon a"));
    }

    public Index getIndex() {
        return new Index(indexLabel().getText().split(":")[1].trim());
    }

    public WebElement indexLabel() {
        return findElement(By.className("index"));
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
