package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsBase;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;

public abstract class Promotion {
    private String trigger;

    public Promotion(String trigger) {
        this.trigger = trigger;
    }

    public String getTrigger() {
        return trigger;
    }

    public PromotionsDetailPage getDetailsPage(AppBody body, ElementFactory elementFactory) {
        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        PromotionsPage promotionsPage = elementFactory.getPromotionsPage();
        promotionsPage.getPromotionLinkWithTitleContaining(trigger).click();
        return elementFactory.getPromotionsDetailPage();
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

    public abstract Wizard makeWizard(CreateNewPromotionsBase createNewPromotionsBase);

    protected class PromotionWizard extends Wizard {
        private CreateNewPromotionsBase page;

        public PromotionWizard(CreateNewPromotionsBase createNewPromotionsBase) {
            page = createNewPromotionsBase;
        }

        @Override
        public void next() {
            if (onFinalStep()) {
                page.finishButton().click();
            } else {
                page.continueButton().click();
                incrementStep();
            }
            page.loadOrFadeWait();
        }

        @Override
        public void cancel() {
            page.cancelButton().click();
            page.loadOrFadeWait();
        }
    }

}
