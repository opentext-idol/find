package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.Action;
import com.autonomy.abc.selenium.actions.ActionFactory;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ConnectionActionFactory extends ActionFactory {
    private HSOElementFactory elementFactory;
    private ConnectionsPage connectionsPage;
    private NewConnectionPage newConnectionPage;
    private ConnectionsDetailPage connectionsDetailPage;

    public ConnectionActionFactory(Application application, ElementFactory elementFactory) {
        super(application, elementFactory);
        this.elementFactory = (HSOElementFactory) elementFactory;
    }

    public Action makeSetUpConnection(final Connector connector) {
        return new Action() {
            @Override
            public void apply() {
                goToPage(NavBarTabId.CONNECTIONS);
                connectionsPage = elementFactory.getConnectionsPage();
                connectionsPage.newConnectionButton().click();
                newConnectionPage = elementFactory.getNewConnectionPage();
                connector.makeWizard(newConnectionPage).apply();
                new WebDriverWait(getDriver(), 300).until(GritterNotice.notificationContaining(connector.getFinishedNotification()));
            }
        };
    }

    public Action makeGoToDetails(final Connector connector) {
        return makeGoToDetails(connector.getName());
    }

    public Action makeGoToDetails(final String name) {
        return new Action() {
            @Override
            public void apply() {
                goToPage(NavBarTabId.CONNECTIONS);
                connectionsPage = elementFactory.getConnectionsPage();
                connectionsPage.connectionWithTitleContaining(name).click();
                connectionsDetailPage = elementFactory.getConnectionsDetailPage();
            }
        };
    }

    public Action makeDeleteConnection(final Connector connector) {
        return new Action() {
            @Override
            public void apply() {
                makeGoToDetails(connector).apply();
                ConnectionsDetailPage connectionsDetailPage = elementFactory.getConnectionsDetailPage();
                connectionsDetailPage.deleteButton().click();
                connectionsDetailPage.alsoDeleteIndexCheckbox().click();
                connectionsDetailPage.deleteConfirmButton().click();
                connectionsPage = elementFactory.getConnectionsPage();
                new WebDriverWait(getDriver(), 100).until(GritterNotice.notificationContaining(connector.getDeleteNotification()));
                // TODO: CSA-1539
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {}
            }
        };
    }
}
