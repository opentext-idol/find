package com.autonomy.abc.selenium.actions;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.element.Dropdown;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.List;

public class PromotionActionFactory {
    private Application application;
    private WebDriver driver;
    private ElementFactory elementFactory;

    public PromotionActionFactory(Application application, WebDriver driver) {
        this.application = application;
        this.driver = driver;
        this.elementFactory = application.createElementFactory(driver);
    }

    public Action goToDetails(String title) {
        return new GoToDetailsAction(title);
    }

    public Action delete(String title) {
        return new DeleteAction(title);
    }

    public Action deleteAll() {
        return new DeleteAllAction();
    }

    public class GoToDetailsAction implements Action {
        private String title;

        private GoToDetailsAction(String title) {
            this.title = title;
        }

        @Override
        public void apply() {
            AppBody body = application.createAppBody(driver);
            body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
            PromotionsPage promotionsPage = elementFactory.getPromotionsPage();
            promotionsPage.getPromotionLinkWithTitleContaining(title).click();
            elementFactory.getPromotionsDetailPage();
        }
    }

    public class DeleteAction implements Action {
        private String title;

        private DeleteAction(String title) {
            this.title = title;
        }

        @Override
        public void apply() {
            new GoToDetailsAction(title).apply();
            final PromotionsDetailPage promotionsDetailPage = elementFactory.getPromotionsDetailPage();
            final Dropdown editMenu = promotionsDetailPage.editMenu();
            editMenu.open();
            editMenu.getItem("Delete").click();
            final ModalView deleteModal = ModalView.getVisibleModalView(driver);
            deleteModal.findElement(By.cssSelector(".btn-danger")).click();
            elementFactory.getPromotionsPage();
        }
    }

    public class DeleteAllAction implements Action {
        private DeleteAllAction() {}

        @Override
        public void apply() {
            AppBody body = application.createAppBody(driver);
            body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
            List<String> titles = elementFactory.getPromotionsPage().getPromotionTitles();
            for (String title : titles) {
                new DeleteAction(title).apply();
            }
        }
    }
}
