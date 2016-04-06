package com.autonomy.abc.selenium.connections;

import com.hp.autonomy.frontend.selenium.element.Dropdown;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.autonomy.abc.selenium.icma.ICMAPageBase;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class ConnectionsPage extends ICMAPageBase {
    private WebElement toolbar;

    private ConnectionsPage(final WebDriver driver) {
        super(driver);
    }

    private WebElement toolbar() {
        if (toolbar == null) {
            toolbar = findElement(By.cssSelector(".affix-toolbar"));
        }
        return toolbar;
    }

    public WebElement newConnectionButton() {
        return toolbar().findElement(By.id("new-repo-btn"));
    }

    public FormInput connectionFilterBox() {
        return new FormInput(toolbar().findElement(By.className(("keywords-search-filter"))), getDriver());
    }

    public Dropdown connectionFilterDropdown() {
        return new Dropdown(toolbar().findElement(By.className("btn-group")), getDriver());
    }

    public List<WebElement> connectionsList() {
        return findElements(By.cssSelector(".list-group .data-container"));
    }

    WebElement connectionWithTitleContaining(String name) {
        return findElement(By.xpath(".//*[contains(@class, 'listItemTitle')][contains(text(), '" + name + "')]"));
    }

    WebElement displayedConnectionWithTitleContaining(String name){
        for (WebElement connection : findElements(By.xpath(".//*[contains(@class, 'listItemTitle')][contains(text(), '" + name + "')]"))){
            if (connection.isDisplayed()){
                return connection;
            }
        }
        return null;
    }

    public String getIndexOf(Connector connector) {
        return ElementUtil.ancestor(connectionWithTitleContaining(connector.getName()), 9).findElement(By.cssSelector(".listItemNormalText.ng-scope")).getText().split(":")[1].trim();
    }

    public List<String> getConnectionNames() {
        List<String> connections = new ArrayList<>();
        for(String connection : ElementUtil.getTexts(connectionsList())){
            connections.add(connection.split("\\(")[0].trim());
        }
        return connections;
    }

    public static class Factory extends ICMAPageFactory<ConnectionsPage> {
        public Factory() {
            super(ConnectionsPage.class);
        }

        @Override
        public ConnectionsPage create(WebDriver context) {
            new WebDriverWait(context, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("base-page-content")));
            return new ConnectionsPage(context);
        }
    }
}
