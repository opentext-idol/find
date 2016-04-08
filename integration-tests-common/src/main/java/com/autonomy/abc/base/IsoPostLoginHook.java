package com.autonomy.abc.base;

import com.autonomy.abc.selenium.actions.Command;
import com.autonomy.abc.selenium.application.IsoApplication;

public class IsoPostLoginHook implements Command {
    private final IsoApplication<?> app;

    IsoPostLoginHook(IsoApplication<?> app) {
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
