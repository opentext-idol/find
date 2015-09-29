package com.autonomy.abc.selenium.actions;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.element.Dropdown;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.HSOPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.DynamicPromotion;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.StaticPromotion;
import com.autonomy.abc.selenium.search.Search;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.openqa.selenium.By;

import java.util.List;

public class PromotionActionFactory extends ActionFactory {
    private PromotionsPage promotionsPage;
    private CreateNewPromotionsPage createNewPromotionsPage;

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

    public Action makeBeginPromotion(final Search search, final int numberOfDocs) {
        return new Action() {
            @Override
            public void apply() {
                search.apply();
                SearchPage searchPage = getElementFactory().getSearchPage();
                searchPage.promoteTheseDocumentsButton().click();
                searchPage.addToBucket(numberOfDocs);
                searchPage.waitUntilClickableThenClick(searchPage.promoteTheseItemsButton());
            }
        };
    }

    public Action makeCreateStaticPromotion(final StaticPromotion promotion) {
        return new Action() {
            @Override
            public void apply() {
                goToPage(NavBarTabId.PROMOTIONS);
                promotionsPage = getElementFactory().getPromotionsPage();
                ((HSOPromotionsPage) promotionsPage).staticPromotionButton().click();
                createNewPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
                promotion.makeWizard(createNewPromotionsPage).apply();
            }
        };
    }

    public Action makeCreatePromotion(final Promotion promotion, final Search search) {
        if (promotion instanceof DynamicPromotion) {
            return makeCreateDynamicPromotion((DynamicPromotion) promotion, search);
        }
        return makeCreatePromotion(promotion, search, 1);
    }

    public Action makeCreateDynamicPromotion(final DynamicPromotion promotion, final Search search) {
        return new Action() {
            @Override
            public void apply() {
                search.apply();
                SearchPage searchPage = getElementFactory().getSearchPage();
                searchPage.promoteThisQueryButton().click();
                createNewPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
                promotion.makeWizard(createNewPromotionsPage).apply();
            }
        };
    }

    public Action makeCreatePromotion(final Promotion promotion, final Search search, final int numberOfDocs) {
        return new Action() {
            @Override
            public void apply() {
                makeBeginPromotion(search, numberOfDocs).apply();
                createNewPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
                promotion.makeWizard(createNewPromotionsPage).apply();
            }
        };
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
            promotionsPage = getElementFactory().getPromotionsPage();
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
