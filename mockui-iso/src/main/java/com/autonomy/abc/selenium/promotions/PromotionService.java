package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.application.IsoElementFactory;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.SearchPage;
import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class PromotionService<T extends IsoElementFactory> extends ServiceBase<T> {
    private PromotionsPage promotionsPage;

    public PromotionService(final IsoApplication<? extends T> application) {
        super(application);
    }

    public PromotionsPage goToPromotions() {
        promotionsPage = getApplication().switchTo(PromotionsPage.class);
        return promotionsPage;
    }

    public PromotionsDetailPage goToDetails(final Promotion promotion) {
        return goToDetails(promotion.getTrigger());
    }

    public PromotionsDetailPage goToDetails(final String title) {
        goToPromotions();
        promotionsPage.getPromotionLinkWithTitleContaining(title).click();
        return getElementFactory().getPromotionsDetailPage();
    }

    public List<String> setUpPromotion(final Promotion promotion, final Query query, final int numberOfDocs) {
        final SearchPage searchPage = getApplication().searchService().search(query);
        searchPage.openPromotionsBucket();
        final List<String> promotedDocTitles = searchPage.addDocsToBucket(numberOfDocs);

        if (promotion instanceof DynamicPromotion) {
            searchPage.promoteThisQueryButton().click();
        } else {
            DriverUtil.scrollIntoView(getDriver(), searchPage.promoteTheseItemsButton());
            DriverUtil.waitUntilClickableThenClick(getDriver(), searchPage.promoteTheseItemsButton());
        }

        promotion.makeWizard(getElementFactory().getCreateNewPromotionsPage()).apply();
        waitForPromotionToBeCreated();
        return promotedDocTitles;
    }

    public List<String> setUpPromotion(final Promotion promotion, final String searchTerm, final int numberOfDocs) {
        return setUpPromotion(promotion, new Query(searchTerm), numberOfDocs);
    }

    private void waitForPromotionToBeCreated() {
        getElementFactory().getSearchPage();
    }

    public PromotionsPage delete(final Promotion promotion) {
        goToPromotions();
        waitForDeleteButtonsToBeClickable();
        promotionsPage.promotionDeleteButton(promotion.getTrigger()).click();
        final ModalView deleteModal = ModalView.getVisibleModalView(getDriver());
        deleteModal.findElement(By.cssSelector(".btn-danger")).click();
        new WebDriverWait(getDriver(), 20).until(GritterNotice.notificationContaining(promotion.getDeleteNotification()));
        return promotionsPage;
    }

    private WebElement deleteNoWait(final WebElement element) {
        final WebElement deleteButton = promotionsPage.promotionDeleteButton(element);
        deleteButton.click();
        final ModalView deleteModal = ModalView.getVisibleModalView(getDriver());
        deleteModal.findElement(By.cssSelector(".btn-danger")).click();
        Waits.loadOrFadeWait();
        return deleteButton;
    }

    public PromotionsPage delete(final String title) {
        goToPromotions();
        waitForDeleteButtonsToBeClickable();
        final WebElement deleteButton = deleteNoWait(promotionsPage.getPromotionLinkWithTitleContaining(title));
        new WebDriverWait(getDriver(), 20)
                .withMessage("deleting promotion with title " + title)
                .until(ExpectedConditions.stalenessOf(deleteButton));
        return promotionsPage;
    }

    public PromotionsPage deleteAll() {
        goToPromotions();
        waitForDeleteButtonsToBeClickable();
        // TODO: possible stale element? refresh promotionsList, or select using int?
        // (multiple promotions may have the same title)
        final List<WebElement> promotionsList = promotionsPage.promotionsList();
        for (final WebElement promotion : promotionsList) {
            deleteNoWait(promotion);
        }
        waitForPromotionsToBeDeleted(promotionsList.size());
        return promotionsPage;
    }

    private void waitForDeleteButtonsToBeClickable() {
        new WebDriverWait(getDriver(), 5)
                .withMessage("waiting for notifications to clear")
                .until(GritterNotice.notificationsDisappear());
    }

    private void waitForPromotionsToBeDeleted(final int numberOfPromotions) {
        final int duration = 10 * (numberOfPromotions + 2);
        new WebDriverWait(getDriver(), duration)
                .withMessage("deleting promotions")
                .until(promotionsAreDeleted());
    }

    private ExpectedCondition<Boolean> promotionsAreDeleted() {
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(final WebDriver input) {
                return promotionsPage.promotionsList().isEmpty();
            }
        };
    }
}
