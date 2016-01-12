package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorIndexStepTab;
import com.autonomy.abc.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ConnectionService {
    private Application application;
    private HSOElementFactory elementFactory;
    private ConnectionsPage connectionsPage;
    private NewConnectionPage newConnectionPage;
    private ConnectionsDetailPage connectionsDetailPage;
    private final static Logger LOGGER = LoggerFactory.getLogger(ConnectionService.class);

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
        connectionsPage.displayedConnectionWithTitleContaining(name).click();
        connectionsDetailPage = getElementFactory().getConnectionsDetailPage();
        return connectionsDetailPage;
    }

    public ConnectionsPage setUpConnection(final Connector connector) {
        goToConnections();
        connectionsPage.newConnectionButton().click();
        newConnectionPage = elementFactory.getNewConnectionPage();
        connector.makeWizard(newConnectionPage).apply();
        new WebDriverWait(getDriver(), 20).until(GritterNotice.notificationContaining("started"));
        LOGGER.info("Connection '" + connector.getName() + "' started");
        Long startTime = System.currentTimeMillis();
        waitForConnectorToRun(connector);
        LOGGER.info("Connection '" + connector.getName() + "' finished");
        if(connector instanceof WebConnector){
            int timeTaken = (int) ((System.currentTimeMillis() - startTime) / 1000);
            if(timeTaken > ((WebConnector) connector).getDuration()) {
                LOGGER.error("Connection '" + connector.getName() + "' took " + timeTaken + " seconds to complete");
            }
        }
        return connectionsPage;
    }

    private void waitForConnectorToRun(final Connector connector) {
        new WebDriverWait(getDriver(), 300)
                .withMessage("running connection " + connector)
                .until(GritterNotice.notificationContaining(connector.getFinishedNotification()));
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

        List<String> titles = new ArrayList<>();
        for(WebElement connector : getDriver().findElements(By.className("listItemTitle"))){
            titles.add(connector.getText().split("\\(")[0].trim());
        }

        for(String title : titles){
            WebConnector webConnector = new WebConnector(null, title);

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

    public ConnectionsDetailPage updateLastRun(WebConnector webConnector) {
        goToDetails(webConnector);
        webConnector.setStatistics(new ConnectionStatistics(connectionsDetailPage.lastRun()));
        return connectionsDetailPage;
    }

    public Connector changeIndex(Connector connector, Index index) {
        goToDetails(connector);
        connectionsDetailPage.editButton().click();

        NewConnectionPage newConnectionPage = NewConnectionPage.make(getDriver());
        newConnectionPage.nextButton().click();
        Waits.loadOrFadeWait();
        newConnectionPage.nextButton().click();
        Waits.loadOrFadeWait();
        ConnectorIndexStepTab connectorIndexStep = newConnectionPage.getIndexStep();

        connectorIndexStep.selectIndexButton().click();
        connectorIndexStep.selectIndex(index);

        newConnectionPage.finishButton().click();

        connector.setIndex(index);
        waitForConnectorToRun(connector);

        return connector;
    }

    public ConnectionsDetailPage cancelConnectionScheduling(Connector connector) {
        goToDetails(connector);

        connectionsDetailPage.editButton().click();

        NewConnectionPage newConnectionPage = NewConnectionPage.make(getDriver());

        newConnectionPage.nextButton().click();
        Waits.loadOrFadeWait();

        newConnectionPage.getConnectorConfigStep().skipSchedulingCheckbox().click();

        newConnectionPage.nextButton().click();
        Waits.loadOrFadeWait();
        newConnectionPage.finishButton().click();

        new WebDriverWait(getDriver(), 10).until(GritterNotice.notificationContaining("Connector " + connector.getName() + " schedule has been cancelled successfully"));

        return connectionsDetailPage;
    }
}
