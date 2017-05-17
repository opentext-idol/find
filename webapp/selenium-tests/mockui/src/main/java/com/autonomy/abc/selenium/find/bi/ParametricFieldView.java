package com.autonomy.abc.selenium.find.bi;

import com.autonomy.abc.selenium.find.Container;
import com.hp.autonomy.frontend.selenium.element.ChosenDrop;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

abstract class ParametricFieldView {

    private final WebDriver driver;
    private final WebElement container;

    ParametricFieldView(final WebDriver driver, final By locator) {
        this.driver = driver;
        this.container = Container.currentTabContents(driver).findElement(locator);
    }

    public String getFirstSelectedFieldName(){
        return firstParametricFilter().getText();
    }

    public String getSecondSelectedFieldName(){
        return secondParametricFilter().getText();
    }

    private WebElement firstParametricFilter(){
        return findElement(By.cssSelector(".parametric-selections span:nth-child(1)"));
    }

    private WebElement secondParametricFilter(){
        return findElement(By.cssSelector(".parametric-selections span:nth-child(3)"));
    }

    public boolean parametricSelectionDropdownsExist(){
        return findElement(By.cssSelector(".parametric-selections span")).isDisplayed();
    }

    public ChosenDrop firstParametricSelectionDropdown(){
        return new ChosenDrop(firstParametricFilter(), getDriver());
    }

    public ChosenDrop secondParametricSelectionDropdown(){
        return new ChosenDrop(secondParametricFilter(), getDriver());
    }

    public List<String> getParametricDropdownItems(final ChosenDrop chosenDrop){
        final List<String> badFormat = ElementUtil.getTexts(chosenDrop.getItems());
        return badFormat.stream().map(String::toUpperCase).collect(Collectors.toList());
    }

    public void clickSwapButton() {
        findElement(By.cssSelector(".parametric-swap")).click();
    }

    public WebElement message() {
        return findElement(By.cssSelector(".parametric-view-message"));
    }

    protected WebDriver getDriver() {
        return driver;
    }

    protected WebElement findElement(final By locator) {
        return container.findElement(locator);
    }

    protected List<WebElement> findElements(final By locator) {
        return container.findElements(locator);
    }
}
