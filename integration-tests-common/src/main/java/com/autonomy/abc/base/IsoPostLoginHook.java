package com.autonomy.abc.base;

import com.autonomy.abc.selenium.actions.Command;
import com.autonomy.abc.selenium.application.SearchOptimizerApplication;

public class IsoPostLoginHook implements Command {
    private final SearchOptimizerApplication<?> app;

    IsoPostLoginHook(SearchOptimizerApplication<?> app) {
        this.app = app;
    }

    @Override
    public void execute() throws Exception {
        //Wait for page to load
        Thread.sleep(2000);
        // wait for the first page to load
        app.elementFactory().getPromotionsPage();
    }
}
