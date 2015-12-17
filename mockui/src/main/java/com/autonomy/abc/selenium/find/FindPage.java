package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.element.Dropdown;
import com.autonomy.abc.selenium.indexes.tree.FindIndexCategoryNode;
import com.autonomy.abc.selenium.indexes.tree.IndexNodeElement;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
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
    private final FindInput input;
    private final FindResults results;

    public FindPage(WebDriver driver){
        super(new WebDriverWait(driver,30)
                .withMessage("loading Find page")
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("container-fluid"))),driver);
        input = new FindInput(driver);
        results = new FindResults(driver);
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.className("container-fluid")));
    }

    public FindInput getInput() {
        return input;
    }

    public FindResults getResultsPage() {
        return results;
    }

    public void search(String searchTerm){
        input.clear();
        input.sendKeys(searchTerm);
        results.waitForSearchLoadIndicatorToDisappear(FindResults.Container.MIDDLE);
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
        return new IndexesTree(new FindIndexCategoryNode(findElement(By.cssSelector(".databases-list [data-category-id='all']")), getDriver()));
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
        results.waitForSearchLoadIndicatorToDisappear(FindResults.Container.MIDDLE);
    }
}
