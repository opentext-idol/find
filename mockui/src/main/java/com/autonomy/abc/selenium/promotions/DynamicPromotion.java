package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.element.Wizard;
import com.autonomy.abc.selenium.page.search.SearchPage;

// TODO: refactor using factories
public class DynamicPromotion extends Promotion {
    private int numberOfResults;
    private SpotlightType spotlightType;

    // for HSO
    public DynamicPromotion(int numberOfResults, String trigger) {
        this(SpotlightType.SPONSORED, numberOfResults, trigger);
    }

    // for OP
    public DynamicPromotion(SpotlightType spotlightType, String trigger) {
        this(spotlightType, 10, trigger);
    }

    public DynamicPromotion(SpotlightType spotlightType, int numberOfResults, String trigger) {
        super(trigger);
        this.numberOfResults = numberOfResults;
        this.spotlightType = spotlightType;
    }

    @Override
    public SpotlightType getSpotlightType() {
        return spotlightType;
    }

    @Override
    public void doWizard(Wizard wizard) {
        if (wizard.getTitle().equals("Spotlight type")) {
            doSpotlightType(wizard);
        } else {
            doResults(wizard);
        }
        doTriggers(wizard);
    }

    public void doResults(Wizard wizard) {
        wizard.formInput().setValue(Integer.toString(numberOfResults));
        wizard.continueButton().click();
        wizard.loadOrFadeWait();
    }
}
