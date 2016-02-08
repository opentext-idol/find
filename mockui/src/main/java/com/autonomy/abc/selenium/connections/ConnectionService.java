package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.application.HSOApplication;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.navigation.HSODElementFactory;
import com.autonomy.abc.selenium.page.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorIndexStepTab;
import com.autonomy.abc.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ConnectionService extends ServiceBase<HSODElementFactory> {
    private ConnectionsPage connectionsPage;
    private ConnectionsDetailPage connectionsDetailPage;
    private final static Logger LOGGER = LoggerFactory.getLogger(ConnectionService.class);

    public ConnectionService(HSOApplication application) {
        super(application);
    }

    public ConnectionsPage goToConnections() {
        connectionsPage = getApplication().switchTo(ConnectionsPage.class);
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
        connector.makeWizard(getElementFactory().getNewConnectionPage()).apply();
        new WebDriverWait(getDriver(), 30)
                .withMessage("starting connection")
                .until(GritterNotice.notificationContaining("started"));
        LOGGER.info("Connection '" + connector.getName() + "' started");
        waitForConnectorToRun(connector);
        return connectionsPage;
    }

    private void waitForConnectorToRun(final Connector connector) {
        Long startTime = System.currentTimeMillis();
        new WebDriverWait(getDriver(), 300)
                .withMessage("running connection " + connector)
                .until(GritterNotice.notificationContaining(connector.getFinishedNotification()));
        LOGGER.info("Connection '" + connector.getName() + "' finished");
        if(connector instanceof WebConnector){
            int timeTaken = (int) ((System.currentTimeMillis() - startTime) / 1000);
            int duration = ((WebConnector) connector).getDuration();
            if(timeTaken > duration) {
                LOGGER.error("Connection '" + connector.getName() + "' took " + timeTaken + " seconds to complete, should have taken " + duration + " seconds");
            }
        }
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
        connectionsPage = getElementFactory().getConnectionsPage();
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

        NewConnectionPage newConnectionPage = getElementFactory().getNewConnectionPage();

        for(int i = 0; i < 2; i++) {
            newConnectionPage.nextButton().click();
            Waits.loadOrFadeWait();
        }

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

        NewConnectionPage newConnectionPage = getElementFactory().getNewConnectionPage();

        newConnectionPage.nextButton().click();
        Waits.loadOrFadeWait();

        newConnectionPage.getConnectorConfigStep().skipSchedulingCheckbox().click();

        newConnectionPage.nextButton().click();
        Waits.loadOrFadeWait();
        newConnectionPage.finishButton().click();

        new WebDriverWait(getDriver(), 20).until(GritterNotice.notificationContaining("Connector " + connector.getName() + " schedule has been cancelled successfully"));

        return connectionsDetailPage;
    }
}
