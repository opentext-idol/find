package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;

class NewDocumentDetailsStep implements WizardStep {
    private static final String TITLE = "Document details";
    private final String docTitle;
    private final String docContent;

    private final HsodCreateNewPromotionsPage page;

    NewDocumentDetailsStep(final HsodCreateNewPromotionsPage createNewPromotionsPage, final String title, final String content) {
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
