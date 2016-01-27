package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.element.SOCheckbox;
import com.autonomy.abc.selenium.indexes.Index;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SOSearchResult extends SearchResult {
    private final WebElement result;

    private final Index index;
    private final float weight;
    private final WebElement trashCan;
    private Date date;

    public SOSearchResult(WebElement result, WebDriver driver){
        this.result = result;
        this.driver = driver;

        title = result.findElement(By.cssSelector("h3 a"));
        description = result.findElement(By.className("result-summary")).getText();
        icon = result.findElement(By.cssSelector(".result-icon a"));

        index = new Index(result.findElement(By.className("index")).getText().split(":")[1].trim());
        weight = Float.parseFloat(result.findElement(By.className("weight")).getText().split(" ")[1]);

        final String dateString = result.findElement(By.cssSelector(".date")).getText();

        if (dateString.isEmpty()) {
            date = null;
        } else {
            try {
                date = new SimpleDateFormat("dd MMMMMMMMM yyyy HH:mm").parse(dateString.split(", ")[1]);
            } catch (ParseException e) {
                date = null;
            }
        }

        trashCan = result.findElement(By.className("hp-trash"));
    }

    public Index getIndex() {
        return index;
    }

    public float getWeight() {
        return weight;
    }

    public WebElement getTrashCan() {
        return trashCan;
    }

    public Date getDate() {
        return date;
    }

    public SOCheckbox getCheckbox(){
        try {
            return new SOCheckbox(result.findElement(By.className("checkbox")), driver);
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}
