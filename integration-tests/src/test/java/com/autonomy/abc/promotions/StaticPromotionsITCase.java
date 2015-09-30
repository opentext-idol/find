package com.autonomy.abc.promotions;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.actions.PromotionActionFactory;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.Editable;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsBase;
import com.autonomy.abc.selenium.page.promotions.HSOCreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.HSOPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.StaticPromotion;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.ElementMatchers.hasAttribute;
import static com.autonomy.abc.matchers.PromotionsMatchers.promotionsList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;

public class StaticPromotionsITCase extends ABCTestBase {

    private HSOPromotionsPage promotionsPage;
    private HSOCreateNewPromotionsPage createPromotionsPage;
    private PromotionsDetailPage promotionsDetailPage;
    private SearchPage searchPage;
    private PromotionActionFactory promotionActionFactory;

    public StaticPromotionsITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
        assumeThat(type, is(ApplicationType.HOSTED));
    }

    @Override
    public HSOElementFactory getElementFactory() {
        return (HSOElementFactory) super.getElementFactory();
    }

    private StaticPromotion setUpStaticPromotion(String trigger) {
        StaticPromotion promotion = new StaticPromotion("title", "content", trigger);
        searchPage = promotionActionFactory.makeCreateStaticPromotion(promotion).apply();
        return promotion;
    }

    @Before
    public void setUp() {
        promotionActionFactory = new PromotionActionFactory(getApplication(), getElementFactory());
        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        promotionsPage = getElementFactory().getPromotionsPage();
        promotionActionFactory.makeDeleteAll().apply();
    }

    @Test
    public void testAddStaticPromotion() {
        final String title = "static promotion";
        final String content = "This is the body of my static promotion.";
        final String trigger = "horse";
        promotionsPage.staticPromotionButton().click();
        createPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        Wizard wizard = new StaticPromotion(title, content, trigger).makeWizard(createPromotionsPage);

        verifyThat(createPromotionsPage.getCurrentStepTitle(), is(wizard.getCurrentStep().getTitle()));
        verifyThat(createPromotionsPage.continueButton(), hasAttribute("disabled"));
        createPromotionsPage.documentTitle().setValue(title);
        verifyThat(createPromotionsPage.continueButton(), hasAttribute("disabled"));
        createPromotionsPage.documentContent().setValue(content);
        verifyThat(createPromotionsPage.continueButton(), not(hasAttribute("disabled")));
        wizard.next();

        verifyThat(createPromotionsPage.getCurrentStepTitle(), is(wizard.getCurrentStep().getTitle()));
        verifyThat(createPromotionsPage.continueButton(), not(hasAttribute("disabled")));
        wizard.getCurrentStep().apply();
        wizard.next();

        verifyThat(createPromotionsPage.getCurrentStepTitle(), is(wizard.getCurrentStep().getTitle()));
        verifyThat(createPromotionsPage.finishButton(), hasAttribute("disabled"));
        wizard.getCurrentStep().apply();
        verifyThat(createPromotionsPage.finishButton(), not(hasAttribute("disabled")));
        wizard.next();
    }

    @Test
    public void testDeleteStaticPromotion() {
        final String trigger = "deletepromotion";
        final StaticPromotion promotion = setUpStaticPromotion(trigger);
        promotionsDetailPage = promotionActionFactory.goToDetails(trigger).apply();
        promotionsDetailPage.editMenu().select("Delete");
        final ModalView deleteModal = ModalView.getVisibleModalView(getDriver());
        verifyThat(deleteModal, containsText(trigger));
        WebElement cancelButton = deleteModal.findElement(By.className("btn-default"));
        verifyThat(cancelButton, containsText("Close"));
        cancelButton.click();

        new WebDriverWait(getDriver(), 10).until(ExpectedConditions.stalenessOf(deleteModal));
        verifyThat("bottom right close button works", promotionsDetailPage.promotionTitle().getValue(), containsString(trigger));

        promotionsDetailPage.editMenu().select("Delete");
        ModalView.getVisibleModalView(getDriver()).close();
        verifyThat("top right close button works", promotionsDetailPage.promotionTitle().getValue(), containsString(trigger));

        promotionsDetailPage.editMenu().select("Delete");
        final ModalView thirdDeleteModal = ModalView.getVisibleModalView(getDriver());
        final WebElement deleteButton = thirdDeleteModal.findElement(By.cssSelector(".btn-danger"));
        verifyThat(deleteButton, containsText("Delete"));
        deleteButton.click();
        promotionsPage = getElementFactory().getPromotionsPage();
        verifyThat(promotionsPage, promotionsList(not(hasItem(containsText(trigger)))));
    }

    @Test
    public void testEditStaticPromotion() {
        final String originalTitle = "apple";
        final String originalContent = "banana cherry";
        final String trigger = "fruit";
        final StaticPromotion promotion = new StaticPromotion(originalTitle, originalContent, trigger);
        promotionActionFactory.makeCreateStaticPromotion(promotion).apply();
        promotionsDetailPage = promotionActionFactory.goToDetails(trigger).apply();

        Editable title = promotionsDetailPage.staticPromotedDocumentTitle();
        final Editable content = promotionsDetailPage.staticPromotedDocumentContent();
        verifyThat(title.getValue(), is(originalTitle));
        verifyThat(content.getValue(), is(originalContent));

        final String secondTitle = "SOMETHING ELSE";
        title.setValueAndWait(secondTitle);
        verifyThat(title.getValue(), is(secondTitle));
        verifyThat(content.getValue(), is(originalContent));
        final String secondContent = "apple";
        content.setValueAndWait(secondContent);
        verifyThat(content.getValue(), is(secondContent));

        getDriver().navigate().refresh();
        promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
        verifyThat("new value stays after page refresh", promotionsDetailPage.staticPromotedDocumentContent().getValue(), is(secondContent));
    }

    @Test
    public void testStaticPromotionNotifications() {
        final String trigger = "banana";
        final StaticPromotion promotion = setUpStaticPromotion(trigger);

        WebElement created = null;
        try {
            created = new WebDriverWait(getDriver(), 10).until(GritterNotice.notificationAppears());
        } catch (Exception e) {}
        verifyThat("creation notification appeared", created, not(nullValue()));
        verifyThat(created, containsText(promotion.getCreateNotification()));

        new WebDriverWait(getDriver(), 10).until(ExpectedConditions.stalenessOf(created));
        promotionsDetailPage = promotionActionFactory.goToDetails(trigger).apply();
        promotionsDetailPage.staticPromotedDocumentTitle().setValueAndWait("different");

        WebElement edited = null;
        try {
            edited = new WebDriverWait(getDriver(), 10).until(GritterNotice.notificationAppears());
        } catch (Exception e) {}
        verifyThat("edit notification appeared", edited, not(nullValue()));
        verifyThat(edited, containsText(promotion.getEditNotification()));

        new WebDriverWait(getDriver(), 10).until(ExpectedConditions.stalenessOf(edited));
        promotionsDetailPage.delete();
        WebElement deleted = null;
        try {
            deleted = new WebDriverWait(getDriver(), 10).until(GritterNotice.notificationAppears());
        } catch (Exception e) {}
        verifyThat("delete notification appeared", deleted, not(nullValue()));
        verifyThat(deleted, containsText(promotion.getDeleteNotification()));
    }
}
