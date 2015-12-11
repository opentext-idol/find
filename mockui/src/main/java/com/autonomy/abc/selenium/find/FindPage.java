package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.element.Dropdown;
import com.autonomy.abc.selenium.page.search.SearchBase;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class FindPage extends AppElement implements AppPage {
    private final Input input;
    private final Service service;

    public FindPage(WebDriver driver){
        super(new WebDriverWait(driver,30).until(ExpectedConditions.visibilityOfElementLocated(By.className("container-fluid"))),driver);
        input = new Input(driver);
        service = new Service(driver);
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.className("container-fluid")));
    }

    public Input getInput() {
        return input;
    }

    public Service getService() {
        return service;
    }

    public void search(String searchTerm){
        input.clear();
        input.sendKeys(searchTerm);
        service.waitForSearchLoadIndicatorToDisappear(Service.Container.MIDDLE);
    }

    public List<String> getSelectedPublicIndexes() {
        List<String> indexes = new ArrayList<>();

        for(WebElement selectedIndex : findElements(By.cssSelector("[data-category-id='public'] .icon-ok.database-icon"))){
            indexes.add(selectedIndex.findElement(By.xpath("./../../span[@class='database-name' or @class='category-name']")).getText());
        }

        return indexes;
    }

    public void sortBy(SearchBase.Sort sort) {
        sortDropdown().select(sort.toString());
    }

    private Dropdown sortDropdown() {
        WebElement dropdownContainer = findElement(By.cssSelector(".sort-container"));
        return new Dropdown(dropdownContainer, getDriver());
    }
}
