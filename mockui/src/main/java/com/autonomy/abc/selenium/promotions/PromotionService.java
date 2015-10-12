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
        return promotion.getDetailsPage(getBody(), getElementFactory());
    }

    public List<String> setUpPromotion(Promotion promotion, Search search, int numberOfDocs) {
        SearchPage searchPage = search.apply();
        searchPage.promoteTheseDocumentsButton().click();
        List<String> promotedDocTitles = searchPage.addToBucket(numberOfDocs);
        if (promotion instanceof DynamicPromotion) {
            searchPage.promoteThisQueryButton().click();
        } else {
            searchPage.waitUntilClickableThenClick(searchPage.promoteTheseItemsButton());
        }
        promotion.makeWizard(getElementFactory().getCreateNewPromotionsPage()).apply();
        getElementFactory().getSearchPage();
        return promotedDocTitles;
    }

    public PromotionsPage delete(Promotion promotion) {
        goToPromotions();
        promotionsPage.promotionDeleteButton(promotion.getTrigger()).click();
        final ModalView deleteModal = ModalView.getVisibleModalView(getDriver());
        deleteModal.findElement(By.cssSelector(".btn-danger")).click();
        new WebDriverWait(getDriver(), 20).until(GritterNotice.notificationContaining(promotion.getDeleteNotification()));
        return promotionsPage;
    }

    private WebElement deleteNoWait(String title) {
        goToPromotions();
        WebElement deleteButton = promotionsPage.promotionDeleteButton(title);
        deleteButton.click();
        final ModalView deleteModal = ModalView.getVisibleModalView(getDriver());
        deleteModal.findElement(By.cssSelector(".btn-danger")).click();
        deleteModal.loadOrFadeWait();
        return deleteButton;
    }

    public PromotionsPage delete(String title) {
        WebElement deleteButton = deleteNoWait(title);
        new WebDriverWait(getDriver(), 20).until(ExpectedConditions.stalenessOf(deleteButton));
        return promotionsPage;
    }

    public PromotionsPage deleteAll() {
        goToPromotions();
        List<String> promotionTitles = promotionsPage.getPromotionTitles();
        for (String title : promotionTitles) {
            deleteNoWait(title);
        }
        new WebDriverWait(getDriver(), 10*(promotionTitles.size() + 1 )).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return promotionsPage.getPromotionTitles().isEmpty();
            }
        });
        return promotionsPage;
    }
}
