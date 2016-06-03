package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.filters.ParametricFieldContainer;
import com.autonomy.abc.selenium.find.filters.ParametricFilterTree;
import com.autonomy.abc.selenium.indexes.IdolDatabaseTree;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class IdolFindPage extends FindPage {

    private IdolFindPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected FilterPanel filters() {
        return new FilterPanel(new IdolDatabaseTree.Factory(), getDriver());
    }

    private ParametricFilterTree parametricFilterTree() {
        return new ParametricFilterTree(leftContainer(), getParametricFilters(), getDriver());
    }

    private List<WebElement> getParametricFilters() {
        List<WebElement> ancestors = new ArrayList<>();
        for (WebElement element : findElements(By.className("parametric-fields-table"))) {
            ancestors.add(ElementUtil.ancestor(element, 3));
        }
        return ancestors;
    }

    //to be displayed as a segment on sunburst, docs in category must be >=5% of total
    private int minDocsNeededForSunburstSegment(int total){
        return (int) Math.round((total/(double)100)*5);
    }

    public int numParametricChildrenBigEnoughForSunburst(String filter){
        return parametricChildrenBigEnoughForSunburst(filter).size();

    }
    public List<String> nameParametricChildrenBigEnoughForSunburst(String filter){
        List <String> names = new ArrayList<>();
        for(WebElement wholeChild:parametricChildrenBigEnoughForSunburst(filter)){
            names.add(wholeChild.findElement(By.className("parametric-value-name")).getText());
        }
        return names;
    }

    private List<WebElement> parametricChildrenBigEnoughForSunburst(String filter){
        ParametricFieldContainer node = parametricFilterTree().findParametricFilterNode(filter);
        int total = node.getTotalDocNumber();
        int cutOff = minDocsNeededForSunburstSegment(total);

        List<WebElement> bigEnough = new ArrayList<>();
        int minShowCount = 20;
        for(WebElement parametricFilter : node.getFullChildrenElements()){
            WebElement count = parametricFilter.findElement(By.className("parametric-value-count"));
            String countString = count.getText().replaceAll("[()]","");
            if (minShowCount > 0 || Integer.parseInt(countString) > cutOff){
                bigEnough.add(parametricFilter);
                minShowCount = minShowCount - 1;
            }
            else{
                return bigEnough;
            }
        }
        //is now returning whole child element
        return bigEnough;
    }

    public static class Factory implements ParametrizedFactory<WebDriver, IdolFindPage> {
        public IdolFindPage create(WebDriver context) {
            return new IdolFindPage(context);
        }
    }
}

