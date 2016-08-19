package com.autonomy.abc.fixtures;

import com.autonomy.abc.config.DualConfigLocator;
import com.autonomy.abc.selenium.actions.Command;
import com.autonomy.abc.selenium.application.IsoApplication;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsoPostLoginHook implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(IsoPostLoginHook.class);
    private final IsoApplication<?> app;

    public IsoPostLoginHook(final IsoApplication<?> app) {
        this.app = app;
    }

    @Override
    public void execute() throws Exception {
        //Wait for page to load
        Thread.sleep(2000);
        getCorrectURL(app);
        // wait for the first page to load
        app.elementFactory().getPromotionsPage();
    }

    private static void getCorrectURL(final IsoApplication<?> app) {
        WebDriver driver = app.elementFactory().getDriver();

        String correctURL;
        try {
            correctURL = new DualConfigLocator()
                    .getJsonConfig()
                    .getAppUrl("search")
                    .toString();
        } catch (Exception e) {
            LOGGER.warn("Could not get correct URL from config file to redirect to it");
            correctURL = driver.getCurrentUrl();
        }
        if (!driver.getCurrentUrl().equals(correctURL)) {
            driver.navigate().to(correctURL);
        }
    }
}
