package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.element.HPRemovable;
import com.autonomy.abc.selenium.element.Removable;
import com.autonomy.abc.selenium.users.LoginService;
import com.autonomy.abc.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class FindTopNavBar implements LoginService.LogoutHandler {
    private WebDriver driver;
    private WebElement header;
    private FormInput input;

    FindTopNavBar(WebDriver driver) {
        this.driver = driver;
        this.header = driver.findElement(By.className("header"));
        this.input = new FormInput(driver.findElement(By.className("find-input")), driver);
    }

    @Override
    public void logOut() {
        findElement(By.className("hp-settings")).click();
        findElement(By.className("navigation-logout")).click();
    }

    public String getSearchBoxTerm() {
        return input.getValue();
    }

    public String getCurrentUsername() {
        return findElement(By.className("navbar-username")).getText();
    }

    public List<Removable> additionalConcepts() {
        List<Removable> removables = new ArrayList<>();
        for (WebElement concept : additionalConceptElements()) {
            removables.add(new HPRemovable(concept, driver));
        }
        return removables;
    }

    public List<String> getAlsoSearchingForTerms() {
        return ElementUtil.getTexts(additionalConceptElements());
    }

    private List<WebElement> additionalConceptElements() {
        return findElements(By.className(".selected-related-concept"));
    }

    private WebElement findElement(By locator) {
        return header.findElement(locator);
    }

    private List<WebElement> findElements(By locator) {
        return header.findElements(locator);
    }
}
