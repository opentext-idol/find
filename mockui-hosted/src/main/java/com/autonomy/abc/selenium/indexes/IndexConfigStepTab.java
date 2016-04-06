package com.autonomy.abc.selenium.indexes;

import com.autonomy.abc.selenium.icma.ICMAPageBase;
import com.hp.autonomy.frontend.selenium.element.ChevronContainer;
import com.hp.autonomy.frontend.selenium.element.Collapsible;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class IndexConfigStepTab extends ICMAPageBase {
    public IndexConfigStepTab(WebDriver driver) {
        super(driver);
    }

    public static IndexConfigStepTab make(WebDriver driver){
        new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'Advanced options are not mandatory')]")));
        return new IndexConfigStepTab(driver);
    }

    public void setIndexFields(List<String> indexFields) {
        advancedOptions().expand();
        String joined = StringUtils.join(indexFields, ",");
        indexFieldsInput().setValue(joined);
    }

    public void setParametricFields(List<String> parametricFields) {
        advancedOptions().expand();
        String joined = StringUtils.join(parametricFields, ",");
        parametricFieldsInput().setValue(joined);
    }

    public FormInput indexFieldsInput() {
        return fieldInput("indexFields");
    }

    public FormInput parametricFieldsInput() {
        return fieldInput("parametricFields");
    }

    private FormInput fieldInput(String inputType) {
        WebElement input = findElement(By.cssSelector("[for='" + inputType + "'] + div input"));
        return new FormInput(input, getDriver());
    }

    public WebElement advancedIndexFieldsInformation() {
        return findElement(By.cssSelector("[for='indexFields'] + div i"));
    }

    public WebElement advancedParametricFieldsInformation() {
        return findElement(By.cssSelector("[for='parametricFields'] + div i"));
    }

    public Collapsible advancedOptions() {
        WebElement panel = findElement(By.className("panel"));
        return new ChevronContainer(ElementUtil.ancestor(panel, 1), getDriver());
    }
}
