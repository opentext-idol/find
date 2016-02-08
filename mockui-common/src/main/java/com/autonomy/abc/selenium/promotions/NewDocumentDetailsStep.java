package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.page.promotions.HSOCreateNewPromotionsPage;

public class NewDocumentDetailsStep implements WizardStep {
    private static final String TITLE = "Document details";
    private String docTitle;
    private String docContent;

    private HSOCreateNewPromotionsPage page;

    public NewDocumentDetailsStep(HSOCreateNewPromotionsPage createNewPromotionsPage, String title, String content) {
        docTitle = title;
        docContent = content;
        page = createNewPromotionsPage;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Object apply() {
        page.documentTitle().setValue(docTitle);
        page.documentContent().setValue(docContent);
        return null;
    }

    @Override
    public String toString() {
        return "wizard step " + getTitle();
    }
}
