package com.autonomy.abc.selenium.query;

import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParametricFilter implements QueryFilter {
    private final Map<String, String> map;

    public ParametricFilter(final String category, final String field){
        this.map = new HashMap<>();
        map.put(category, field);
    }

    public ParametricFilter(final Map<String, String> map){
        this.map = new HashMap<>(map);
    }

    @Override
    public void apply(final QueryFilter.Filterable page) {
        if(page instanceof Filterable){
            final Filterable filterable = (Filterable) page;
            final WebElement parametricContainer = filterable.parametricContainer();
            uncheckAll(parametricContainer, filterable);

            for(final Map.Entry<String, String> entry : map.entrySet()){
                final WebElement filterContainer = filterContainer(parametricContainer, entry.getKey());
                fieldCheckbox(filterContainer, entry.getValue()).click();
                filterable.waitForParametricValuesToLoad();
            }
        }
    }

    private WebElement filterContainer(final WebElement parametricContainer, final String category){
        return parametricContainer.findElement(By.cssSelector("[data-field='" + category.replace(" ","_") + "']"));
    }

    private WebElement fieldCheckbox(final WebElement filterContainer, final String field){
        return filterContainer.findElement(By.cssSelector("[data-value='" + field.toUpperCase() + "'] .parametric-value-text"));
    }

    private boolean isUnchecked(final WebElement checkBox){
        return ElementUtil.hasClass("hide", checkBox) && !ElementUtil.hasClass("checked", checkBox);
    }

    private void uncheckAll(final WebElement parametricContainer, final Filterable filterable){
        final List<WebElement> checkboxes;

        checkboxes = parametricContainer.findElements(By.className("icheckbox_square-green"));
        if(checkboxes.isEmpty()){
            parametricContainer.findElements(By.className("fa-check"));
        }

        for(final WebElement element : checkboxes){
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

    public interface Filterable extends QueryFilter.Filterable{
        WebElement parametricContainer();
        void waitForParametricValuesToLoad();
    }
}
