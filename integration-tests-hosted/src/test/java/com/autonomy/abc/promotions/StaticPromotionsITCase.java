package com.autonomy.abc.promotions;

import com.autonomy.abc.Trigger.SharedTriggerTests;
import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.element.Editable;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.element.PromotionsDetailTriggerForm;
import com.autonomy.abc.selenium.promotions.HSODPromotionsPage;
import com.autonomy.abc.selenium.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.search.DocumentViewer;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.HSODPromotionService;
import com.autonomy.abc.selenium.promotions.StaticPromotion;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.ElementMatchers.hasTextThat;
import static com.autonomy.abc.matchers.PromotionsMatchers.promotionsList;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

public class StaticPromotionsITCase extends HostedTestBase {

    private HSODPromotionsPage promotionsPage;
    private PromotionsDetailPage promotionsDetailPage;
    private SearchPage searchPage;
    private HSODPromotionService promotionService;
    private final String title = "title";
    private final String content = "content";
    private final String trigger = "dog";
    private final StaticPromotion promotion = new StaticPromotion(title, content, trigger);

    public StaticPromotionsITCase(TestConfig config) {
        super(config);
    }

    public void goToDetails() {
        promotionsDetailPage = promotionService.goToDetails(trigger);
    }

    @Before
    public void setUp() {
        promotionService = getApplication().promotionService();

        promotionsPage = (HSODPromotionsPage) promotionService.deleteAll();
        searchPage = promotionService.setUpStaticPromotion(promotion);
    }

    @Test
    public void testDeleteStaticPromotion() {
        promotionsPage = promotionService.goToPromotions();
        promotionsPage.promotionDeleteButton(trigger).click();
        final ModalView deleteModal = ModalView.getVisibleModalView(getDriver());
        verifyThat(deleteModal, containsText(trigger));
        WebElement cancelButton = deleteModal.findElement(By.className("btn-default"));
        verifyThat(cancelButton, hasTextThat(equalToIgnoringCase("Close")));
        cancelButton.click();

        new WebDriverWait(getDriver(), 10).until(ExpectedConditions.stalenessOf(deleteModal));
        verifyThat("bottom right close button works", promotionsPage, promotionsList(hasItem(containsText(trigger))));

        promotionsPage.promotionDeleteButton(trigger).click();
        ModalView modalView2 = ModalView.getVisibleModalView(getDriver());
        modalView2.close();
        verifyThat("top right close button works", promotionsPage, promotionsList(hasItem(containsText(trigger))));

        promotionsPage.promotionDeleteButton(trigger).click();
        final ModalView thirdDeleteModal = ModalView.getVisibleModalView(getDriver());
        final WebElement deleteButton = thirdDeleteModal.findElement(By.cssSelector(".btn-danger"));
        verifyThat(deleteButton, hasTextThat(equalToIgnoringCase("Delete")));
        deleteButton.click();
        new WebDriverWait(getDriver(), 20).until(GritterNotice.notificationContaining(promotion.getDeleteNotification()));
        verifyThat(promotionsPage, promotionsList(not(hasItem(containsText(trigger)))));
    }

    @Test
    public void testEditStaticPromotion() {
        goToDetails();

        Editable editTitle = promotionsDetailPage.staticPromotedDocumentTitle();
        final Editable editContent = promotionsDetailPage.staticPromotedDocumentContent();
        verifyThat(editTitle.getValue(), is(title));
        verifyThat(editContent.getValue(), is(content));

        final String secondTitle = "SOMETHING ELSE";
        editTitle.setValueAndWait(secondTitle);
        verifyThat(editTitle.getValue(), is(secondTitle));
        verifyThat(editContent.getValue(), is(content));
        final String secondContent = "apple";
        editContent.setValueAndWait(secondContent);
        verifyThat(editContent.getValue(), is(secondContent));

        getDriver().navigate().refresh();
        promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
        verifyThat("new value stays after page refresh", promotionsDetailPage.staticPromotedDocumentContent().getValue(), is(secondContent));
    }

    @Test
    public void testStaticPromotionNotifications() {
        verifyNotification("create", promotion.getCreateNotification());

        promotionsDetailPage = promotionService.goToDetails(promotion);
        promotionsDetailPage.staticPromotedDocumentTitle().setValueAsync("different");
        verifyNotification("edit", promotion.getEditNotification());

        promotionService.delete(promotion);
        verifyNotification("delete", promotion.getDeleteNotification());
    }

    private void verifyNotification(String notificationType, String notificationText) {
        WebElement notification = null;
        try {
            notification = new WebDriverWait(getDriver(), 10).until(GritterNotice.notificationAppears());
        } catch (Exception e) {
            e.printStackTrace();
        }
        verifyThat(notificationType + " notification appeared", notification, not(nullValue()));
        verifyThat(notification, containsText(notificationText));
        new WebDriverWait(getDriver(), 10).until(ExpectedConditions.stalenessOf(notification));
    }

    @Test
    public void testInvalidTriggers() {
        goToDetails();

        SharedTriggerTests.badTriggersTest(promotionsDetailPage.getTriggerForm());
    }

    @Test
    public void testPromotionViewable() {
        final String handle = getDriver().getWindowHandle();
        searchPage.promotedDocumentTitle(1).click();
        DocumentViewer documentViewer = DocumentViewer.make(getDriver());
        verifyThat("document has a reference", documentViewer.getField("Reference"), not(isEmptyOrNullString()));

        getDriver().switchTo().frame(getDriver().findElement(By.tagName("iframe")));
        // these fail on Chrome - seems to be an issue with ChromeDriver
        verifyThat(getDriver().findElement(By.cssSelector("h1")), containsText(title));
        verifyThat(getDriver().findElement(By.cssSelector("p")), containsText(content));
        getDriver().switchTo().window(handle);
        documentViewer.close();
    }

    @Test
    public void testPromotionFilter() {
        goToDetails();
        final String newTitle = "aaa";
        final String newTrigger = "alternative";

        PromotionsDetailTriggerForm triggerForm = promotionsDetailPage.getTriggerForm();

        promotionsDetailPage.promotionTitle().setValueAndWait(newTitle);
        triggerForm.addTrigger(newTrigger);
        triggerForm.removeTrigger(trigger);
        verifyThat(triggerForm.getNumberOfTriggers(), is(1));
        promotionsPage = promotionService.goToPromotions();

        promotionsPage.selectPromotionsCategoryFilter("Spotlight");
        verifyThat(promotionsPage.getPromotionTitles(), empty());
        promotionsPage.selectPromotionsCategoryFilter("Static Promotion");
        verifyThat(promotionsPage.getPromotionTitles(), not(empty()));

        promotionsPage.promotionsSearchFilter().sendKeys(newTrigger);
        verifyThat(promotionsPage.getPromotionTitles(), not(empty()));

        promotionsPage.clearPromotionsSearchFilter();
        promotionsPage.selectPromotionsCategoryFilter("All Types");
        promotionsPage.promotionsSearchFilter().sendKeys(trigger);
        verifyThat(promotionsPage.getPromotionTitles(), empty());
    }
}
