package com.autonomy.abc.selenium.promotions;


import com.autonomy.abc.selenium.actions.wizard.BlankWizardStep;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsBase;
import com.autonomy.abc.selenium.page.promotions.HSOCreateNewPromotionsPage;


public class StaticPromotion extends Promotion {
    private String title;
    private String content;

    public StaticPromotion(String title, String content, String trigger) {
        super(trigger);
        this.title = title;
        this.content = content;
    }

    @Override
    public String getName() {
        return "static";
    }

    @Override
    public Wizard makeWizard(CreateNewPromotionsBase newPromotionsBase) {
        return new StaticPromotionWizard((HSOCreateNewPromotionsPage) newPromotionsBase);
    }

    private class StaticPromotionWizard extends PromotionWizard {
        public StaticPromotionWizard(HSOCreateNewPromotionsPage page) {
            super(page);
            add(new NewDocumentDetailsStep(page, title, content));
            add(new BlankWizardStep("Language"));
            add(new SearchTriggerStep(page, getTrigger()));
        }
    }
}
