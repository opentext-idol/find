package com.autonomy.abc.promotions;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.actions.PromotionActionFactory;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.Editable;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.promotions.HSOPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.search.DocumentViewer;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.StaticPromotion;
import com.autonomy.abc.selenium.util.Errors;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.ElementMatchers.disabled;
import static com.autonomy.abc.matchers.PromotionsMatchers.promotionsList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;

public class StaticPromotionsITCase extends ABCTestBase {

    private HSOPromotionsPage promotionsPage;
    private PromotionsDetailPage promotionsDetailPage;
    private SearchPage searchPage;
    private PromotionActionFactory promotionActionFactory;
    private final String title = "title";
    private final String content = "content";
    private final String trigger = "dog";
    private final StaticPromotion promotion = new StaticPromotion(title, content, trigger);

    public StaticPromotionsITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
        assumeThat(type, is(ApplicationType.HOSTED));
    }

    @Override
    public HSOElementFactory getElementFactory() {
        return (HSOElementFactory) super.getElementFactory();
    }

    public void goToDetails() {
        promotionsDetailPage = promotionActionFactory.goToDetails(trigger).apply();
    }

    @Before
    public void setUp() {
        promotionActionFactory = new PromotionActionFactory(getApplication(), getElementFactory());
        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        promotionsPage = getElementFactory().getPromotionsPage();
        promotionActionFactory.makeDeleteAll().apply();
        searchPage = promotionActionFactory.makeCreateStaticPromotion(promotion).apply();
    }

    @Test
    public void testDeleteStaticPromotion() {
        promotionsPage = (HSOPromotionsPage) promotionActionFactory.goToPromotions();
        promotionsPage.promotionDeleteButton(trigger).click();
        final ModalView deleteModal = ModalView.getVisibleModalView(getDriver());
        verifyThat(deleteModal, containsText(trigger));
        WebElement cancelButton = deleteModal.findElement(By.className("btn-default"));
        verifyThat(cancelButton, containsText("Close"));
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
        verifyThat(deleteButton, containsText("Delete"));
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
        promotionActionFactory.makeDelete(trigger).apply();
        WebElement deleted = null;
        try {
            deleted = new WebDriverWait(getDriver(), 10).until(GritterNotice.notificationAppears());
        } catch (Exception e) {}
        verifyThat("delete notification appeared", deleted, not(nullValue()));
        verifyThat(deleted, containsText(promotion.getDeleteNotification()));
    }

    private void checkBadTriggers(String[] triggers, String errorSubstring) {
        for (String trigger : triggers) {
            promotionsDetailPage.addTrigger(trigger);
            verifyThat("trigger '" + trigger + "' not added", promotionsDetailPage.getTriggerList(), hasSize(1));
            verifyThat(promotionsDetailPage.getTriggerError(), containsString(errorSubstring));
            verifyThat(promotionsDetailPage.triggerAddButton(), disabled());
        }
    }

    // TODO: this same test should apply for promotions, create promotions, keywords and create keywords?
    @Test
    public void testInvalidTriggers() {
        goToDetails();
        final String[] duplicateTriggers = {
                "dog",
                " dog",
                "dog ",
                " dog  ",
                "\"dog\""
        };
        final String[] quoteTriggers = {
                "\"bad",
                "bad\"",
                "b\"ad",
                "\"trigger with\" 3 quo\"tes"
        };
        final String[] commaTriggers = {
                "comma,",
                ",comma",
                "com,ma",
                ",,,,,,"
        };
        final String[] caseTriggers = {
                "Dog",
                "doG",
                "DOG"
        };
        assertThat(promotionsDetailPage.getTriggerList(), hasSize(1));

        checkBadTriggers(duplicateTriggers, Errors.Term.DUPLICATE_EXISTING);
        checkBadTriggers(quoteTriggers, Errors.Term.QUOTES);
        checkBadTriggers(commaTriggers, Errors.Term.COMMAS);
        checkBadTriggers(caseTriggers, Errors.Term.CASE);

        FormInput triggerBox = promotionsDetailPage.triggerAddBox();
        WebElement addButton = promotionsDetailPage.triggerAddButton();

        triggerBox.setValue("a");
        verifyThat("error message is cleared", promotionsDetailPage.getTriggerError(), isEmptyOrNullString());
        verifyThat(addButton, not(disabled()));

        triggerBox.setValue("    ");
        verifyThat("cannot add '     '", promotionsDetailPage.triggerAddButton(), disabled());
        triggerBox.setValue("\t");
        verifyThat("cannot add '\\t'", promotionsDetailPage.triggerAddButton(), disabled());
        promotionsDetailPage.addTrigger("\"valid trigger\"");
        verifyThat("can add valid trigger", promotionsDetailPage.getTriggerList(), hasSize(2));
    }

    @Test
    public void testPromotionViewable() {
        final String handle = getDriver().getWindowHandle();
        searchPage.getPromotedResult(1).click();
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

        promotionsDetailPage.promotionTitle().setValueAndWait(newTitle);
        promotionsDetailPage.addTrigger(newTrigger);
        promotionsDetailPage.trigger(trigger).removeAndWait();
        verifyThat(promotionsDetailPage.getTriggerList(), hasSize(1));
        promotionsPage = (HSOPromotionsPage) promotionActionFactory.goToPromotions();

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
