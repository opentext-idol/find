package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.element.Wizard;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class Promotion {
    private String trigger;

    public Promotion(String trigger) {
        this.trigger = trigger;
    }

    public String getTrigger() {
        return trigger;
    }

    public Type getType() {
        return null;
    };

    public SpotlightType getSpotlightType() {
        return null;
    }

    public abstract void doWizard(Wizard wizard);

    // these steps are not always relevant, but often are
    public void doType(Wizard wizard) {
        wizard.option(getType().getOption()).click();
        wizard.continueButton().click();
        wizard.loadOrFadeWait();
    }

    public void doSpotlightType(Wizard wizard) {
        wizard.option(getSpotlightType().getOption()).click();
        wizard.continueButton().click();
        wizard.loadOrFadeWait();
    }

    public void doTriggers(Wizard wizard) {
        wizard.formInput().setAndSubmit(getTrigger());
        wizard.finishButton().click();
        wizard.loadOrFadeWait();
    }

    public PromotionsPage getDetailsPage(AppBody body, ElementFactory elementFactory) {
        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        PromotionsPage promotionsPage = elementFactory.getPromotionsPage();
        promotionsPage.getPromotionLinkWithTitleContaining(trigger).click();
        new WebDriverWait(elementFactory.getDriver(), 10).until(ExpectedConditions.visibilityOf(promotionsPage.triggerAddButton()));
        return promotionsPage;
    }

    public enum Type {
        SPOTLIGHT("SPOTLIGHT"),
        PIN_TO_POSITION("PIN_TO_POSITION");

        private String option;

        Type(String option) {
            this.option = option;
        }

        public String getOption() {
            return option;
        }
    }

    public enum SpotlightType {
        SPONSORED("Sponsored"),
        HOTWIRE("Hotwire"),
        TOP_PROMOTIONS("Top Promotions");

        private String option;

        SpotlightType(String option) {
            this.option = option;
        }

        public String getOption() {
            return option;
        }
    }

}
