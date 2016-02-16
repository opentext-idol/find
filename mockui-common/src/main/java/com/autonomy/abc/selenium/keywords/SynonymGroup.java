package com.autonomy.abc.selenium.keywords;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.element.LabelBox;
import com.autonomy.abc.selenium.element.Removable;
import com.autonomy.abc.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class SynonymGroup {
    private final WebElement group;
    private final WebDriver driver;

    public SynonymGroup(WebElement group, WebDriver driver) {
        this.group = group;
        this.driver = driver;
    }

    public void add(String synonym) {
        try {
            synonymAddButton().click();
        } catch (ElementNotVisibleException e) {
            /* box already open */
        }
        synonymInput().setAndSubmit(synonym);
        new WebDriverWait(driver, 30).until(GritterNotice.notificationContaining("Added \"" + synonym.toLowerCase() + "\" to a synonym group"));
    }

    public void remove(String synonym) {
        synonymBox(synonym).removeAndWait();
    }

    public Removable synonymBox(String synonym) {
        WebElement synonymBox = group.findElement(By.cssSelector("[data-term='" + synonym.toLowerCase() + "']"));
        return new LabelBox(synonymBox, driver);
    }

    public List<String> getSynonyms() {
        return ElementUtil.getTexts(group.findElements(By.cssSelector("[data-term]")));
    }

    public WebElement synonymAddButton() {
        return group.findElement(By.cssSelector(".hp-add"));
    }

    public FormInput synonymInput() {
        return new FormInput(group.findElement(By.cssSelector("[name='new-synonym']")), driver);
    }

    public WebElement tickButton() {
        return group.findElement(By.cssSelector(".fa-check"));
    }
}
