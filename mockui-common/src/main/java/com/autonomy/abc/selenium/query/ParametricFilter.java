package com.autonomy.abc.selenium.query;

import com.autonomy.abc.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParametricFilter implements SearchFilter {
    private Map<String, String> map;

    public ParametricFilter(String category, String field){
        this.map = new HashMap<>();
        map.put(category, field);
    }

    public ParametricFilter(Map<String, String> map){
        this.map = new HashMap<>(map);
    }

    @Override
    public void apply(SearchFilter.Filterable page) {
        if(page instanceof Filterable){
            Filterable filterable = (Filterable) page;
            WebElement parametricContainer = filterable.parametricContainer();
            uncheckAll(parametricContainer, filterable);

            for(Map.Entry<String, String> entry : map.entrySet()){
                WebElement filterContainer = filterContainer(parametricContainer, entry.getKey());
                fieldCheckbox(filterContainer, entry.getValue()).click();
                filterable.waitForParametricValuesToLoad();
            }
        }
    }

    private WebElement filterContainer(WebElement parametricContainer, String category){
        return parametricContainer.findElement(By.cssSelector("[data-field='" + category.toLowerCase().replace(" ","_") + "']"));
    }

    private WebElement fieldCheckbox(WebElement filterContainer, String field){
        return filterContainer.findElement(By.cssSelector("[data-value='" + field.toUpperCase() + "'] .parametric-value-text"));
    }

    private boolean isUnchecked(WebElement checkBox){
        return ElementUtil.hasClass("hide", checkBox) && !ElementUtil.hasClass("checked", checkBox);
    }

    private void uncheckAll(WebElement parametricContainer, Filterable filterable){
        List<WebElement> checkboxes;

        checkboxes = parametricContainer.findElements(By.className("icheckbox_square-green"));
        if(checkboxes.isEmpty()){
            parametricContainer.findElements(By.className("fa-check"));
        }

        for(WebElement element : checkboxes){
            if(isUnchecked(element)){
                element.click();
                filterable.waitForParametricValuesToLoad();
            }
        }
    }

    @Override
    public String toString() {
        return "ParametricFilter:" + map;
    }

    public interface Filterable extends SearchFilter.Filterable{
        WebElement parametricContainer();
        void waitForParametricValuesToLoad();
    }
}
