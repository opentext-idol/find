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
import java.util.List;

public class ParametricFilterModal extends ModalView {
    ParametricFilterModal(final WebElement element, final WebDriver driver) {
        super(element, driver);
    }

    public static ParametricFilterModal getParametricModal(final WebDriver driver) {
        final WebElement $el = (new WebDriverWait(driver, 30)).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".parametric-modal")));
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
}
