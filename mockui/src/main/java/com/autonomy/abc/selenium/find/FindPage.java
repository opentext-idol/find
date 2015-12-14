package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.element.Dropdown;
import com.autonomy.abc.selenium.indexes.IndexNodeElement;
import com.autonomy.abc.selenium.indexes.IndexesTree;
import com.autonomy.abc.selenium.page.search.SearchBase;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.SearchFilter;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class FindPage extends AppElement implements AppPage, IndexFilter.Filterable {
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

    @Override
    public IndexesTree indexesTree() {
        return new IndexesTree(findElement(By.cssSelector(".indexes-container")), getDriver());
    }

    public void sortBy(SearchBase.Sort sort) {
        sortDropdown().select(sort.toString());
    }

    private Dropdown sortDropdown() {
        WebElement dropdownContainer = findElement(By.cssSelector(".sort-container"));
        return new Dropdown(dropdownContainer, getDriver());
    }

    public List<String> getPrivateIndexNames() {
        List<String> names = new ArrayList<>();
        indexesTree().privateIndexes().expand();
        for (IndexNodeElement element : indexesTree().privateIndexes()) {
            names.add(element.getName());
        }
        return names;
    }

    @Override
    public void filterBy(SearchFilter filter) {
        filter.apply(this);
        service.waitForSearchLoadIndicatorToDisappear(Service.Container.MIDDLE);
    }
}
