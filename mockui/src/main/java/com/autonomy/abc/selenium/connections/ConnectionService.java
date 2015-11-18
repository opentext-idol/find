package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

public class ConnectionService {
    private Application application;
    private HSOElementFactory elementFactory;
    private ConnectionsPage connectionsPage;
    private NewConnectionPage newConnectionPage;
    private ConnectionsDetailPage connectionsDetailPage;
    private Logger logger = LoggerFactory.getLogger(ConnectionService.class);

    public ConnectionService(Application application, HSOElementFactory elementFactory) {
        this.application = application;
        this.elementFactory = elementFactory;
    }

    protected WebDriver getDriver() {
        return getElementFactory().getDriver();
    }

    protected HSOElementFactory getElementFactory() {
        return elementFactory;
    }

    protected AppBody getBody() {
        return application.createAppBody(getDriver());
    }

    public ConnectionsPage goToConnections() {
        getBody().getSideNavBar().switchPage(NavBarTabId.CONNECTIONS);
        connectionsPage = getElementFactory().getConnectionsPage();
        return connectionsPage;
    }

    public ConnectionsDetailPage goToDetails(final Connector connector) {
        return goToDetails(connector.getName());
    }

    public ConnectionsDetailPage goToDetails(final String name) {
        goToConnections();
        connectionsPage.connectionWithTitleContaining(name).click();
        connectionsDetailPage = getElementFactory().getConnectionsDetailPage();
        return connectionsDetailPage;
    }

    public ConnectionsPage setUpConnection(final Connector connector) {
        goToConnections();
        connectionsPage.newConnectionButton().click();
        newConnectionPage = elementFactory.getNewConnectionPage();
        connector.makeWizard(newConnectionPage).apply();
        new WebDriverWait(getDriver(), 300).withMessage("connection " + connector + " timed out").until(GritterNotice.notificationContaining(connector.getFinishedNotification()));
        return connectionsPage;
    }

    public ConnectionsPage deleteConnection(final Connector connector, boolean deleteIndex) {
        beginDelete(connector);

        if(deleteIndex) {
            deleteIndex();
        }

        confirmDelete(connector);

        return connectionsPage;
    }

    private void beginDelete(Connector connector){
        ConnectionsDetailPage connectionsDetailPage = goToDetails(connector);
        connectionsDetailPage.deleteButton().click();
    }

    private void deleteIndex(){
        connectionsDetailPage.alsoDeleteIndexCheckbox().click();
    }

    private void confirmDelete(Connector connector){
        connectionsDetailPage.deleteConfirmButton().click();
        connectionsPage = elementFactory.getConnectionsPage();
        new WebDriverWait(getDriver(), 100).until(GritterNotice.notificationContaining(connector.getDeleteNotification()));
    }

    public ConnectionsPage deleteAllConnections(boolean deleteIndex) {
        goToConnections();
        for(WebElement connector : getDriver().findElements(By.className("listItemTitle"))){
            WebConnector webConnector = new WebConnector(null, connector.getText().split("\\(")[0].trim());

            beginDelete(webConnector);

            if (deleteIndex) {
                try {
                    deleteIndex();
                } catch (Exception e) {/* May have other connections associated */}
            }

            confirmDelete(webConnector);
        }
        return connectionsPage;
    }
}
