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

public class ParametricFilterModal extends ModalView{
    ParametricFilterModal(WebElement element, WebDriver driver){super(element,driver);}

    public static ParametricFilterModal getParametricModal(WebDriver driver){
        WebElement $el = (WebElement)(new WebDriverWait(driver, 30)).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".parametric-modal")));
        ParametricFilterModal view = new ParametricFilterModal($el, driver);
        return view;
    }

    public WebElement cancelButton(){return findElement(new Locator()
            .withTagName("button")
            .containingText("Cancel")
    );}

    public WebElement applyButton(){return findElement(new Locator()
            .withTagName("button")
            .containingText("Apply")
    );}

    public List<WebElement> tabs(){
        return findElements(By.cssSelector(".category-title"));
    }

    public WebElement activeTab(){
        return findElement(By.cssSelector(".category-title .active"));
    }
    public String activeTabName(){return findElement(By.cssSelector("li.category-title.active span")).getText();}

    //input 0-indexed like panel
    public void goToTab(int tabNumber){
        findElement(By.cssSelector(".category-title:nth-child("+ CssUtil.cssifyIndex(tabNumber)+")"));
    }

    public WebElement activePane(){return findElement(By.cssSelector(".tab-pane.active"));
    }

    public List<WebElement> activeFieldList(){return activePane().findElements(By.cssSelector(".checkbox.parametric-field-label"));}

    public String checkCheckBoxInActivePane(int i){
        ParametricModalCheckbox box = new ParametricModalCheckbox(activeFieldList().get(i),getDriver());
        box.check();
        return box.getName();
    }

    public List<String> checkedFieldsAllPanes() {
        List<String> allCheckedFields = new ArrayList<>();
        for (WebElement tab : tabs()) {
            tab.click();
            allCheckedFields.addAll(ElementUtil.getTexts(activePane().findElements(By.cssSelector(".icheckbox-hp.checked + span"))));
        }
        return allCheckedFields;
        }
}
