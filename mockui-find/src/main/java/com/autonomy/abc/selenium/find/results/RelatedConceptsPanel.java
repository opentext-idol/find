package com.autonomy.abc.selenium.find.results;

import com.autonomy.abc.selenium.find.Container;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Iterator;
import java.util.List;

public class RelatedConceptsPanel implements Iterable<WebElement> {
    private final WebElement panel;
    private final WebDriver driver;

    public RelatedConceptsPanel(final WebDriver driver) {
        this.driver = driver;
        this.panel = Container.RIGHT.findUsing(driver);
    }
    
    @Override
    public Iterator<WebElement> iterator() {
        return relatedConcepts().iterator();
    }

    public WebElement concept(final int i) {
        return relatedConcepts().get(i);
    }

    public List<WebElement> relatedConcepts() {
        waitForRelatedConceptsToLoad();
        return panel.findElements(By.cssSelector(".related-concepts-list:not(.hide) a"));
    }

    public List<WebElement> relatedConceptsClusters() {
        waitForRelatedConceptsToLoad();
        return panel.findElements(By.cssSelector("a.data-entity-cluster"));
    }

    public List<WebElement> membersOfCluster(int i) {
        WebElement container = ElementUtil.ancestor(relatedConceptsClusters().get(i),2);
        return container.findElements(By.cssSelector(".entity-text"));
    }

    public List<String> getRelatedConcepts() {
        return ElementUtil.getTexts(relatedConcepts());
    }

    public WebElement hoverOverRelatedConcept(final int i) {
        final WebElement concept = relatedConcepts().get(i);
        DriverUtil.hover(driver, concept);
        final WebElement popover = panel.findElement(By.className("popover"));
        waitForPopoverToLoad(popover);
        return popover;
    }

    private void waitForPopoverToLoad(final WebElement popover) {
        new WebDriverWait(driver,10).until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(popover, "Loading")));
    }

    private void waitForRelatedConceptsToLoad() {
        Container.RIGHT.waitForLoad(driver);
    }

}
