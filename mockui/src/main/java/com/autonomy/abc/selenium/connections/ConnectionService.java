package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ConnectionService {
    private Application application;
    private HSOElementFactory elementFactory;
    private ConnectionsPage connectionsPage;
    private NewConnectionPage newConnectionPage;
    private ConnectionsDetailPage connectionsDetailPage;

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
        ConnectionsDetailPage connectionsDetailPage = goToDetails(connector);
        connectionsDetailPage.deleteButton().click();
        if(deleteIndex) {
            connectionsDetailPage.alsoDeleteIndexCheckbox().click();
        }
        connectionsDetailPage.deleteConfirmButton().click();
        connectionsPage = elementFactory.getConnectionsPage();
        new WebDriverWait(getDriver(), 100).until(GritterNotice.notificationContaining(connector.getDeleteNotification()));
        // TODO: CSA-1539
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {}
        return connectionsPage;
    }
}
