package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class RelatedConceptsPanel {
    private final WebElement panel;
    private final WebDriver driver;

    public RelatedConceptsPanel(WebDriver driver) {
        this.driver = driver;
        this.panel = Container.RIGHT.findUsing(driver);
    }

    public List<WebElement> relatedConcepts() {
        waitForRelatedConceptsToLoad();
        return panel.findElements(By.cssSelector(".related-concepts-list a"));
    }

    public List<String> getRelatedConcepts() {
        return ElementUtil.getTexts(relatedConcepts());
    }

    public WebElement hoverOverRelatedConcept(int i) {
        WebElement concept = relatedConcepts().get(i);
        DriverUtil.hover(getDriver(), concept);
        WebElement popover = panel.findElement(By.className("popover"));
        waitForPopoverToLoad(popover);
        return popover;
    }

    public void unhover() {
        /* click somewhere not important to remove hover -
        * clicking the search term box seems safe... */
        getDriver().findElement(By.cssSelector("input.find-input")).click();
        new WebDriverWait(getDriver(),2).until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("popover"))));
    }

    private void waitForPopoverToLoad(WebElement popover) {
        new WebDriverWait(getDriver(),10).until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(popover, "Loading")));
    }

    private void waitForRelatedConceptsToLoad() {
        Container.RIGHT.waitForLoad(driver);
    }

    private WebDriver getDriver() {
        return driver;
    }

}
