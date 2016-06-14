package com.autonomy.abc.selenium.keywords;

import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import com.hp.autonomy.frontend.selenium.element.LabelBox;
import com.hp.autonomy.frontend.selenium.element.Removable;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class SynonymGroup {
    private final WebElement group;
    private final WebDriver driver;

    public SynonymGroup(final WebElement group, final WebDriver driver) {
        this.group = group;
        this.driver = driver;
    }

    public void add(final String synonym) {
        try {
            synonymAddButton().click();
        } catch (final ElementNotVisibleException e) {
            /* box already open */
        }
        synonymInput().setAndSubmit(synonym);
        new WebDriverWait(driver, 30).until(GritterNotice.notificationContaining("Added \"" + synonym.toLowerCase() + "\" to a synonym group"));
    }

    public void remove(final String synonym) {
        synonymBox(synonym).removeAndWait();
    }

    public Removable synonymBox(final String synonym) {
        final WebElement synonymBox = group.findElement(By.cssSelector("[data-term='" + synonym.toLowerCase() + "']"));
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
