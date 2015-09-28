package com.autonomy.abc.selenium.page.promotions;

import com.autonomy.abc.selenium.element.*;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class PromotionsDetailPage extends AppElement implements AppPage {
    private final static By TRIGGERS = By.cssSelector(".promotion-view-match-terms .term");

    public PromotionsDetailPage(WebDriver driver) {
        super(new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper-content"))), driver);
        waitForLoad();
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOfElementLocated(By.className("promotion-match-terms")));
    }

    public WebElement backButton() {
        return findElement(By.xpath(".//a[text()[contains(., 'Back')]]"));
    }

    public Dropdown editMenu() {
        return new Dropdown(findElement(By.className("extra-functions")), getDriver());
    }

    public Editable promotionTitle() {
        return new InlineEdit(findElement(By.className("promotion-title-edit")), getDriver());
    }

    public List<String> getTriggerList() {
        final List<String> triggers = new ArrayList<>();
        for (final WebElement trigger : findElements(TRIGGERS)) {
            triggers.add(trigger.getAttribute("data-id"));
        }
        return triggers;
    }

    public List<Removable> triggers() {
        final List<Removable> triggers = new ArrayList<>();
        for (final WebElement trigger : findElements(By.cssSelector(".promotion-view-match-terms .term"))) {
            triggers.add(new LabelBox(trigger, getDriver()));
        }
        return triggers;
    }

    public Removable trigger(final String triggerName) {
        return new LabelBox(findElement(By.cssSelector(".promotion-view-match-terms [data-id='" + triggerName + "']")), getDriver());
    }

    public FormInput triggerAddBox() {
        return new FormInput(findElement(By.cssSelector(".promotion-match-terms [name='words']")), getDriver());
    }

    public List<WebElement> promotedList() {
        return findElements(By.cssSelector(".promotion-list-container h3"));
    }

    public List<String> getPromotedTitles() {
        final List<String> docTitles = new ArrayList<>();
        for (final WebElement docTitle : promotedList()) {
            docTitles.add(docTitle.getText());
        }
        return docTitles;
    }
}
