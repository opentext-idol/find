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

import java.util.List;

public class PromotionActionFactory extends ActionFactory {
    public PromotionActionFactory(Application application, ElementFactory elementFactory) {
        super(application, elementFactory);
    }

    public Action goToDetails(String title) {
        return new GoToDetailsAction(title);
    }

    public Action makeDelete(String title) {
        return new DeleteAction(title);
    }

    public Action makeDeleteAll() {
        return new DeleteAllAction();
    }

    public class GoToDetailsAction implements Action {
        private String title;

        private GoToDetailsAction(String title) {
            this.title = title;
        }

        @Override
        public void apply() {
            AppBody body = getApplication().createAppBody(getDriver());
            body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
            PromotionsPage promotionsPage = getElementFactory().getPromotionsPage();
            promotionsPage.getPromotionLinkWithTitleContaining(title).click();
            getElementFactory().getPromotionsDetailPage();
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
            final PromotionsDetailPage promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
            final Dropdown editMenu = promotionsDetailPage.editMenu();
            editMenu.open();
            editMenu.getItem("Delete").click();
            final ModalView deleteModal = ModalView.getVisibleModalView(getDriver());
            deleteModal.findElement(By.cssSelector(".btn-danger")).click();
            getElementFactory().getPromotionsPage();
        }
    }

    public class DeleteAllAction implements Action {
        private DeleteAllAction() {}

        @Override
        public void apply() {
            AppBody body = getApplication().createAppBody(getDriver());
            body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
            List<String> titles = getElementFactory().getPromotionsPage().getPromotionTitles();
            for (String title : titles) {
                new DeleteAction(title).apply();
            }
        }
    }
}
