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
        final WebElement $el = new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".parametric-modal")));
        return new ParametricFilterModal($el, driver);
    }

    public WebElement cancelButton() {
        return findElement(new Locator()
            .withTagName("button")
            .containingText("Cancel")
        );
    }

    public WebElement applyButton() {
        return findElement(new Locator()
            .withTagName("button")
            .containingText("Apply")
        );
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

    public List<WebElement> activeFieldList() {
        return activePane().findElements(By.cssSelector(".checkbox.parametric-field-label"));
    }

    public String checkCheckBoxInActivePane(final int i) {
        final ParametricModalCheckbox box = new ParametricModalCheckbox(activeFieldList().get(i), getDriver());
        box.check();
        return box.getName();
    }

    public List<String> checkedFieldsAllPanes() {
        final List<String> allCheckedFields = new ArrayList<>();
        for (final WebElement tab : tabs()) {
            tab.click();
            allCheckedFields.addAll(ElementUtil.getTexts(activePane().findElements(By.cssSelector(".icheckbox-hp.checked + span"))));
        }
        return allCheckedFields;
    }

    public Iterator<ParametricModalCheckbox> iterator() {
        return values().iterator();
    }

    public List<ParametricModalCheckbox> values(){
        final List<ParametricModalCheckbox> boxes = new ArrayList<>();
        for (final WebElement checkbox: activeFieldList()){
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
