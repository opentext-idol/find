package com.autonomy.abc.selenium.users;

import org.openqa.selenium.WebDriver;

public final class NullAuthStrategy implements GoesToAuthPage {
    private static final GoesToAuthPage INSTANCE = new NullAuthStrategy();

    private NullAuthStrategy() {}

    public static GoesToAuthPage getInstance() {
        return INSTANCE;
    }

    @Override
    public void tryGoingToAuthPage(WebDriver driver) throws Exception {}

    @Override
    public void cleanUp(WebDriver driver) {}
}
