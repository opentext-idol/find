package com.autonomy.abc.selenium.page.keywords;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.element.LabelBox;
import com.autonomy.abc.selenium.util.ElementUtil;
import org.openqa.selenium.By;
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
        synonymAddButton().click();
        synonymInput().setAndSubmit(synonym);
        new WebDriverWait(driver, 30).until(GritterNotice.notificationContaining("Added \"" + synonym + "\" to a synonym group"));
    }

    public void remove(String synonym) {
        WebElement synonymBox = group.findElement(By.cssSelector("[data-term='" + synonym + "']"));
        new LabelBox(synonymBox, driver).removeAndWait();
    }

    public List<String> getSynonyms() {
        return ElementUtil.getTexts(group.findElements(By.cssSelector("[data-term]")));
    }

    private WebElement synonymAddButton() {
        return group.findElement(By.cssSelector(".hp-add"));
    }

    private FormInput synonymInput() {
        return new FormInput(group.findElement(By.cssSelector("[name='new-synonym']")), driver);
    }
}
