package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.util.CssUtil;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Locator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ParametricFilterModal extends ModalView implements Iterable<ParametricModalCheckbox> {
    //number of segments visible in sunburst - calculable from ParametricFilterModal
    private static final int VISIBLE_SEGMENTS = 20;

    private final WebDriver driver;

    ParametricFilterModal(final WebElement element, final WebDriver webDriver) {
        super(element, webDriver);
        driver = webDriver;
    }

    public static ParametricFilterModal getParametricModal(final WebDriver driver) {
        final WebElement $el = new WebDriverWait(driver, 30)
                .withMessage("Parametric filter modal did not open within 30 seconds ")
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".parametric-modal")));
        return new ParametricFilterModal($el, driver);
    }

    public boolean loadingIndicatorPresent() {
        return !findElements(By.cssSelector(".loading-spinner")).isEmpty();
    }

    public void cancel() {
        findElement(new Locator()
            .withTagName("button")
            .containingText("Cancel")
        ).click();
        new WebDriverWait(driver,5).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".parametric-modal")));
    }

    public void apply() {
        findElement(new Locator()
            .withTagName("button")
            .containingText("Apply")
        ).click();
        new WebDriverWait(driver,5).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".parametric-modal")));

    }

    public List<WebElement> tabs() {
        return findElements(By.cssSelector(".category-title"));
    }

    public WebElement activeTab() {
        return findElement(By.cssSelector(".category-title .active"));
    }

    public String activeTabName() {
        return findElement(By.cssSelector("li.category-title.active span")).getText();
    }

    //input 0-indexed like panel
    public void goToTab(final int tabNumber) {
        findElement(By.cssSelector(".category-title:nth-child(" + CssUtil.cssifyIndex(tabNumber) + ')'));
    }

    public WebElement activePane() {
        return findElement(By.cssSelector(".tab-pane.active"));
    }

    public List<WebElement> activePaneFilterList() {
        return activePane().findElements(By.cssSelector(".checkbox.parametric-field-label"));
    }

    public List<WebElement> allFilters() {
        return findElements(By.cssSelector(".checkbox.parametric-field-label"));
    }

    public String checkCheckBoxInActivePane(final int i) {
        final ParametricModalCheckbox box = new ParametricModalCheckbox(activePaneFilterList().get(i), getDriver());
        box.check();
        return box.getName();
    }

    public List<String> checkedFiltersAllPanes() {
        final List<String> allCheckedFilters = new ArrayList<>();
        for (final WebElement tab : tabs()) {
            tab.click();
            allCheckedFilters.addAll(ElementUtil.getTexts(activePane().findElements(By.cssSelector(".icheckbox-hp.checked + span"))));
        }
        return allCheckedFilters;
    }

    public Iterator<ParametricModalCheckbox> iterator() {
        return values().iterator();
    }

    public List<ParametricModalCheckbox> values(){
        final List<ParametricModalCheckbox> boxes = new ArrayList<>();
        for (final WebElement checkbox: activePaneFilterList()){
            boxes.add(new ParametricModalCheckbox(checkbox,driver));
        }
        return boxes;
    }

    private int totalResultsInPane(final Iterable<ParametricModalCheckbox> checkboxes){
        int totalResults = 0;

        for (final ParametricModalCheckbox checkbox: checkboxes){
            if (checkbox.getResultsCount()!=0) {
                totalResults += checkbox.getResultsCount();
            }
            else{
                break;
            }
        }
        return totalResults;
    }

    public int filtersWithResultsForCurrentSearch() {
        final Iterable<ParametricModalCheckbox> checkboxes = values();
        int count = 0;

        for (final ParametricModalCheckbox checkbox : checkboxes) {
            if(checkbox.getResultsCount()!=0) {
                count++;
            }
            else {
                break;
            }
        }
        cancel();
        return count;
    }

    public List<String> expectedParametricValues(){
        final Iterable<ParametricModalCheckbox> checkboxes = values();
        final List<String> expected = new ArrayList<>();

        int totalResults = totalResultsInPane(checkboxes);

        for (final ParametricModalCheckbox checkbox : checkboxes) {
            final int thisCount = checkbox.getResultsCount();
            if ((expected.size() < VISIBLE_SEGMENTS || isBigEnough(thisCount, totalResults)) && thisCount!=0) {
                expected.add(checkbox.getName());
            } else {
                break;
            }
        }
        return expected;
    }

    private static boolean isBigEnough(final int thisCount, final int totalResults) {
        return (double) thisCount /totalResults >= 0.05;
    }
}
