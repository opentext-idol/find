package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.indexes.Index;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class SOSearchResult extends SearchResult {
    private final Index index;
    private final float weight;
    private final WebElement trashCan;
    private String date;

    public SOSearchResult(WebElement result){
        title = result.findElement(By.cssSelector("h3 a"));
        description = result.findElement(By.className("result-summary")).getText();
        icon = result.findElement(By.className(".result-icon a"));

        index = new Index(result.findElement(By.className("index")).getText().split(":")[1].trim());
        weight = Float.parseFloat(result.findElement(By.className("weight")).getText().split(" ")[1]);

        try {
            date = result.findElement(By.className("date")).getText().split(":")[1].trim();
        } catch (NoSuchElementException e) {
            date = null;
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

    public String getDate() {
        return date;
    }
}
