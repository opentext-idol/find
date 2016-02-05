package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.element.SOCheckbox;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.page.search.SearchBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SOSearchResult extends SearchResult {
    private WebDriver driver;

    public SOSearchResult(WebElement result, WebDriver driver){
        super(result);
        this.driver = driver;
    }

    @Override
    public WebElement title() {
        return result.findElement(By.cssSelector("h3 a"));
    }

    @Override
    public WebElement getIcon() {
        return result.findElement(By.cssSelector(".result-icon a"));
    }

    public Index getIndex() {
        return new Index(result.findElement(By.className("index")).getText().split(":")[1].trim());
    }

    public float getWeight() {
        return Float.parseFloat(result.findElement(By.className("weight")).getText().split(" ")[1]);
    }

    public WebElement trashCan() {
        return result.findElement(By.className("hp-trash"));
    }

    public Date getDate() {
        Date date = null;
        final String dateString = result.findElement(By.cssSelector(".date")).getText();
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
        return new SOCheckbox(result.findElement(By.className("checkbox")), driver);
    }
}
