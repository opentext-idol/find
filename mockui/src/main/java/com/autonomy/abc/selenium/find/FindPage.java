package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.page.search.SearchBase;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FindPage extends SearchBase implements AppPage {
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
        service.refreshContainers();
        service.waitForSearchLoadIndicatorToDisappear(Service.Container.MIDDLE);
    }

}
