package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.element.Wizard;

public class SpotlightPromotion extends Promotion {
    private final static Type TYPE = Type.SPOTLIGHT;
    private SpotlightType spotlightType;

    public SpotlightPromotion(String trigger) {
        this(SpotlightType.SPONSORED, trigger);
    }

    public SpotlightPromotion(SpotlightType type, String trigger) {
        super(trigger);
        spotlightType = type;
    }

    public Type getType() {
        return TYPE;
    }

    public SpotlightType getSpotlightType() {
        return spotlightType;
    }

    public void doWizard(Wizard wizard) {
        doType(wizard);
        String title = wizard.getTitle();
        if (wizard.getTitle().equals("Promotion details")) {
            doSpotlightType(wizard);
        }
        doTriggers(wizard);
    }
}
