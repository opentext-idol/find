package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.search.Search;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class PromotionService {
    private Application application;
    private ElementFactory elementFactory;
    private PromotionsPage promotionsPage;

    public PromotionService(Application application, ElementFactory elementFactory) {
        this.application = application;
        this.elementFactory = elementFactory;
    }

    protected WebDriver getDriver() {
        return getElementFactory().getDriver();
    }

    protected ElementFactory getElementFactory() {
        return elementFactory;
    }

    protected AppBody getBody() {
        return application.createAppBody(getDriver());
    }

    public PromotionsPage goToPromotions() {
        getBody().getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        promotionsPage = getElementFactory().getPromotionsPage();
        return promotionsPage;
    }

    public PromotionsDetailPage goToDetails(Promotion promotion) {
        return goToDetails(promotion.getTrigger());
    }

    public PromotionsDetailPage goToDetails(String title) {
        goToPromotions();
        promotionsPage.getPromotionLinkWithTitleContaining(title).click();
        return getElementFactory().getPromotionsDetailPage();
    }

    public List<String> setUpPromotion(Promotion promotion, Search search, int numberOfDocs) {
        SearchPage searchPage = search.apply();
        List<String> promotedDocTitles = null;

        if (promotion instanceof DynamicPromotion) {
            searchPage.promoteThisQueryButton().click();
        } else {
            searchPage.promoteTheseDocumentsButton().click();
            promotedDocTitles = searchPage.addToBucket(numberOfDocs);
            ElementUtil.waitUntilClickableThenClick(searchPage.promoteTheseItemsButton(), getDriver());
        }
        promotion.makeWizard(getElementFactory().getCreateNewPromotionsPage()).apply();
        getElementFactory().getSearchPage();
        return promotedDocTitles;
    }

    public List<String> setUpPromotion(Promotion promotion, String searchTerm, int numberOfDocs) {
        return setUpPromotion(promotion, new Search(application, elementFactory, searchTerm), numberOfDocs);
    }

    public PromotionsPage delete(Promotion promotion) {
        goToPromotions();
        promotionsPage.promotionDeleteButton(promotion.getTrigger()).click();
        final ModalView deleteModal = ModalView.getVisibleModalView(getDriver());
        deleteModal.findElement(By.cssSelector(".btn-danger")).click();
        new WebDriverWait(getDriver(), 20).until(GritterNotice.notificationContaining(promotion.getDeleteNotification()));
        return promotionsPage;
    }

    private WebElement deleteNoWait(WebElement element) {
        WebElement deleteButton = promotionsPage.promotionDeleteButton(element);
        deleteButton.click();
        final ModalView deleteModal = ModalView.getVisibleModalView(getDriver());
        deleteModal.findElement(By.cssSelector(".btn-danger")).click();
        Waits.loadOrFadeWait();
        return deleteButton;
    }

    public PromotionsPage delete(String title) {
        goToPromotions();
        WebElement deleteButton = deleteNoWait(promotionsPage.getPromotionLinkWithTitleContaining(title));
        new WebDriverWait(getDriver(), 20).until(ExpectedConditions.stalenessOf(deleteButton));
        return promotionsPage;
    }

    public PromotionsPage deleteAll() {
        goToPromotions();
        // TODO: possible stale element? refresh promotionsList, or select using int?
        // (multiple promotions may have the same title)
        List<WebElement> promotionsList = promotionsPage.promotionsList();
        for (WebElement promotion : promotionsList) {
            deleteNoWait(promotion);
        }
        new WebDriverWait(getDriver(), 10*(promotionsList.size() + 1 )).withMessage("Promotions list not cleared").until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return promotionsPage.promotionsList().isEmpty();
            }
        });
        return promotionsPage;
    }
}
