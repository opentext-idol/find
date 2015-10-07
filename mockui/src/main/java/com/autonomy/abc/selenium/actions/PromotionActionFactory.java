package com.autonomy.abc.selenium.actions;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.element.Dropdown;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.promotions.*;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.DynamicPromotion;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.StaticPromotion;
import com.autonomy.abc.selenium.search.Search;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class PromotionActionFactory extends ActionFactory {
    private PromotionsPage promotionsPage;
    private CreateNewPromotionsPage createNewPromotionsPage;

    public PromotionActionFactory(Application application, ElementFactory elementFactory) {
        super(application, elementFactory);
    }

    public PromotionsPage goToPromotions() {
        goToPage(NavBarTabId.PROMOTIONS);
        promotionsPage = getElementFactory().getPromotionsPage();
        return promotionsPage;
    }

    public Action<PromotionsDetailPage> goToDetails(String title) {
        return new GoToDetailsAction(title);
    }

    public Action<PromotionsPage> makeDelete(String title) {
        return new DeleteAction(title);
    }

    public Action<PromotionsPage> makeDeleteAll() {
        return new DeleteAllAction();
    }

    public Action<SearchPage> makeCreateStaticPromotion(final StaticPromotion promotion) {
        return new Action<SearchPage>() {
            @Override
            public SearchPage apply() {
                goToPage(NavBarTabId.PROMOTIONS);
                promotionsPage = getElementFactory().getPromotionsPage();
                ((HSOPromotionsPage) promotionsPage).staticPromotionButton().click();
                createNewPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
                promotion.makeWizard(createNewPromotionsPage).apply();
                return getElementFactory().getSearchPage();
            }
        };
    }

    public Action<List<String>> makeCreatePromotion(final Promotion promotion, final Search search, final int numberOfDocs) {
        return new Action<List<String>>() {
            @Override
            public List<String> apply() {
                search.apply();
                SearchPage searchPage = getElementFactory().getSearchPage();
                searchPage.promoteTheseDocumentsButton().click();
                List<String> promotedDocs = searchPage.addToBucket(numberOfDocs);
                if (promotion instanceof DynamicPromotion) {
                    searchPage.promoteThisQueryButton().click();
                } else {
                    searchPage.waitUntilClickableThenClick(searchPage.promoteTheseItemsButton());
                }
                promotion.makeWizard(getElementFactory().getCreateNewPromotionsPage()).apply();
                getElementFactory().getSearchPage();
                return promotedDocs;
            }
        };
    }

    public class GoToDetailsAction implements Action<PromotionsDetailPage> {
        private String title;

        private GoToDetailsAction(String title) {
            this.title = title;
        }

        @Override
        public PromotionsDetailPage apply() {
            AppBody body = getApplication().createAppBody(getDriver());
            body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
            promotionsPage = getElementFactory().getPromotionsPage();
            promotionsPage.getPromotionLinkWithTitleContaining(title).click();
            return getElementFactory().getPromotionsDetailPage();
        }
    }

    public class DeleteAction implements Action<PromotionsPage> {
        private String title;

        private DeleteAction(String title) {
            this.title = title;
        }

        @Override
        public PromotionsPage apply() {
            goToPage(NavBarTabId.PROMOTIONS);
            promotionsPage = getElementFactory().getPromotionsPage();
            promotionsPage.promotionDeleteButton(title).click();
            final ModalView deleteModal = ModalView.getVisibleModalView(getDriver());
            deleteModal.findElement(By.cssSelector(".btn-danger")).click();
            new WebDriverWait(getDriver(), 20).until(GritterNotice.notificationContaining("Removed a"));
            return promotionsPage;
        }
    }

    public class DeleteAllAction implements Action<PromotionsPage> {
        private DeleteAllAction() {}

        @Override
        public PromotionsPage apply() {
            AppBody body = getApplication().createAppBody(getDriver());
            body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
            List<String> titles = getElementFactory().getPromotionsPage().getPromotionTitles();
            for (String title : titles) {
                new DeleteAction(title).apply();
            }
            return promotionsPage;
        }
    }
}
